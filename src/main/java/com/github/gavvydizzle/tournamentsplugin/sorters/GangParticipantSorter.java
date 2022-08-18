package com.github.gavvydizzle.tournamentsplugin.sorters;

import com.github.gavvydizzle.tournamentsplugin.tournaments.GangParticipant;

import java.util.Comparator;

public class GangParticipantSorter implements Comparator<GangParticipant> {

    @Override
    public int compare(GangParticipant o1, GangParticipant o2) {
        return Long.compare(o2.getScore(), o1.getScore());
    }
}