package com.github.gavvydizzle.tournamentsplugin.gui;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.rewards.Reward;
import com.github.gavvydizzle.tournamentsplugin.rewards.TournamentRewards;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Represents the inventory that displays the rewards items for a given tournament
 */
public class RewardInventory implements ClickableGUI {

    private final TournamentRewards tournamentReward;
    private final Inventory inventory;

    public RewardInventory(TournamentRewards tournamentReward) {
        this.tournamentReward = tournamentReward;
        inventory = Bukkit.createInventory(null, tournamentReward.getInventorySize(), tournamentReward.getInventoryName());
        loadInventory();
    }

    private void loadInventory() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, tournamentReward.getFiller());
        }

        for (Reward reward : tournamentReward.getRewards()) {
            if (reward.getInventorySlot() >= inventory.getSize() || reward.getInventorySlot() < 0) {
                TournamentsPlugin.getInstance().getLogger().warning("A reward in the opened inventory is out of bounds! (size=" + inventory.getSize() + ", slot=" + reward.getInventorySlot() + ")");
                continue;
            }
            inventory.setItem(reward.getInventorySlot(), reward.getItemStack());
        }

        try {
            inventory.setItem(tournamentReward.getBackButtonSlot(), TournamentsPlugin.getInstance().getInventoryManager().getBackButtonItem());
        } catch (Exception ignored) {}
    }


    @Override
    public void openInventory(Player player) {
        player.openInventory(inventory);
        TournamentsPlugin.getInstance().getInventoryManager().setClickableGUI(player, this);
    }

    @Override
    public void closeInventory(Player player) {
        TournamentsPlugin.getInstance().getInventoryManager().removePlayerFromGUI(player);
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        if (e.getSlot() == tournamentReward.getBackButtonSlot()) {
            TournamentsPlugin.getInstance().getInventoryManager().getTournamentListByTournament(tournamentReward.getTournament()).openInventory((Player) e.getWhoClicked());
        }
    }
}
