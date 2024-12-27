package com.example.myjavafxapp.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Member extends Person {
    private int id;
    private Event event; // Связь с событием
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<Dog> dogs = new ArrayList<>(); // Список собак участника

    public Member(int id, String name, int age, Event event, Timestamp createdAt, Timestamp updatedAt) {
        super(name, age);
        this.id = id;
        this.event = event;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Геттеры и сеттеры для списка собак
    public List<Dog> getDogs() {
        return dogs;
    }

    public void setDogs(List<Dog> dogs) {
        this.dogs = dogs;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDogName() {
        return dogs != null && !dogs.isEmpty() ? dogs.get(0).getName() : null;
    }


    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + getName() + '\'' +
                ", age=" + getAge() +
                ", dogs=" + dogs +
                ", event=" + (event != null ? event.getLocation() : "None") +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
