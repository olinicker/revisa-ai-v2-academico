package br.edu.ifsuldeminas.mch.revisaai.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Goal {
    private String id;
    private String description;
    private boolean completed;
    private String userId;
    @ServerTimestamp
    private Date creationDate;
    private String category; // Novo campo
    private String dueDate;  // Novo campo
    private String priority; // Novo campo

    // Construtor vazio é necessário para o Firestore
    public Goal() {}

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

    // Getters e Setters para os novos campos
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
