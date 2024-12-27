package com.example.myjavafxapp.models;

import java.util.ArrayList;
import java.util.List;

public class Breed {
    private int id;
    private String name;

    public Breed() {
    }

    public Breed(int id, String name) {
        this.id = id;
        this.name = name;
    }

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

    @Override
    public String toString() {
        return name; // Возвращает только имя породы
    }

}
