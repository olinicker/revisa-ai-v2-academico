package br.edu.ifsuldeminas.mch.revisaai.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import br.edu.ifsuldeminas.mch.revisaai.model.Deck;
import br.edu.ifsuldeminas.mch.revisaai.model.Flashcard;

@Database(entities = {Deck.class, Flashcard.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract DeckDAO deckDao();
    public abstract FlashcardDAO flashcardDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "revisa_ai_database")
                            // Permite consultas na thread principal (NÃO RECOMENDADO PARA PRODUÇÃO)
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
