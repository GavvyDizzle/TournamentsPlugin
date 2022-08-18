package com.github.gavvydizzle.tournamentsplugin.sorters;

import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;

import java.util.Comparator;

public class TournamentStartDateSorter implements Comparator<Tournament> {

    @Override
    public int compare(Tournament o1, Tournament o2) {
        return Long.compare(o2.getStartDate().getTime(), o1.getStartDate().getTime());
    }
}
