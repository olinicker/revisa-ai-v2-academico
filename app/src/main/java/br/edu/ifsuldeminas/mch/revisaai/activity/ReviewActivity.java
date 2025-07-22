package br.edu.ifsuldeminas.mch.revisaai.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import java.util.Collections;
import java.util.List;
import br.edu.ifsuldeminas.mch.revisaai.R;
import br.edu.ifsuldeminas.mch.revisaai.database.AppDatabase;
import br.edu.ifsuldeminas.mch.revisaai.database.FlashcardDAO;
import br.edu.ifsuldeminas.mch.revisaai.model.Flashcard;

public class ReviewActivity extends AppCompatActivity {

    private List<Flashcard> flashcardsToReview;
    private int currentCardIndex = 0;
    private boolean isShowingFront = true;

    private TextView textViewContent;
    private CardView cardView;
    private Button buttonCorrect;
    private Button buttonWrong;
    private FlashcardDAO flashcardDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        flashcardDao = AppDatabase.getDatabase(this).flashcardDao();

        Toolbar toolbar = findViewById(R.id.toolbarReview);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        flashcardsToReview = (List<Flashcard>) getIntent().getSerializableExtra("FLASHCARD_LIST");

        textViewContent = findViewById(R.id.textViewFlashcardContent);
        cardView = findViewById(R.id.cardViewFlashcard);
        buttonCorrect = findViewById(R.id.buttonCorrect);
        buttonWrong = findViewById(R.id.buttonWrong);

        if (flashcardsToReview != null && !flashcardsToReview.isEmpty()) {
            Collections.shuffle(flashcardsToReview);
            showCurrentCard();
        } else {
            Toast.makeText(this, "Não há flashcards para revisar.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cardView.setOnClickListener(v -> flipCard());
        buttonCorrect.setOnClickListener(v -> processAnswer(true));
        buttonWrong.setOnClickListener(v -> processAnswer(false));
    }

    private void showCurrentCard() {
        if (currentCardIndex < flashcardsToReview.size()) {
            Flashcard currentCard = flashcardsToReview.get(currentCardIndex);
            textViewContent.setText(currentCard.getFront());
            isShowingFront = true;
        } else {
            Toast.makeText(this, "Revisão concluída!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void flipCard() {
        if (currentCardIndex < flashcardsToReview.size()) {
            Flashcard currentCard = flashcardsToReview.get(currentCardIndex);
            if (isShowingFront) {
                textViewContent.setText(currentCard.getBack());
            } else {
                textViewContent.setText(currentCard.getFront());
            }
            isShowingFront = !isShowingFront;
        }
    }

    private void processAnswer(boolean wasCorrect) {
        if (currentCardIndex < flashcardsToReview.size()) {
            Flashcard currentCard = flashcardsToReview.get(currentCardIndex);
            currentCard.setTotalReviews(currentCard.getTotalReviews() + 1);
            if (wasCorrect) {
                currentCard.setCorrectAnswers(currentCard.getCorrectAnswers() + 1);
            }
            // Salva as novas estatísticas no banco de dados usando o método update do Room
            flashcardDao.update(currentCard);
        }
        currentCardIndex++;
        showCurrentCard();
    }

    private void shareCurrentFlashcard() {
        if (currentCardIndex < flashcardsToReview.size()) {
            Flashcard currentCard = flashcardsToReview.get(currentCardIndex);
            String shareText = "RevisaAí - Flashcard:\n\n" +
                    "Pergunta: " + currentCard.getFront() + "\n\n" +
                    "Resposta: " + currentCard.getBack();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, "Compartilhar Flashcard via...");
            startActivity(shareIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.review_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            shareCurrentFlashcard();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
