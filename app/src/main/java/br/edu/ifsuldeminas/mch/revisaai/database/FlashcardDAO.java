package br.edu.ifsuldeminas.mch.revisaai.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import br.edu.ifsuldeminas.mch.revisaai.model.Flashcard;

@Dao
public interface FlashcardDAO {
    @Insert
    void insert(Flashcard flashcard);

    @Update
    void update(Flashcard flashcard);

    @Delete
    void delete(Flashcard flashcard);

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY front ASC")
    List<Flashcard> getFlashcardsByDeck(int deckId);

    @Query("SELECT * FROM flashcards")
    List<Flashcard> getAllFlashcards();

    // Método para contar flashcards, usado pelo DeckAdapter
    @Query("SELECT COUNT(id) FROM flashcards WHERE deckId = :deckId")
    int getFlashcardCountForDeck(int deckId);
}
