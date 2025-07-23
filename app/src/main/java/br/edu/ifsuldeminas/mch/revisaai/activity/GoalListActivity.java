package br.edu.ifsuldeminas.mch.revisaai.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.edu.ifsuldeminas.mch.revisaai.R;
import br.edu.ifsuldeminas.mch.revisaai.adapter.GoalAdapter;
import br.edu.ifsuldeminas.mch.revisaai.model.Goal;

public class GoalListActivity extends AppCompatActivity implements GoalAdapter.OnItemClickListener {

    private static final String TAG = "GoalListActivity";
    private RecyclerView recyclerViewGoals;
    private GoalAdapter goalAdapter;
    private List<Goal> goals;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_list);

        // Inicializa o Firebase Firestore e Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inicializa o formatador de data
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Obtém o usuário atual e seu ID
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            // Se não houver usuário logado, redireciona para a tela de login
            Toast.makeText(this, "Usuário não autenticado. Faça login novamente.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Configura a Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarGoals);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Minhas Metas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Configura o RecyclerView
        recyclerViewGoals = findViewById(R.id.recyclerViewGoals);
        recyclerViewGoals.setLayoutManager(new LinearLayoutManager(this));

        goals = new ArrayList<>();
        goalAdapter = new GoalAdapter(this, goals, this);
        recyclerViewGoals.setAdapter(goalAdapter);

        // Configura o Floating Action Button para adicionar novas metas
        FloatingActionButton fab = findViewById(R.id.fabAddGoal);
        fab.setOnClickListener(view -> showAddEditGoalDialog(null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGoals(); // Carrega as metas sempre que a activity for retomada
    }

    private void loadGoals() {
        if (currentUserId == null) {
            return; // Não carrega se o userId for nulo
        }

        // Consulta as metas do Firestore para o usuário atual
        db.collection("goals")
                .whereEqualTo("userId", currentUserId)
                .orderBy("creationDate") // Ordena as metas pela data de criação
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        goals.clear(); // Limpa a lista existente
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Goal goal = document.toObject(Goal.class);
                            goal.setId(document.getId()); // Define o ID do documento como ID da meta
                            goals.add(goal);
                        }
                        goalAdapter.updateGoals(goals); // Atualiza o adapter do RecyclerView
                    } else {
                        Log.w(TAG, "Erro ao carregar metas: ", task.getException());
                        Toast.makeText(GoalListActivity.this, "Erro ao carregar metas.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddEditGoalDialog(final Goal goalToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,  R.style.Theme_RevisaAi_Dialog);
        builder.setTitle(goalToEdit == null ? "Adicionar Nova Meta" : "Editar Meta");

        // Layout para o diálogo com múltiplos campos
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_goal, null);
        builder.setView(dialogView);

        final EditText editTextDescription = dialogView.findViewById(R.id.editTextGoalDescription);
        final EditText editTextCategory = dialogView.findViewById(R.id.editTextGoalCategory);
        final TextView textViewDueDate = dialogView.findViewById(R.id.textViewGoalDueDate); // Agora é TextView
        final Spinner spinnerPriority = dialogView.findViewById(R.id.spinnerGoalPriority); // Agora é Spinner

        // Configura o Spinner de Prioridade
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(this,
                R.array.priority_options, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        // Preenche os campos se estiver no modo de edição
        if (goalToEdit != null) {
            editTextDescription.setText(goalToEdit.getDescription());
            editTextCategory.setText(goalToEdit.getCategory());
            textViewDueDate.setText(goalToEdit.getDueDate()); // Define a data formatada
            // Seleciona o item correto no Spinner
            int spinnerPosition = priorityAdapter.getPosition(goalToEdit.getPriority());
            spinnerPriority.setSelection(spinnerPosition);
        } else {
            // Define a data atual como padrão para a data de vencimento
            textViewDueDate.setText(dateFormatter.format(Calendar.getInstance().getTime()));
        }

        // Configura o DatePickerDialog para a data de vencimento
        textViewDueDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            if (goalToEdit != null && goalToEdit.getDueDate() != null && !goalToEdit.getDueDate().isEmpty()) {
                try {
                    Date date = dateFormatter.parse(goalToEdit.getDueDate());
                    c.setTime(date);
                } catch (ParseException e) {
                    Log.e(TAG, "Erro ao parsear data de vencimento para edição: " + goalToEdit.getDueDate(), e);
                }
            }

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(GoalListActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year1, monthOfYear, dayOfMonth);
                        textViewDueDate.setText(dateFormatter.format(selectedDate.getTime()));
                    }, year, month, day);
            datePickerDialog.show();
        });


        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String description = editTextDescription.getText().toString().trim();
            String category = editTextCategory.getText().toString().trim();
            String dueDate = textViewDueDate.getText().toString().trim(); // Pega do TextView
            String priority = spinnerPriority.getSelectedItem().toString(); // Pega do Spinner

            if (description.isEmpty() || category.isEmpty() || dueDate.isEmpty() || priority.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (goalToEdit == null) {
                // Adicionar nova meta
                Goal newGoal = new Goal();
                newGoal.setDescription(description);
                newGoal.setCompleted(false); // Nova meta não está completa por padrão
                newGoal.setUserId(currentUserId);
                newGoal.setCreationDate(new Date()); // Define a data de criação localmente
                newGoal.setCategory(category);
                newGoal.setDueDate(dueDate);
                newGoal.setPriority(priority);

                addGoal(newGoal);
            } else {
                // Editar meta existente
                goalToEdit.setDescription(description);
                goalToEdit.setCategory(category);
                goalToEdit.setDueDate(dueDate);
                goalToEdit.setPriority(priority);
                updateGoal(goalToEdit);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addGoal(Goal goal) {
        db.collection("goals")
                .add(goal)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(GoalListActivity.this, "Meta adicionada com sucesso!", Toast.LENGTH_SHORT).show();
                    loadGoals(); // Recarrega a lista após adicionar
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Erro ao adicionar meta", e);
                    Toast.makeText(GoalListActivity.this, "Erro ao adicionar meta.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateGoal(Goal goal) {
        if (goal.getId() == null) {
            Toast.makeText(this, "Erro: ID da meta não encontrado para atualização.", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("goals").document(goal.getId())
                .set(goal) // Usa set para sobrescrever o documento, ou update para campos específicos
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(GoalListActivity.this, "Meta atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                    loadGoals(); // Recarrega a lista após atualizar
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Erro ao atualizar meta", e);
                    Toast.makeText(GoalListActivity.this, "Erro ao atualizar meta.", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteGoal(Goal goal) {
        if (goal.getId() == null) {
            Toast.makeText(this, "Erro: ID da meta não encontrado para exclusão.", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("goals").document(goal.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(GoalListActivity.this, "Meta excluída com sucesso!", Toast.LENGTH_SHORT).show();
                    loadGoals(); // Recarrega a lista após excluir
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Erro ao excluir meta", e);
                    Toast.makeText(GoalListActivity.this, "Erro ao excluir meta.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onItemClick(Goal goal) {
        // Implemente o que acontece ao clicar em uma meta (ex: marcar como concluída/incompleta)
        goal.setCompleted(!goal.isCompleted()); // Alterna o estado de conclusão
        updateGoal(goal); // Atualiza no Firestore
    }

    @Override
    public void onItemLongClick(Goal goal, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.goal_context_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.context_goal_edit) {
                showAddEditGoalDialog(goal);
                return true;
            } else if (itemId == R.id.context_goal_delete) {
                showDeleteConfirmDialog(goal);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showDeleteConfirmDialog(final Goal goal) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Meta")
                .setMessage("Tem certeza que deseja excluir a meta '" + goal.getDescription() + "'?")
                .setPositiveButton("Excluir", (dialog, which) -> deleteGoal(goal))
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
