package br.edu.ifsuldeminas.mch.revisaai.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import br.edu.ifsuldeminas.mch.revisaai.R;
import br.edu.ifsuldeminas.mch.revisaai.database.AppDatabase;
import br.edu.ifsuldeminas.mch.revisaai.database.FlashcardDAO;
import br.edu.ifsuldeminas.mch.revisaai.model.Flashcard;
import java.util.Date;

public class FlashcardAddEditActivity extends AppCompatActivity {

    private EditText editTextFront;
    private EditText editTextBack;
    private Button buttonSave;
    private FlashcardDAO flashcardDao;
    private int deckId;
    private Flashcard currentFlashcard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_add_edit);

        flashcardDao = AppDatabase.getDatabase(this).flashcardDao();

        Toolbar toolbar = findViewById(R.id.toolbarAddEdit);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextFront = findViewById(R.id.editTextFront);
        editTextBack = findViewById(R.id.editTextBack);
        buttonSave = findViewById(R.id.buttonSaveFlashcard);

        if (getIntent().hasExtra("FLASHCARD_TO_EDIT")) {
            currentFlashcard = (Flashcard) getIntent().getSerializableExtra("FLASHCARD_TO_EDIT");
            deckId = currentFlashcard.getDeckId();
            getSupportActionBar().setTitle("Editar Flashcard");
            editTextFront.setText(currentFlashcard.getFront());
            editTextBack.setText(currentFlashcard.getBack());
        } else {
            deckId = getIntent().getIntExtra("DECK_ID", -1);
            getSupportActionBar().setTitle("Criar Novo Flashcard");
        }

        buttonSave.setOnClickListener(v -> saveFlashcard());
    }

    private void saveFlashcard() {
        String front = editTextFront.getText().toString().trim();
        String back = editTextBack.getText().toString().trim();

        if (front.isEmpty() || back.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha ambos os lados do flashcard.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentFlashcard != null) { // Modo Edição
            currentFlashcard.setFront(front);
            currentFlashcard.setBack(back);
            flashcardDao.update(currentFlashcard);
            Toast.makeText(this, "Flashcard atualizado!", Toast.LENGTH_SHORT).show();
        } else { // Modo Adição
            if (deckId != -1) {
                Flashcard newFlashcard = new Flashcard();
                newFlashcard.setFront(front);
                newFlashcard.setBack(back);
                newFlashcard.setDeckId(deckId);
                newFlashcard.setLastReviewed(new Date());
                newFlashcard.setCorrectAnswers(0);
                newFlashcard.setTotalReviews(0);
                flashcardDao.insert(newFlashcard);
                Toast.makeText(this, "Flashcard salvo!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erro: ID do deck inválido.", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
