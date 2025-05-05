package com.example.msway.models;


import java.io.Serializable;

// Classe modello per rappresentare un utente dell’applicazione
public class User implements Serializable {
    private static final long serialVersionUID = 1L; // Per serializzazione sicura

    // Campi principali dell’utente
    private String username;
    private String name;
    private String role;

    public User() {
        // Costruttore vuoto richiesto per serializzazione/deserializzazione
    }

    // Costruttore completo
    public User(String username, String name, String role) {
        this.username = username;
        this.name = name;
        this.role = role;
    }

    // Metodi getter e setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Override per rappresentare l’oggetto in formato leggibile
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
