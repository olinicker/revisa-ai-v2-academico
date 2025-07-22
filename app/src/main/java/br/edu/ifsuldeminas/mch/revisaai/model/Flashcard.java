package br.edu.ifsuldeminas.mch.revisaai.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "flashcards",
        foreignKeys = @ForeignKey(entity = Deck.class,
                parentColumns = "id",
                childColumns = "deckId",
                onDelete = ForeignKey.CASCADE))
public class Flashcard implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String front;
    private String back;
    @ColumnInfo(index = true)
    private int deckId;
    private Date lastReviewed;
    private int correctAnswers; // Corrigido para corresponder ao seu código
    private int totalReviews;   // Corrigido para corresponder ao seu código

    public Flashcard() {}

    // Construtor para compatibilidade
    public Flashcard(int id, int deckId, String front, String back) {
        this.id = id;
        this.deckId = deckId;
        this.front = front;
        this.back = back;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }
    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }
    public int getDeckId() { return deckId; }
    public void setDeckId(int deckId) { this.deckId = deckId; }
    public Date getLastReviewed() { return lastReviewed; }
    public void setLastReviewed(Date lastReviewed) { this.lastReviewed = lastReviewed; }
    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
    public int getTotalReviews() { return totalReviews; }
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }
}
