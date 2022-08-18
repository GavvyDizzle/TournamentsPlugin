package com.github.gavvydizzle.tournamentsplugin.gui.tournamentlists;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.configs.GUIConfig;
import com.github.gavvydizzle.tournamentsplugin.sorters.TournamentStartDateSorter;
import com.github.gavvydizzle.tournamentsplugin.tournaments.GangTournament;
import com.github.gavvydizzle.tournamentsplugin.tournaments.IndividualTournament;
import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;
import com.github.mittenmc.serverutils.Colors;
import org.bukkit.configuration.file.FileConfiguration;

public class TournamentListManager {

    private TournamentListInventory pendingIndividual, activeIndividual, completedIndividual;
    private TournamentListInventory pendingGang, activeGang, completedGang;

    /**
     * Reloads all tournament lists.
     * This method must be called after all tournaments have been reloaded.
     */
    public void reloadLists() {
        FileConfiguration config = GUIConfig.get();

        config.addDefault("tournamentLists.inventoryNames.pendingIndividual", "Pending Individual Tournaments");
        config.addDefault("tournamentLists.inventoryNames.activeIndividual", "Active Individual Tournaments");
        config.addDefault("tournamentLists.inventoryNames.completedIndividual", "Completed Individual Tournaments");
        config.addDefault("tournamentLists.inventoryNames.pendingGang", "Pending Gang Tournaments");
        config.addDefault("tournamentLists.inventoryNames.activeGang", "Active Gang Tournaments");
        config.addDefault("tournamentLists.inventoryNames.completedGang", "Completed Gang Tournaments");

        config.addDefault("tournamentLists.backButtonSlot", 45);

        int backButtonSlot = config.getInt("tournamentLists.backButtonSlot");

        pendingIndividual = new TournamentListPendingInventory(Colors.conv(config.getString("tournamentLists.inventoryNames.pendingIndividual")), backButtonSlot);
        activeIndividual = new TournamentListActiveInventory(Colors.conv(config.getString("tournamentLists.inventoryNames.activeIndividual")), backButtonSlot);
        completedIndividual = new TournamentListCompletedInventory(Colors.conv(config.getString("tournamentLists.inventoryNames.completedIndividual")), backButtonSlot);
        pendingGang = new TournamentListPendingInventory(Colors.conv(config.getString("tournamentLists.inventoryNames.pendingGang")), backButtonSlot);
        activeGang = new TournamentListActiveInventory(Colors.conv(config.getString("tournamentLists.inventoryNames.activeGang")), backButtonSlot);
        completedGang = new TournamentListCompletedInventory(Colors.conv(config.getString("tournamentLists.inventoryNames.completedGang")), backButtonSlot);

        updateLists();
    }

    public void updateLists() {
        pendingIndividual.getTournaments().clear();
        activeIndividual.getTournaments().clear();
        completedIndividual.getTournaments().clear();
        pendingGang.getTournaments().clear();
        activeGang.getTournaments().clear();
        completedGang.getTournaments().clear();

        for (Tournament tournament : TournamentsPlugin.getInstance().getTournamentManager().getTournaments()) {
            if (tournament instanceof IndividualTournament) {
                switch (tournament.getTimeType()) {
                    case PENDING:
                        pendingIndividual.getTournaments().add(tournament);
                        break;
                    case ACTIVE:
                        activeIndividual.getTournaments().add(tournament);
                        break;
                    case COMPLETED:
                        completedIndividual.getTournaments().add(tournament);
                        break;
                }
            }
            else if (tournament instanceof GangTournament) {
                switch (tournament.getTimeType()) {
                    case PENDING:
                        pendingGang.getTournaments().add(tournament);
                        break;
                    case ACTIVE:
                        activeGang.getTournaments().add(tournament);
                        break;
                    case COMPLETED:
                        completedGang.getTournaments().add(tournament);
                        break;
                }
            }
        }

        pendingIndividual.getTournaments().sort(new TournamentStartDateSorter());
        activeIndividual.getTournaments().sort(new TournamentStartDateSorter());
        completedIndividual.getTournaments().sort(new TournamentStartDateSorter());
        pendingGang.getTournaments().sort(new TournamentStartDateSorter());
        activeGang.getTournaments().sort(new TournamentStartDateSorter());
        completedGang.getTournaments().sort(new TournamentStartDateSorter());

        pendingIndividual.generateInventoryItems();
        activeIndividual.generateInventoryItems();
        completedIndividual.generateInventoryItems();
        pendingGang.generateInventoryItems();
        activeGang.generateInventoryItems();
        completedGang.generateInventoryItems();
    }

    public TournamentListInventory getPendingIndividual() {
        return pendingIndividual;
    }

    public TournamentListInventory getActiveIndividual() {
        return activeIndividual;
    }

    public TournamentListInventory getCompletedIndividual() {
        return completedIndividual;
    }

    public TournamentListInventory getPendingGang() {
        return pendingGang;
    }

    public TournamentListInventory getActiveGang() {
        return activeGang;
    }

    public TournamentListInventory getCompletedGang() {
        return completedGang;
    }
}
