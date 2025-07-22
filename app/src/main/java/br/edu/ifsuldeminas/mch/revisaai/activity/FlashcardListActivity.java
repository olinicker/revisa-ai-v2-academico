package br.edu.ifsuldeminas.mch.revisaai.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import br.edu.ifsuldeminas.mch.revisaai.R;
import br.edu.ifsuldeminas.mch.revisaai.adapter.FlashcardAdapter;
import br.edu.ifsuldeminas.mch.revisaai.database.AppDatabase;
import br.edu.ifsuldeminas.mch.revisaai.database.FlashcardDAO;
import br.edu.ifsuldeminas.mch.revisaai.model.Flashcard;

public class FlashcardListActivity extends AppCompatActivity implements FlashcardAdapter.OnItemClickListener {

    private RecyclerView recyclerViewFlashcards;
    private FlashcardAdapter flashcardAdapter;
    private List<Flashcard> flashcards;
    private FlashcardDAO flashcardDao;
    private int deckId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_list);

        flashcardDao = AppDatabase.getDatabase(this).flashcardDao();

        deckId = getIntent().getIntExtra("DECK_ID", -1);
        String deckName = getIntent().getStringExtra("DECK_NAME");

        Toolbar toolbar = findViewById(R.id.toolbarFlashcards);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(deckName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewFlashcards = findViewById(R.id.recyclerViewFlashcards);
        recyclerViewFlashcards.setLayoutManager(new LinearLayoutManager(this));

        flashcards = new ArrayList<>();
        flashcardAdapter = new FlashcardAdapter(this, flashcards, this);
        recyclerViewFlashcards.setAdapter(flashcardAdapter);

        FloatingActionButton fab = findViewById(R.id.fabAddFlashcard);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(FlashcardListActivity.this, FlashcardAddEditActivity.class);
            intent.putExtra("DECK_ID", deckId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFlashcards();
    }

    private void loadFlashcards() {
        if (deckId != -1) {
            this.flashcards = flashcardDao.getFlashcardsByDeck(deckId);
            flashcardAdapter.updateFlashcards(this.flashcards);
        }
    }

    @Override
    public void onItemClick(Flashcard flashcard) {
        Intent intent = new Intent(this, ReviewActivity.class);
        ArrayList<Flashcard> singleFlashcardList = new ArrayList<>();
        singleFlashcardList.add(flashcard);
        intent.putExtra("FLASHCARD_LIST", singleFlashcardList);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(Flashcard flashcard, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.flashcard_context_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.context_flashcard_edit) {
                editFlashcard(flashcard);
                return true;
            } else if (itemId == R.id.context_flashcard_delete) {
                deleteFlashcard(flashcard);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void editFlashcard(Flashcard flashcard) {
        Intent intent = new Intent(this, FlashcardAddEditActivity.class);
        intent.putExtra("FLASHCARD_TO_EDIT", flashcard);
        intent.putExtra("DECK_ID", deckId);
        startActivity(intent);
    }

    private void deleteFlashcard(final Flashcard flashcard) {
        flashcardDao.delete(flashcard);
        loadFlashcards();

        Snackbar.make(findViewById(R.id.flashcardListCoordinatorLayout), "Flashcard excluído", Snackbar.LENGTH_LONG)
                .setAction("Desfazer", v -> {
                    flashcardDao.insert(flashcard);
                    loadFlashcards();
                })
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
