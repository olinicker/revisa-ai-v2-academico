package br.edu.ifsuldeminas.mch.revisaai.api;

import com.google.gson.annotations.SerializedName;

public class QuoteResponse {

    // O campo 'id' do JSON será mapeado para esta variável
    @SerializedName("id")
    private int id; // Adicionado o campo ID

    // O campo 'quote' do JSON será mapeado para esta variável
    @SerializedName("quote")
    private String quote;

    // O campo 'author' do JSON será mapeado para esta variável
    @SerializedName("author")
    private String author;

    // Construtor vazio é necessário para o GSON
    public QuoteResponse() {
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getQuote() {
        return quote;
    }

    public String getAuthor() {
        return author;
    }

    // Setters (opcionais, mas boa prática se for usar para enviar dados também)
    public void setId(int id) {
        this.id = id;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
