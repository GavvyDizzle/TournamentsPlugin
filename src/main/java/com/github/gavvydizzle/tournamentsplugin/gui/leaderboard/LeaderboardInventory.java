package com.github.gavvydizzle.tournamentsplugin.gui.leaderboard;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.gui.ClickableGUI;
import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;
import com.github.mittenmc.serverutils.ColoredItems;
import com.github.mittenmc.serverutils.Colors;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardInventory implements ClickableGUI {

    protected final Tournament tournament;
    protected final String inventoryName;
    protected final int inventorySize;
    protected final ItemStack filler;
    protected final int numPlacementsShown, personalPlacementItemSlot, backButtonSlot;
    protected final String placementItemName, personalPlacementItemName;
    protected final List<String> placementItemLore, personalPlacementItemLore;

    public LeaderboardInventory(Tournament tournament, FileConfiguration config) {
        this.tournament = tournament;

        config.addDefault("leaderboard.name", "Leaderboard");
        config.addDefault("leaderboard.rows", 6);
        config.addDefault("leaderboard.filler", "black");
        config.addDefault("leaderboard.backButtonSlot", 45);
        config.addDefault("leaderboard.numPlacementsShown", 45);
        config.addDefault("leaderboard.placementItem.name", "&e{player_name}");
        config.addDefault("leaderboard.placementItem.lore", new ArrayList<>());
        config.addDefault("leaderboard.personalPlacementItem.slot", 49);
        config.addDefault("leaderboard.personalPlacementItem.name", "&eYour Placement");
        config.addDefault("leaderboard.personalPlacementItem.lore", new ArrayList<>());

        inventoryName = Colors.conv(config.getString("leaderboard.name"));
        inventorySize = config.getInt("leaderboard.rows") * 9;
        filler = ColoredItems.getGlassByName(config.getString("leaderboard.filler"));
        backButtonSlot = config.getInt("leaderboard.backButtonSlot");

        numPlacementsShown = config.getInt("leaderboard.numPlacementsShown");

        placementItemName = Colors.conv(config.getString("leaderboard.placementItem.name"));
        placementItemLore = Colors.conv(config.getStringList("leaderboard.placementItem.lore"));
        personalPlacementItemSlot = config.getInt("leaderboard.personalPlacementItem.slot");
        personalPlacementItemName = Colors.conv(config.getString("leaderboard.personalPlacementItem.name"));
        personalPlacementItemLore = Colors.conv(config.getStringList("leaderboard.personalPlacementItem.lore"));
    }

    @Override
    public void openInventory(Player player) {}

    @Override
    public void closeInventory(Player player) {
        TournamentsPlugin.getInstance().getInventoryManager().removePlayerFromGUI(player);
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        if (e.getSlot() == backButtonSlot) {
            TournamentsPlugin.getInstance().getInventoryManager().getTournamentListByTournament(tournament).openInventory((Player) e.getWhoClicked());
        }
    }


    public Tournament getTournament() {
        return tournament;
    }

}
