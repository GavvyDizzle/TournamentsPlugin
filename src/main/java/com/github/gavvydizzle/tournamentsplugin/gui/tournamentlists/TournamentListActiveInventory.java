package com.github.gavvydizzle.tournamentsplugin.gui.tournamentlists;

import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;

public class TournamentListActiveInventory extends TournamentListInventory {

    public TournamentListActiveInventory(String inventoryName, int backButtonSlot) {
        super(inventoryName, backButtonSlot);
    }

    @Override
    void generateInventoryItems() {
        tournamentItems.clear();
        for (Tournament tournament : tournaments) {
            tournamentItems.add(setItemStackPlaceholders(tournament, tournament.getActiveItem()));
        }
    }
}
