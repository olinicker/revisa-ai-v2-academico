package br.edu.ifsuldeminas.mch.revisaai.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "revisaai.db";
    private static final int DATABASE_VERSION = 1;

    // Tabela Decks
    public static final String TABLE_DECKS = "decks";
    public static final String COLUMN_DECK_ID = "id";
    public static final String COLUMN_DECK_NAME = "name";

    // Tabela Flashcards
    public static final String TABLE_FLASHCARDS = "flashcards";
    public static final String COLUMN_CARD_ID = "id";
    public static final String COLUMN_CARD_DECK_ID = "deck_id";
    public static final String COLUMN_CARD_FRONT = "front";
    public static final String COLUMN_CARD_BACK = "back";
    public static final String COLUMN_CARD_LAST_REVIEW = "last_review";
    public static final String COLUMN_CARD_CORRECT = "correct_answers";
    public static final String COLUMN_CARD_TOTAL = "total_reviews";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createDeckTable = "CREATE TABLE " + TABLE_DECKS + " (" +
                COLUMN_DECK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DECK_NAME + " TEXT NOT NULL)";

        String createFlashcardTable = "CREATE TABLE " + TABLE_FLASHCARDS + " (" +
                COLUMN_CARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CARD_DECK_ID + " INTEGER NOT NULL, " +
                COLUMN_CARD_FRONT + " TEXT NOT NULL, " +
                COLUMN_CARD_BACK + " TEXT NOT NULL, " +
                COLUMN_CARD_LAST_REVIEW + " TEXT, " +
                COLUMN_CARD_CORRECT + " INTEGER DEFAULT 0, " +
                COLUMN_CARD_TOTAL + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + COLUMN_CARD_DECK_ID + ") REFERENCES " + TABLE_DECKS + "(" + COLUMN_DECK_ID + ") ON DELETE CASCADE)";

        db.execSQL(createDeckTable);
        db.execSQL(createFlashcardTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FLASHCARDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DECKS);
        onCreate(db);
    }
}