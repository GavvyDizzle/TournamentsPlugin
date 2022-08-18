package com.github.gavvydizzle.tournamentsplugin.gui.tournamentlists;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.configs.Configuration;
import com.github.gavvydizzle.tournamentsplugin.gui.ClickableGUI;
import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;
import com.github.gavvydizzle.tournamentsplugin.tournaments.TournamentTimeType;
import com.github.mittenmc.serverutils.ColoredItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public abstract class TournamentListInventory implements ClickableGUI {

    private static final int inventorySize;
    private static final int pageDownSlot;
    private static final int pageInfoSlot;
    private static final int pageUpSlot;
    private static final ItemStack pageInfoItem;
    private static final ItemStack previousPageItem;
    private static final ItemStack nextPageItem;
    private static final ItemStack pageRowFiller;

    static {
        inventorySize = 54;
        pageDownSlot = 48;
        pageInfoSlot = 49;
        pageUpSlot = 50;

        pageInfoItem = new ItemStack(Material.PAPER);

        previousPageItem = new ItemStack(Material.PAPER);
        ItemMeta prevPageMeta = previousPageItem.getItemMeta();
        assert prevPageMeta != null;
        prevPageMeta.setDisplayName(ChatColor.YELLOW + "Previous Page");
        previousPageItem.setItemMeta(prevPageMeta);

        nextPageItem = new ItemStack(Material.PAPER);
        ItemMeta nextPageMeta = nextPageItem.getItemMeta();
        assert nextPageMeta != null;
        nextPageMeta.setDisplayName(ChatColor.YELLOW + "Next Page");
        nextPageItem.setItemMeta(nextPageMeta);

        pageRowFiller = ColoredItems.WHITE.getGlass();
    }

    private final String inventoryName;
    private final int backButtonSlot;
    protected final ArrayList<Tournament> tournaments;
    protected final ArrayList<ItemStack> tournamentItems;
    private final HashMap<UUID, Integer> playerPages;

    public TournamentListInventory(String inventoryName, int backButtonSlot) {
        this.inventoryName = inventoryName;
        this.backButtonSlot = backButtonSlot;

        tournaments = new ArrayList<>();
        tournamentItems = new ArrayList<>();
        playerPages = new HashMap<>();
    }

    /**
     * Loads the items for this inventory. This should be called after the Tournaments have bene sorted
     */
    abstract void generateInventoryItems();


    @Override
    public void openInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(player, inventorySize, inventoryName);

        for (int slot = 0; slot < getNumItemsOnPage(1); slot++) {
            inventory.setItem(slot, tournamentItems.get(getIndexByPage(1, slot)));
        }
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, pageRowFiller);
        }
        inventory.setItem(pageDownSlot, previousPageItem);
        inventory.setItem(pageInfoSlot, getPageItem(1));
        inventory.setItem(pageUpSlot, nextPageItem);
        try {
            inventory.setItem(backButtonSlot, TournamentsPlugin.getInstance().getInventoryManager().getBackButtonItem());
        } catch (Exception ignored) {}

        player.openInventory(inventory);
        TournamentsPlugin.getInstance().getInventoryManager().setClickableGUI(player, this);
        playerPages.put(player.getUniqueId(), 1);
    }

    @Override
    public void closeInventory(Player player) {
        TournamentsPlugin.getInstance().getInventoryManager().removePlayerFromGUI(player);
        playerPages.remove(player.getUniqueId());
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        if (e.getSlot() == pageUpSlot) {
            if (playerPages.get(e.getWhoClicked().getUniqueId()) < getMaxPage()) {
                playerPages.put(e.getWhoClicked().getUniqueId(), playerPages.get(e.getWhoClicked().getUniqueId()) + 1);
                updatePage((Player) e.getWhoClicked());
            }
        }
        else if (e.getSlot() == pageDownSlot) {
            if (playerPages.get(e.getWhoClicked().getUniqueId()) > 1) {
                playerPages.put(e.getWhoClicked().getUniqueId(), playerPages.get(e.getWhoClicked().getUniqueId()) - 1);
                updatePage((Player) e.getWhoClicked());
            }
        }
        else if (e.getSlot() == backButtonSlot) {
            TournamentsPlugin.getInstance().getInventoryManager().openTopInventory((Player) e.getWhoClicked());
            playerPages.remove(e.getWhoClicked().getUniqueId());
        }
        else {
            Tournament tournament;
            try {
                tournament = tournaments.get(getIndexByPage(playerPages.get(e.getWhoClicked().getUniqueId()), e.getSlot()));
            } catch (Exception ignored) {
                return;
            }

            if (tournament == null) return;

            if (e.isLeftClick()) {
                // Stop this because there is no leaderboard for pending tournaments
                if (tournament.getTimeType() == TournamentTimeType.PENDING) return;

                tournament.openLeaderboard((Player) e.getWhoClicked());
                playerPages.remove(e.getWhoClicked().getUniqueId());
            }
            else if (e.isRightClick()) {
                switch (tournament.getTimeType()) {
                    case PENDING:
                        if (!tournament.getTournamentRewards().isOpenableWhenPending()) return;
                        break;
                    case ACTIVE:
                        if (!tournament.getTournamentRewards().isOpenableWhenActive()) return;
                        break;
                    case COMPLETED:
                        if (!tournament.getTournamentRewards().isOpenableWhenComplete()) return;
                        break;
                }

                tournament.openRewardInventory((Player) e.getWhoClicked());
                playerPages.remove(e.getWhoClicked().getUniqueId());
            }
        }
    }

    private void updatePage(Player player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        int page = playerPages.get(player.getUniqueId());

        for (int i = 0; i < 45; i++) {
            inventory.clear(i);
        }

        for (int i = 0; i < getNumItemsOnPage(page); i++) {
            inventory.setItem(i, tournamentItems.get(getNumItemsOnPage(page)));
        }

        inventory.setItem(pageInfoSlot, getPageItem(page));
    }

    private ItemStack getPageItem(int page) {
        ItemStack pageInfo = pageInfoItem.clone();
        ItemMeta meta = pageInfo.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.YELLOW + "Page " + page + "/" + getMaxPage());
        pageInfo.setItemMeta(meta);
        return pageInfo;
    }

    protected ItemStack setItemStackPlaceholders(Tournament tournament, ItemStack item) {
        ItemStack itemStack = item.clone();
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(meta.getDisplayName()
                .replace("{tournament_name}", tournament.getDisplayName())
                .replace("{start_date}", Configuration.dateFormat.format(tournament.getStartDate()))
                .replace("{end_date}", Configuration.dateFormat.format(tournament.getEndDate()))
                .replace("{pretty_start_date}", Configuration.prettyDateFormat.format(tournament.getStartDate()))
                .replace("{pretty_end_date}", Configuration.prettyDateFormat.format(tournament.getEndDate()))
                .replace("{time_until_start}", tournament.getTimeRemainingUntilStart())
                .replace("{time_until_end}", tournament.getTimeRemainingUntilEnd())
        );

        if (meta.hasLore()) {
            ArrayList<String> lore = new ArrayList<>();
            for (String str : Objects.requireNonNull(meta.getLore())) {
                lore.add(str
                        .replace("{tournament_name}", tournament.getDisplayName())
                        .replace("{start_date}", Configuration.dateFormat.format(tournament.getStartDate()))
                        .replace("{end_date}", Configuration.dateFormat.format(tournament.getEndDate()))
                        .replace("{pretty_start_date}", Configuration.prettyDateFormat.format(tournament.getStartDate()))
                        .replace("{pretty_end_date}", Configuration.prettyDateFormat.format(tournament.getEndDate()))
                        .replace("{time_until_start}", tournament.getTimeRemainingUntilStart())
                        .replace("{time_until_end}", tournament.getTimeRemainingUntilEnd())
                );
            }
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    private int getMaxPage() {
        return (tournamentItems.size() - 1) / 45 + 1;
    }

    private int getNumItemsOnPage(int page) {
        return Math.min(45, tournamentItems.size() - (page - 1) * 45);
    }

    private int getIndexByPage(int page, int slot) {
        return (page - 1) * 45 + slot;
    }


    public ArrayList<Tournament> getTournaments() {
        return tournaments;
    }

}
