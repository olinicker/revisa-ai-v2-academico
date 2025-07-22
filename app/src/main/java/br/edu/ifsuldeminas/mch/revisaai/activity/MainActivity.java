package br.edu.ifsuldeminas.mch.revisaai.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.edu.ifsuldeminas.mch.revisaai.R;
import br.edu.ifsuldeminas.mch.revisaai.adapter.DeckAdapter;
import br.edu.ifsuldeminas.mch.revisaai.database.AppDatabase;
import br.edu.ifsuldeminas.mch.revisaai.database.DeckDAO;
import br.edu.ifsuldeminas.mch.revisaai.database.FlashcardDAO;
import br.edu.ifsuldeminas.mch.revisaai.model.Deck;
import br.edu.ifsuldeminas.mch.revisaai.model.Flashcard;

public class MainActivity extends AppCompatActivity implements DeckAdapter.OnItemClickListener {

    private RecyclerView recyclerViewDecks;
    private DeckAdapter deckAdapter;
    private List<Deck> decks;
    private DeckDAO deckDao;
    private FlashcardDAO flashcardDao;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Obtém a instância do banco de dados Room e os DAOs
        AppDatabase db = AppDatabase.getDatabase(this);
        deckDao = db.deckDao();
        flashcardDao = db.flashcardDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerViewDecks = findViewById(R.id.recyclerViewDecks);
        recyclerViewDecks.setLayoutManager(new LinearLayoutManager(this));

        decks = new ArrayList<>();
        deckAdapter = new DeckAdapter(this, decks, this);
        recyclerViewDecks.setAdapter(deckAdapter);

        FloatingActionButton fab = findViewById(R.id.fabAddDeck);
        fab.setOnClickListener(view -> showAddDeckDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDecks();
    }

    private void loadDecks() {
        List<Deck> loadedDecks = deckDao.getAll();
        deckAdapter.updateDecks(loadedDecks);
    }

    @Override
    public void onItemClick(Deck deck) {
        Intent intent = new Intent(MainActivity.this, FlashcardListActivity.class);
        intent.putExtra("DECK_ID", deck.getId());
        intent.putExtra("DECK_NAME", deck.getName());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(Deck deck, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.deck_context_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.context_edit) {
                showEditDeckDialog(deck);
                return true;
            } else if (itemId == R.id.context_delete) {
                showDeleteConfirmDialog(deck);
                return true;
            } else if (itemId == R.id.context_review_now) {
                reviewDeckNow(deck);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void reviewDeckNow(Deck deck) {
        List<Flashcard> flashcardsToReview = flashcardDao.getFlashcardsByDeck(deck.getId());
        if (flashcardsToReview.isEmpty()) {
            Toast.makeText(this, "Este deck não tem flashcards para revisar.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, ReviewActivity.class);
            intent.putExtra("FLASHCARD_LIST", (Serializable) flashcardsToReview);
            startActivity(intent);
        }
    }

    private void showAddDeckDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Novo Deck");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("Nome da Matéria");
        builder.setView(input);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String deckName = input.getText().toString().trim();
            if (!deckName.isEmpty()) {
                Deck newDeck = new Deck(); // Usa construtor vazio
                newDeck.setName(deckName);
                deckDao.insert(newDeck);
                Toast.makeText(this, "Deck salvo!", Toast.LENGTH_SHORT).show();
                loadDecks();
            } else {
                Toast.makeText(this, "O nome do deck não pode ser vazio.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showEditDeckDialog(final Deck deck) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Deck");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(deck.getName());
        builder.setView(input);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                deck.setName(newName);
                deckDao.update(deck);
                Toast.makeText(this, "Deck atualizado!", Toast.LENGTH_SHORT).show();
                loadDecks();
            } else {
                Toast.makeText(this, "O nome não pode ser vazio.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteConfirmDialog(final Deck deck) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Deck")
                .setMessage("Tem certeza que deseja excluir o deck '" + deck.getName() + "'? Todos os flashcards dele serão perdidos.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    deckDao.delete(deck);
                    Toast.makeText(this, "Deck excluído!", Toast.LENGTH_SHORT).show();
                    loadDecks();
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_stats) {
            startActivity(new Intent(this, StatsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
