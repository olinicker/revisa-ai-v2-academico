package br.edu.ifsuldeminas.mch.revisaai.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "decks")
public class Deck implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;

    // Construtor vazio é necessário para o Room
    public Deck() {}

    // Construtor que você já usava
    public Deck(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
