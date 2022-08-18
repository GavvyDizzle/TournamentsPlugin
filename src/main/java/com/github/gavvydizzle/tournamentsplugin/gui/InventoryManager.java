package com.github.gavvydizzle.tournamentsplugin.gui;

import com.github.gavvydizzle.tournamentsplugin.configs.GUIConfig;
import com.github.gavvydizzle.tournamentsplugin.gui.tournamentlists.TournamentListManager;
import com.github.gavvydizzle.tournamentsplugin.tournaments.GangTournament;
import com.github.gavvydizzle.tournamentsplugin.tournaments.IndividualTournament;
import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;
import com.github.mittenmc.serverutils.ColoredItems;
import com.github.mittenmc.serverutils.Colors;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

public class InventoryManager implements Listener {

    private final HashMap<UUID, ClickableGUI> playersInInventory;
    private final TopInventory topInventory;
    private final TournamentListManager tournamentListManager;
    private ItemStack backButtonItem;

    public InventoryManager() {
        playersInInventory = new HashMap<>();
        this.topInventory = new TopInventory();
        this.tournamentListManager = new TournamentListManager();

        reload();
    }

    public void reload() {
        GUIConfig.reload();

        FileConfiguration config = GUIConfig.get();
        config.options().copyDefaults(true);
        config.addDefault("backButton.color", "red");
        config.addDefault("backButton.name", "&cBack");

        backButtonItem = ColoredItems.getGlassByName(config.getString("backButton.color"));
        ItemMeta meta = backButtonItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("backButton.name")));
        backButtonItem.setItemMeta(meta);

        topInventory.reload();

        GUIConfig.save();
    }

    public void openTopInventory(Player player) {
        topInventory.openInventory(player);
        playersInInventory.put(player.getUniqueId(), topInventory);
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent e) {
        if (playersInInventory.containsKey(e.getPlayer().getUniqueId())) {
            removePlayerFromGUI((Player) e.getPlayer());
        }
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;

        if (playersInInventory.containsKey(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getView().getTopInventory()) {
                playersInInventory.get(e.getWhoClicked().getUniqueId()).handleClick(e);
            }
        }
    }

    /**
     * Gets the tournament list inventory based on the tournament's time type
     * @param tournament The tournament to check
     * @return The inventory for this tournament
     */
    public ClickableGUI getTournamentListByTournament(Tournament tournament) {
        if (tournament instanceof IndividualTournament) {
            switch (tournament.getTimeType()) {
                case PENDING:
                    return tournamentListManager.getPendingIndividual();
                case ACTIVE:
                    return tournamentListManager.getActiveIndividual();
                case COMPLETED:
                    return tournamentListManager.getCompletedIndividual();
            }
        }
        else if (tournament instanceof GangTournament) {
            switch (tournament.getTimeType()) {
                case PENDING:
                    return tournamentListManager.getPendingGang();
                case ACTIVE:
                    return tournamentListManager.getActiveGang();
                case COMPLETED:
                    return tournamentListManager.getCompletedGang();
            }
        }
        return topInventory;
    }


    public void setClickableGUI(Player player, ClickableGUI clickableGUI) {
        playersInInventory.put(player.getUniqueId(), clickableGUI);
    }

    public void removePlayerFromGUI(Player player) {
        playersInInventory.remove(player.getUniqueId());
    }

    public TournamentListManager getTournamentListManager() {
        return tournamentListManager;
    }

    public ItemStack getBackButtonItem() {
        return backButtonItem;
    }

}
