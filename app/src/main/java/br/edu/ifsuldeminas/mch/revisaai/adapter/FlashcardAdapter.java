package br.edu.ifsuldeminas.mch.revisaai.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import br.edu.ifsuldeminas.mch.revisaai.R;
import br.edu.ifsuldeminas.mch.revisaai.model.Flashcard;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

    private List<Flashcard> flashcards;
    private final Context context;
    private final OnItemClickListener listener;

    // Interface para o clique
    public interface OnItemClickListener {
        void onItemClick(Flashcard flashcard);
        void onItemLongClick(Flashcard flashcard, View view); // Adicionado para clique longo
    }

    public FlashcardAdapter(Context context, List<Flashcard> flashcards, OnItemClickListener listener) {
        this.context = context;
        this.flashcards = flashcards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.flashcard_item, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcard flashcard = flashcards.get(position);
        holder.bind(flashcard, listener);
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    public void updateFlashcards(List<Flashcard> newFlashcards) {
        this.flashcards = newFlashcards;
        notifyDataSetChanged();
    }

    public static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView cardFront;

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardFront = itemView.findViewById(R.id.textViewCardFront);
        }

        public void bind(final Flashcard flashcard, final OnItemClickListener listener) {
            cardFront.setText(flashcard.getFront());
            itemView.setOnClickListener(v -> listener.onItemClick(flashcard));
            // Adicionando o listener de clique longo
            itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(flashcard, v);
                return true; // Indica que o evento foi consumido
            });
        }
    }
}
