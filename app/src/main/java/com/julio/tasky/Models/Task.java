package com.julio.tasky.Models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Task {
    private String id; // <-- Añadir un campo para el ID del documento de Firestore
    private String userId;
    private String title;
    private String description;
    private boolean isCompleted;
    private Date createdAt;

    // Constructor sin argumentos necesario para Firestore
    public Task() {
    }

    // Constructor con argumentos (opcional)
    public Task(String userId, String title, String description, boolean isCompleted) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
    }

    // Getters y Setters
    // ... (los mismos getters y setters de antes) ...

    // Getter y Setter para el ID
    // Firestore no mapea automáticamente el ID del documento a un campo
    // Tendrás que establecerlo manualmente después de obtener los datos.
    // Por eso, este getter/setter es útil para tu adaptador.
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ServerTimestamp
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
