package com.example.myjavafxapp.controllers;

/**
 * Утилитарный класс для хранения breedToOpen,
 * если нужно переходить на страницу Breed по ID породы.
 */
public class Breed_controller_static {
    private static int breedToOpen = 0;

    public static void setBreedToOpen(int breedId) {
        breedToOpen = breedId;
    }

    public static int getBreedToOpen() {
        return breedToOpen;
    }
}
