package com.example.myjavafxapp.models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Event {
    private int id;
    private String location;
    private Timestamp eventTime;
    private int participantCount;
    private int judgeCount;
    private Member winner; // Один победитель
    private BigDecimal prizePool;

    public Event() {
    }

    public Event(int id, String location, Timestamp eventTime, int participantCount, int judgeCount, Member winner, BigDecimal prizePool) {
        this.id = id;
        this.location = location;
        this.eventTime = eventTime;
        this.participantCount = participantCount;
        this.judgeCount = judgeCount;
        this.winner = winner;
        this.prizePool = prizePool;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Timestamp getEventTime() {
        return eventTime;
    }

    public void setEventTime(Timestamp eventTime) {
        this.eventTime = eventTime;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }

    public int getJudgeCount() {
        return judgeCount;
    }

    public void setJudgeCount(int judgeCount) {
        this.judgeCount = judgeCount;
    }

    public Member getWinner() {
        return winner;
    }

    public void setWinner(Member winner) {
        this.winner = winner;
    }

    public BigDecimal getPrizePool() {
        return prizePool;
    }

    public void setPrizePool(BigDecimal prizePool) {
        this.prizePool = prizePool;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", location='" + location + '\'' +
                ", eventTime=" + eventTime +
                ", participantCount=" + participantCount +
                ", judgeCount=" + judgeCount +
                ", winner=" + (winner != null ? winner.getName() : "None") +
                ", prizePool=" + prizePool +
                '}';
    }
}
