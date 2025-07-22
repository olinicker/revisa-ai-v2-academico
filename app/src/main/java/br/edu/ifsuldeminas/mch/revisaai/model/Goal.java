package br.edu.ifsuldeminas.mch.revisaai.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Goal {
    // Exclude para que o Firestore não tente guardar este campo,
    // pois ele já é o ID do documento.
    @Exclude
    private String id;

    private String description;
    private boolean completed;
    private String userId;

    // Anotação para que o Firestore preencha este campo automaticamente
    // com a data do servidor no momento da criação.
    @ServerTimestamp
    private Date creationDate;

    // Construtor vazio é obrigatório para o Firestore
    public Goal() {}

    // Getters e Setters
    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
