package br.edu.ifsuldeminas.mch.revisaai.activity;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import br.edu.ifsuldeminas.mch.revisaai.R;
import br.edu.ifsuldeminas.mch.revisaai.adapter.GoalAdapter;
import br.edu.ifsuldeminas.mch.revisaai.model.Goal;

public class GoalsActivity extends AppCompatActivity implements GoalAdapter.OnGoalListener {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerViewGoals;
    private GoalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarGoals);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewGoals = findViewById(R.id.recyclerViewGoals);
        recyclerViewGoals.setLayoutManager(new LinearLayoutManager(this));

        setupRecyclerView();

        FloatingActionButton fab = findViewById(R.id.fabAddGoal);
        fab.setOnClickListener(view -> showAddGoalDialog());
    }

    private void setupRecyclerView() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Utilizador não autenticado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();

        Query query = db.collection("goals")
                .whereEqualTo("userId", userId)
                .orderBy("creationDate", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Goal> options = new FirestoreRecyclerOptions.Builder<Goal>()
                .setQuery(query, Goal.class)
                .build();

        adapter = new GoalAdapter(options, this);
        recyclerViewGoals.setAdapter(adapter);
    }

    private void showAddGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nova Meta de Estudo");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setHint("Ex: Terminar o capítulo 5");
        builder.setView(input);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String description = input.getText().toString().trim();
            if (!description.isEmpty()) {
                addGoalToFirestore(description);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addGoalToFirestore(String description) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        Goal newGoal = new Goal();
        newGoal.setDescription(description);
        newGoal.setCompleted(false);
        newGoal.setUserId(currentUser.getUid());

        db.collection("goals").add(newGoal)
                .addOnSuccessListener(documentReference -> Toast.makeText(GoalsActivity.this, "Meta adicionada!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(GoalsActivity.this, "Erro ao adicionar meta.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onGoalChecked(String goalId, boolean isChecked) {
        db.collection("goals").document(goalId).update("completed", isChecked);
    }

    @Override
    public void onGoalLongClick(String goalId) {
        // Opções para Editar e Excluir
        new AlertDialog.Builder(this)
                .setTitle("Ações da Meta")
                .setItems(new CharSequence[]{"Editar", "Excluir"}, (dialog, which) -> {
                    if (which == 0) { // Editar
                        showEditGoalDialog(goalId);
                    } else { // Excluir
                        deleteGoalFromFirestore(goalId);
                    }
                })
                .show();
    }

    private void showEditGoalDialog(String goalId) {
        db.collection("goals").document(goalId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String currentDescription = documentSnapshot.getString("description");

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Editar Meta");

                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                input.setText(currentDescription);
                builder.setView(input);

                builder.setPositiveButton("Salvar", (dialog, which) -> {
                    String newDescription = input.getText().toString().trim();
                    if (!newDescription.isEmpty()) {
                        db.collection("goals").document(goalId).update("description", newDescription);
                    }
                });
                builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
                builder.show();
            }
        });
    }

    private void deleteGoalFromFirestore(String goalId) {
        db.collection("goals").document(goalId).delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(GoalsActivity.this, "Meta excluída.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(GoalsActivity.this, "Erro ao excluir meta.", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
