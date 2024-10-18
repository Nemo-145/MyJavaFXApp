package com.example.myjavafxapp.models;

public class Dog {
    // fields
    private String breed;
    private String person_name;
    private String judge_name;
    private String winner_event;

    // methods
    public String getBreed() {
        return breed;
    }

    public void setBreed(String newVal) {
        this.breed = newVal;
    }

    public String getPersonName() {
        return person_name;
    }

    public void setPersonName(String newVal) {
        this.person_name = newVal;
    }

    public String getJudgeName() {
        return judge_name;
    }

    public void setJudgeName(String newVal) {
        this.judge_name = newVal;
    }

    public String getWinnerEvent() {
        return winner_event;
    }

    public void setWinnerEvent(String newVal) {
        this.winner_event = newVal;
    }
}
