package com.github.gavvydizzle.tournamentsplugin.gui;

import org.bukkit.entity.Player;

import java.util.UUID;

public class InventoryPlayer {

    private final Player player;
    private ClickableGUI currentInventory;

    public InventoryPlayer(Player player, ClickableGUI initialInventory) {
        this.player = player;
        this.currentInventory = initialInventory;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    public ClickableGUI getCurrentInventory() {
        return currentInventory;
    }

    public void setCurrentInventory(ClickableGUI currentInventory) {
        this.currentInventory = currentInventory;
    }
}
