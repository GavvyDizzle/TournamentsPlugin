package com.github.gavvydizzle.tournamentsplugin.gui.inventoryitems;

import org.bukkit.inventory.ItemStack;

/**
 * Represents an item that fills a slot in an inventory.
 */
public class ClickableItem {

    private final ItemStack itemStack;
    private final int slot;
    private final boolean isClickable;

    public ClickableItem(ItemStack itemStack, int slot, boolean isClickable) {
        this.itemStack = itemStack;
        this.slot = slot;
        this.isClickable = isClickable;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getSlot() {
        return slot;
    }

    public boolean isClickable() {
        return isClickable;
    }
}
