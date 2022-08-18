package com.github.gavvydizzle.tournamentsplugin.rewards;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a reward that is given to one or many players at the conclusion of a tournament
 */
public class Reward {

    private final ItemStack itemStack;
    private final int inventorySlot;
    private final ArrayList<Integer> placements;
    private final List<String> commands;

    public Reward(int minPlacement, int maxPlacement, List<String> commands,
                  int slot, Material material, String name, List<String> lore, boolean isGlowing) {

        placements = new ArrayList<>(Math.abs(minPlacement - maxPlacement) + 1);
        for (int i = Math.min(minPlacement, maxPlacement); i <= Math.max(minPlacement, maxPlacement); i++) {
            placements.add(i);
        }

        this.commands = commands;
        this.inventorySlot = slot;

        itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(name);
        meta.setLore(lore);
        if (isGlowing) {
            meta.addEnchant(Enchantment.ARROW_FIRE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemStack.setItemMeta(meta);
    }

    public ArrayList<Integer> getPlacements() {
        return placements;
    }

    public List<String> getCommands() {
        return commands;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getInventorySlot() {
        return inventorySlot;
    }
}
