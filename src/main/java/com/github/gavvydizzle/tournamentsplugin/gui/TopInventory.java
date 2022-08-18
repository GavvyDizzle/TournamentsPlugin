package com.github.gavvydizzle.tournamentsplugin.gui;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.configs.GUIConfig;
import com.github.gavvydizzle.tournamentsplugin.gui.inventoryitems.ClickableItem;
import com.github.mittenmc.serverutils.ColoredItems;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class TopInventory implements ClickableGUI {

    private ClickableItem pendingIndividual, activeIndividual, completedIndividual;
    private ClickableItem pendingGang, activeGang, completedGang;
    private Inventory inventory;

    public void reload() {
        FileConfiguration config = GUIConfig.get();
        config.options().copyDefaults(true);

        config.addDefault("topInventory.name", true);
        config.addDefault("topInventory.rows", 3);
        config.addDefault("topInventory.filler", "black");

        config.addDefault("topInventory.items.pendingIndividual.isOpenable", true);
        config.addDefault("topInventory.items.pendingIndividual.slot", 10);
        config.addDefault("topInventory.items.pendingIndividual.material", "CLOCK");
        config.addDefault("topInventory.items.pendingIndividual.name", "&ePending Individual Tournaments");
        config.addDefault("topInventory.items.pendingIndividual.lore", new ArrayList<>());
        config.addDefault("topInventory.items.pendingIndividual.isGlowing", false);

        config.addDefault("topInventory.items.activeIndividual.isOpenable", true);
        config.addDefault("topInventory.items.activeIndividual.slot", 11);
        config.addDefault("topInventory.items.activeIndividual.material", "REDSTONE");
        config.addDefault("topInventory.items.activeIndividual.name", "&eActive Individual Tournaments");
        config.addDefault("topInventory.items.activeIndividual.lore", new ArrayList<>());
        config.addDefault("topInventory.items.activeIndividual.isGlowing", false);

        config.addDefault("topInventory.items.completedIndividual.isOpenable", true);
        config.addDefault("topInventory.items.completedIndividual.slot", 12);
        config.addDefault("topInventory.items.completedIndividual.material", "PAPER");
        config.addDefault("topInventory.items.completedIndividual.name", "&eCompleted Individual Tournaments");
        config.addDefault("topInventory.items.completedIndividual.lore", new ArrayList<>());
        config.addDefault("topInventory.items.completedIndividual.isGlowing", false);

        config.addDefault("topInventory.items.pendingGang.isOpenable", true);
        config.addDefault("topInventory.items.pendingGang.slot", 14);
        config.addDefault("topInventory.items.pendingGang.material", "CLOCK");
        config.addDefault("topInventory.items.pendingGang.name", "&ePending Gang Tournaments");
        config.addDefault("topInventory.items.pendingGang.lore", new ArrayList<>());
        config.addDefault("topInventory.items.pendingGang.isGlowing", false);

        config.addDefault("topInventory.items.activeGang.isOpenable", true);
        config.addDefault("topInventory.items.activeGang.slot", 15);
        config.addDefault("topInventory.items.activeGang.material", "REDSTONE");
        config.addDefault("topInventory.items.activeGang.name", "&eActive Gang Tournaments");
        config.addDefault("topInventory.items.activeGang.lore", new ArrayList<>());
        config.addDefault("topInventory.items.activeGang.isGlowing", false);

        config.addDefault("topInventory.items.completedGang.isOpenable", true);
        config.addDefault("topInventory.items.completedGang.slot", 16);
        config.addDefault("topInventory.items.completedGang.material", "PAPER");
        config.addDefault("topInventory.items.completedGang.name", "&eCompleted Gang Tournaments");
        config.addDefault("topInventory.items.completedGang.lore", new ArrayList<>());
        config.addDefault("topInventory.items.completedGang.isGlowing", false);

        GUIConfig.save();

        pendingIndividual = getItem(config, "topInventory.items.pendingIndividual");
        activeIndividual = getItem(config, "topInventory.items.activeIndividual");
        completedIndividual = getItem(config, "topInventory.items.completedIndividual");
        pendingGang = getItem(config, "topInventory.items.pendingGang");
        activeGang = getItem(config, "topInventory.items.activeGang");
        completedGang = getItem(config, "topInventory.items.completedGang");

        config.addDefault("topInventory.name", true);
        config.addDefault("topInventory.rows", 3);
        config.addDefault("topInventory.filler", "black");

        String inventoryName = Colors.conv(config.getString("topInventory.name"));
        int inventorySize = config.getInt("topInventory.rows") * 9;
        ItemStack filler = ColoredItems.getGlassByName(config.getString("topInventory.filler"));

        inventory = Bukkit.createInventory(null, inventorySize, inventoryName);
        for (int i = 0; i < inventorySize; i++) {
            inventory.setItem(i, filler);
        }

        addItem(pendingIndividual, "pendingIndividual");
        addItem(activeIndividual, "activeIndividual");
        addItem(completedIndividual, "completedIndividual");
        addItem(pendingGang, "pendingGang");
        addItem(activeGang, "activeGang");
        addItem(completedGang, "completedGang");
    }

    private ClickableItem getItem(FileConfiguration config, String path) {
        ItemStack itemStack = new ItemStack(ConfigUtils.getMaterial(config.getString(path + ".material")));
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString(path + ".name")));
        meta.setLore(Colors.conv(config.getStringList(path + ".lore")));
        if (config.getBoolean(path + ".isGlowing")) {
            meta.addEnchant(Enchantment.ARROW_FIRE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemStack.setItemMeta(meta);

        return new ClickableItem(itemStack, config.getInt(path + ".slot"), config.getBoolean(path + ".isOpenable"));
    }

    private void addItem(ClickableItem clickableItem, String name) {
        if (clickableItem.isClickable()) {
            if (clickableItem.getSlot() < 0 || clickableItem.getSlot() >= inventory.getSize()) {
                TournamentsPlugin.getInstance().getLogger().warning("The slot for the " + name + " item is out of bounds in gui.yml!");
            }
            else {
                inventory.setItem(clickableItem.getSlot(), clickableItem.getItemStack());
            }
        }
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
        if (e.getSlot() == pendingIndividual.getSlot() && pendingIndividual.isClickable()) {
            TournamentsPlugin.getInstance().getInventoryManager().getTournamentListManager().getPendingIndividual().openInventory((Player) e.getWhoClicked());
        }
        else if (e.getSlot() == activeIndividual.getSlot() && activeIndividual.isClickable()) {
            TournamentsPlugin.getInstance().getInventoryManager().getTournamentListManager().getActiveIndividual().openInventory((Player) e.getWhoClicked());
        }
        else if (e.getSlot() == completedIndividual.getSlot() && completedIndividual.isClickable()) {
            TournamentsPlugin.getInstance().getInventoryManager().getTournamentListManager().getCompletedIndividual().openInventory((Player) e.getWhoClicked());
        }
        else if (e.getSlot() == pendingGang.getSlot() && pendingGang.isClickable()) {
            TournamentsPlugin.getInstance().getInventoryManager().getTournamentListManager().getPendingGang().openInventory((Player) e.getWhoClicked());
        }
        else if (e.getSlot() == activeGang.getSlot() && activeGang.isClickable()) {
            TournamentsPlugin.getInstance().getInventoryManager().getTournamentListManager().getActiveGang().openInventory((Player) e.getWhoClicked());
        }
        else if (e.getSlot() == completedGang.getSlot() && completedGang.isClickable()) {
            TournamentsPlugin.getInstance().getInventoryManager().getTournamentListManager().getCompletedGang().openInventory((Player) e.getWhoClicked());
        }
    }
}
