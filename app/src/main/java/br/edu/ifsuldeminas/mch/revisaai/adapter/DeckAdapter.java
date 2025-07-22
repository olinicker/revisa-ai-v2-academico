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
import br.edu.ifsuldeminas.mch.revisaai.database.AppDatabase;
import br.edu.ifsuldeminas.mch.revisaai.database.FlashcardDAO;
import br.edu.ifsuldeminas.mch.revisaai.model.Deck;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {

    private List<Deck> decks;
    private final Context context;
    private final OnItemClickListener listener;
    private final FlashcardDAO flashcardDao;

    public interface OnItemClickListener {
        void onItemClick(Deck deck);
        void onItemLongClick(Deck deck, View view);
    }

    public DeckAdapter(Context context, List<Deck> decks, OnItemClickListener listener) {
        this.context = context;
        this.decks = decks;
        this.listener = listener;
        // Obtém a instância do DAO do Room
        this.flashcardDao = AppDatabase.getDatabase(context).flashcardDao();
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.deck_item, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        Deck deck = decks.get(position);
        // Usa o novo método do Room para contar os flashcards
        int cardCount = flashcardDao.getFlashcardCountForDeck(deck.getId());
        holder.bind(deck, cardCount, listener);
    }

    @Override
    public int getItemCount() {
        return decks.size();
    }

    public void updateDecks(List<Deck> newDecks) {
        this.decks = newDecks;
        notifyDataSetChanged();
    }

    public static class DeckViewHolder extends RecyclerView.ViewHolder {
        TextView deckName;
        TextView cardCount;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            deckName = itemView.findViewById(R.id.textViewDeckName);
            cardCount = itemView.findViewById(R.id.textViewCardCount);
        }

        public void bind(final Deck deck, int count, final OnItemClickListener listener) {
            deckName.setText(deck.getName());
            cardCount.setText(count + " flashcards");
            itemView.setOnClickListener(v -> listener.onItemClick(deck));
            itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(deck, v);
                return true;
            });
        }
    }
}
