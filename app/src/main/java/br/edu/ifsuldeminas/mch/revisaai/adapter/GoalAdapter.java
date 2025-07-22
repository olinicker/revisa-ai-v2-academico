package br.edu.ifsuldeminas.mch.revisaai.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import br.edu.ifsuldeminas.mch.revisaai.R;
import br.edu.ifsuldeminas.mch.revisaai.model.Goal;

public class GoalAdapter extends FirestoreRecyclerAdapter<Goal, GoalAdapter.GoalViewHolder> {

    private final OnGoalListener listener;

    public interface OnGoalListener {
        void onGoalChecked(String goalId, boolean isChecked);
        void onGoalLongClick(String goalId);
    }

    public GoalAdapter(@NonNull FirestoreRecyclerOptions<Goal> options, OnGoalListener listener) {
        super(options);
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull GoalViewHolder holder, int position, @NonNull Goal model) {
        holder.bind(model, listener);
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goal_item, parent, false);
        return new GoalViewHolder(view);
    }

    class GoalViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxCompleted;
        TextView textViewDescription;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxCompleted = itemView.findViewById(R.id.checkBoxCompleted);
            textViewDescription = itemView.findViewById(R.id.textViewGoalDescription);
        }

        public void bind(final Goal goal, final OnGoalListener listener) {
            textViewDescription.setText(goal.getDescription());
            checkBoxCompleted.setChecked(goal.isCompleted());

            // Adiciona ou remove o efeito de texto riscado
            if (goal.isCompleted()) {
                textViewDescription.setPaintFlags(textViewDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                textViewDescription.setPaintFlags(textViewDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            // Listener para o CheckBox
            checkBoxCompleted.setOnClickListener(v -> {
                String goalId = getSnapshots().getSnapshot(getAdapterPosition()).getId();
                listener.onGoalChecked(goalId, checkBoxCompleted.isChecked());
            });

            // Listener para clique longo (para editar/excluir)
            itemView.setOnLongClickListener(v -> {
                String goalId = getSnapshots().getSnapshot(getAdapterPosition()).getId();
                listener.onGoalLongClick(goalId);
                return true;
            });
        }
    }
}
