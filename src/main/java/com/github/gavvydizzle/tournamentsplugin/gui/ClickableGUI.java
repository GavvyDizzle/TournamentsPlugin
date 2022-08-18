package com.github.gavvydizzle.tournamentsplugin.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public interface ClickableGUI {

    /**
     * Handles when the player opens this inventory
     * @param player The player
     */
    void openInventory(Player player);

    /**
     * Handles when the player closes this inventory
     * @param player The player
     */
    void closeInventory(Player player);

    /**
     * Handles when the player clicks this inventory
     * @param e The original click event
     */
    void handleClick(InventoryClickEvent e);

}
