package com.github.gavvydizzle.tournamentsplugin.sorters;

import com.github.gavvydizzle.tournamentsplugin.tournaments.IndividualParticipant;

import java.util.Comparator;

public class IndividualParticipantSorter implements Comparator<IndividualParticipant> {

    @Override
    public int compare(IndividualParticipant o1, IndividualParticipant o2) {
        return Long.compare(o2.getScore(), o1.getScore());
    }
}
