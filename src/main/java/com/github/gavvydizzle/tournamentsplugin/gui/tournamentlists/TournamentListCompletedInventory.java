package com.github.gavvydizzle.tournamentsplugin.gui.tournamentlists;

import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;

public class TournamentListCompletedInventory extends TournamentListInventory {

    public TournamentListCompletedInventory(String inventoryName, int backButtonSlot) {
        super(inventoryName, backButtonSlot);
    }

    @Override
    void generateInventoryItems() {
        tournamentItems.clear();
        for (Tournament tournament : tournaments) {
            tournamentItems.add(setItemStackPlaceholders(tournament, tournament.getCompletedItem()));
        }
    }
}
