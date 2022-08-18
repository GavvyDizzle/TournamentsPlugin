package com.github.gavvydizzle.tournamentsplugin.tournaments;

import com.github.mittenmc.gangsplugin.gangs.Gang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GangParticipant {

    private final int id;
    private final String gangName;
    private long score;
    private final Gang gang;

    /**
     * Creates a new GangParticipant for when the tournament is active
     * @param gang The gang
     * @param score The score
     */
    public GangParticipant(@NotNull Gang gang, long score) {
        this.gang = gang;
        this.id = gang.getId();
        this.gangName = gang.getName();
        this.score = score;
    }

    /**
     * Creates a new GangParticipant for when the tournament is completed
     * @param id The gang's id
     * @param gangName The gang's name
     * @param score The score
     */
    public GangParticipant(int id, String gangName, long score) {
        this.id = id;
        this.gangName = gangName;
        this.score = score;
        this.gang = null;
    }

    public int getId() {
        return id;
    }

    public String getGangName() {
        return gangName;
    }

    @Nullable
    public Gang getGang() {
        return gang;
    }

    public long getScore() {
        return score;
    }

    public void incrementScore(long amount) {
        score += amount;
    }

    public void setScore(long newScore) {
        score = newScore;
    }

}
