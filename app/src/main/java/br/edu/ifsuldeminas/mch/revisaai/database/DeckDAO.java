package br.edu.ifsuldeminas.mch.revisaai.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import br.edu.ifsuldeminas.mch.revisaai.model.Deck;

@Dao
public interface DeckDAO {
    @Insert
    void insert(Deck deck);

    @Update
    void update(Deck deck);

    @Delete
    void delete(Deck deck);

    @Query("SELECT * FROM decks")
    List<Deck> getAll();

    @Query("SELECT * FROM decks WHERE id = :deckId")
    Deck getDeckById(int deckId);
}
