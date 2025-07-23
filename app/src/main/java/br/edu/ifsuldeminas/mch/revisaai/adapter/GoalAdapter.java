package br.edu.ifsuldeminas.mch.revisaai.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import br.edu.ifsuldeminas.mch.revisaai.R;
import br.edu.ifsuldeminas.mch.revisaai.model.Goal;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private List<Goal> goals;
    private final android.content.Context context; // Use o nome completo para evitar conflito com 'Context' do Android
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Goal goal);
        void onItemLongClick(Goal goal, View view);
    }

    public GoalAdapter(android.content.Context context, List<Goal> goals, OnItemClickListener listener) {
        this.context = context;
        this.goals = goals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.goal_item, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goals.get(position);
        holder.bind(goal, listener);
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    public void updateGoals(List<Goal> newGoals) {
        this.goals = newGoals;
        notifyDataSetChanged();
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGoalDescription;
        TextView textViewGoalStatus;
        TextView textViewGoalCategory;
        TextView textViewGoalDueDate;
        TextView textViewGoalPriority;


        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGoalDescription = itemView.findViewById(R.id.textViewGoalDescription);
            textViewGoalStatus = itemView.findViewById(R.id.textViewGoalStatus);
            textViewGoalCategory = itemView.findViewById(R.id.textViewGoalCategory);
            textViewGoalDueDate = itemView.findViewById(R.id.textViewGoalDueDate);
            textViewGoalPriority = itemView.findViewById(R.id.textViewGoalPriority);
        }

        public void bind(final Goal goal, final OnItemClickListener listener) {
            textViewGoalDescription.setText(goal.getDescription());

            // Define o status e o estilo baseado se a meta está completa
            if (goal.isCompleted()) {
                textViewGoalStatus.setText("Status: Concluída");
                // Usar uma cor que contraste bem com o fundo escuro e indique sucesso
                textViewGoalStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.orange_accent));
                textViewGoalDescription.setPaintFlags(textViewGoalDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                textViewGoalStatus.setText("Status: Pendente");
                // Usar uma cor que contraste bem com o fundo escuro e indique pendência
                textViewGoalStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.text_input_hint));
                textViewGoalDescription.setPaintFlags(textViewGoalDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            // Preenche os novos campos
            textViewGoalCategory.setText("Categoria: " + goal.getCategory());
            textViewGoalDueDate.setText("Data de Vencimento: " + goal.getDueDate());
            textViewGoalPriority.setText("Prioridade: " + goal.getPriority());


            itemView.setOnClickListener(v -> listener.onItemClick(goal));
            itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(goal, v);
                return true;
            });
        }
    }
}
