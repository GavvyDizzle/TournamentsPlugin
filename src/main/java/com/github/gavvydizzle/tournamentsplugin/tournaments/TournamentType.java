package com.github.gavvydizzle.tournamentsplugin.tournaments;

import com.github.gavvydizzle.tournamentsplugin.objectives.ObjectiveType;

public enum TournamentType {

    INDIVIDUAL,
    GANG;

    public static TournamentType getTournamentType(String str) {
        for (TournamentType tournamentType : TournamentType.values()) {
            if (tournamentType.toString().equalsIgnoreCase(str)) {
                return tournamentType;
            }
        }
        return null;
    }

}
