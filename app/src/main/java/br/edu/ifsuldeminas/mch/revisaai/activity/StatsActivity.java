package br.edu.ifsuldeminas.mch.revisaai.activity;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.List;
import java.util.Locale;
import br.edu.ifsuldeminas.mch.revisaai.R;
import br.edu.ifsuldeminas.mch.revisaai.database.AppDatabase;
import br.edu.ifsuldeminas.mch.revisaai.database.FlashcardDAO;
import br.edu.ifsuldeminas.mch.revisaai.model.Flashcard;

public class StatsActivity extends AppCompatActivity {

    private TextView textViewTotalReviewed;
    private TextView textViewSuccessRate;
    private FlashcardDAO flashcardDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        flashcardDao = AppDatabase.getDatabase(this).flashcardDao();

        Toolbar toolbar = findViewById(R.id.toolbarStats);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        textViewTotalReviewed = findViewById(R.id.textViewTotalReviewed);
        textViewSuccessRate = findViewById(R.id.textViewSuccessRate);

        loadStats();
    }

    private void loadStats() {
        List<Flashcard> allFlashcards = flashcardDao.getAllFlashcards();

        if (allFlashcards == null || allFlashcards.isEmpty()) {
            textViewTotalReviewed.setText("Total de Revisões: 0");
            textViewSuccessRate.setText("Taxa de Acerto Geral: 0%");
            return;
        }

        int totalReviews = 0;
        int totalCorrectAnswers = 0;

        for (Flashcard flashcard : allFlashcards) {
            totalReviews += flashcard.getTotalReviews();
            totalCorrectAnswers += flashcard.getCorrectAnswers();
        }

        double successRate = 0;
        if (totalReviews > 0) {
            successRate = ((double) totalCorrectAnswers / totalReviews) * 100;
        }

        textViewTotalReviewed.setText(String.format(Locale.getDefault(), "Total de Revisões: %d", totalReviews));
        textViewSuccessRate.setText(String.format(Locale.getDefault(), "Taxa de Acerto Geral: %.1f%%", successRate));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
