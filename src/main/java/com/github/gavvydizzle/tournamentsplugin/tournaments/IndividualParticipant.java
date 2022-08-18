package com.github.gavvydizzle.tournamentsplugin.tournaments;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class IndividualParticipant {

    private final UUID uuid;
    private final String name;
    private long score;

    public IndividualParticipant(UUID uuid, long score) {
        this.uuid = uuid;
        this.name = Bukkit.getOfflinePlayer(uuid).getName();
        this.score = score;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public OfflinePlayer offlinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
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
