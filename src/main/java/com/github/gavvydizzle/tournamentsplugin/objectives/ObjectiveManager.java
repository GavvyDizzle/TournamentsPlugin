package com.github.gavvydizzle.tournamentsplugin.objectives;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.configs.ObjectivesConfig;
import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;
import com.github.gavvydizzle.tournamentsplugin.tournaments.TournamentManager;
import me.wax.prisonenchants.events.EnchantBreakBlockEvent;
import net.brcdev.shopgui.event.ShopPreTransactionEvent;
import net.brcdev.shopgui.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ObjectiveManager implements Listener {

    private boolean isDebugging;
    private final HashSet<UUID> disabledWorlds;
    private final HashMap<ObjectiveType, HashSet<UUID>> disabledWorldsByType;
    private final TournamentManager tournamentManager;

    public ObjectiveManager(TournamentManager tournamentManager) {
        this.tournamentManager = tournamentManager;
        disabledWorlds = new HashSet<>();
        disabledWorldsByType = new HashMap<>();
        reload();
    }

    public void reload() {
        FileConfiguration config = ObjectivesConfig.get();
        config.options().copyDefaults(true);
        config.addDefault("debug", false);
        config.addDefault("disabledWorlds", new ArrayList<>());
        for (ObjectiveType objectiveType : ObjectiveType.values()) {
            config.addDefault("disabledWorldsByType." + objectiveType, new ArrayList<>());
        }

        isDebugging = config.getBoolean("debug");

        disabledWorlds.clear();
        for (String str : config.getStringList("disabledWorlds")) {
            if (Bukkit.getWorld(str) != null) {
                disabledWorlds.add(Objects.requireNonNull(Bukkit.getWorld(str)).getUID());
            }
            else {
                TournamentsPlugin.getInstance().getLogger().warning("The world '" + str + "' does not exist");
            }
        }

        disabledWorldsByType.clear();
        for (String key : config.getConfigurationSection("disabledWorldsByType").getKeys(false)) {
            String path = "disabledWorldsByType." + key;

            ObjectiveType objectiveType;
            try {
                objectiveType = ObjectiveType.valueOf(key.toUpperCase());
            } catch (Exception e) {
                TournamentsPlugin.getInstance().getLogger().warning("The ObjectiveType '" + key.toUpperCase() + "' does not exist. Skipping it");
                continue;
            }

            HashSet<UUID> worlds = new HashSet<>();
            for (String str : config.getStringList(path)) {
                if (Bukkit.getWorld(str) == null) {
                    TournamentsPlugin.getInstance().getLogger().warning("The world '" + str + "' does not exist. It could not be disabled for " + objectiveType.name() + " objectives.");
                    continue;
                }
                worlds.add(Objects.requireNonNull(Bukkit.getWorld(str)).getUID());
            }
            disabledWorldsByType.put(objectiveType, worlds);
        }

        //Check to make sure all questTypes were added. If not, make them match with an empty list
        for (ObjectiveType objectiveType : ObjectiveType.values()) {
            if (!disabledWorldsByType.containsKey(objectiveType)) {
                disabledWorldsByType.put(objectiveType, new HashSet<>());
                TournamentsPlugin.getInstance().getLogger().warning("[Tournaments] Since the ObjectiveType '" + objectiveType.name() + "' was skipped, it will not have any disabled worlds.");
            }
        }

        ObjectivesConfig.save();
    }

    /**
     * Checks the provided objective against any active tournaments.
     * If a tournament has a matching objective, then progress will be updated for the player and/or player's gang.
     *
     * @param player     The player to increment. This will also add to their gang's total if possible
     * @param objective  The objective to check for
     * @param amount     The amount to increment by
     */
    public synchronized void incrementObjective(@NotNull Player player, @NotNull Objective objective, long amount) {
        boolean debug = isDebugging && player.hasPermission("tournamentsadmin.debug");
        if (debug) {
            player.sendMessage(ChatColor.YELLOW +  String.valueOf(ChatColor.BOLD) + "[Tourn Debug] " + ChatColor.WHITE + "Progressing: " + objective);
        }

        for (Tournament tournament : tournamentManager.getActiveTournaments()) {
            if (objective.isMatch(tournament.getObjective())) {
                tournament.incrementProgress(player, amount);
                if (debug) {
                    player.sendMessage(ChatColor.YELLOW + String.valueOf(ChatColor.BOLD) + "- [Tourn Debug] " + ChatColor.GREEN + "Progressed " + tournament.getId() + " by " + amount);
                }
            }
        }
    }


    //*** EVENT LISTENERS ***//

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBreakBlock(BlockBreakEvent e) {
        if (disabledWorlds.contains(e.getPlayer().getWorld().getUID())) return;
        if (disabledWorldsByType.get(ObjectiveType.MINE).contains(e.getPlayer().getWorld().getUID())) return;

        // Specific check to stop it from counting player placed blocks
        if (e.getBlock().hasMetadata("player_placed")) return;

        incrementObjective(e.getPlayer(), new Objective(ObjectiveType.MINE_RAW), 1);
        incrementObjective(e.getPlayer(), new MaterialObjective(ObjectiveType.MINE_RAW, e.getBlock().getType()), 1);
        incrementObjective(e.getPlayer(), new Objective(ObjectiveType.MINE), 1);
        incrementObjective(e.getPlayer(), new MaterialObjective(ObjectiveType.MINE, e.getBlock().getType()), 1);
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEnchantBreakBlock(EnchantBreakBlockEvent e) {
        if (disabledWorlds.contains(e.getPlayer().getWorld().getUID())) return;
        if (disabledWorldsByType.get(ObjectiveType.MINE).contains(e.getPlayer().getWorld().getUID())) return;

        // Specific check to stop it from counting player placed blocks
        if (e.getBlock().hasMetadata("player_placed")) return;

        incrementObjective(e.getPlayer(), new Objective(ObjectiveType.MINE), 1);
        incrementObjective(e.getPlayer(), new MaterialObjective(ObjectiveType.MINE, e.getBlock().getType()), 1);
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerCraft(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() != InventoryType.CRAFTING && e.getInventory().getType() != InventoryType.WORKBENCH) return;

        if (disabledWorlds.contains(e.getWhoClicked().getWorld().getUID())) return;
        if (disabledWorldsByType.get(ObjectiveType.CRAFT).contains(e.getWhoClicked().getWorld().getUID())) return;

        if (e.getSlot() != 0 || e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)) return;

        int itemSlot = -1;
        int preCraftAmount = -1;
        for (int i = 1; i <= 9; i++) {
            if (e.getClickedInventory().getItem(i) != null && e.getClickedInventory().getItem(i).getType() != Material.AIR) {
                itemSlot = i;
                preCraftAmount = e.getClickedInventory().getItem(i).getAmount();
                break;
            }
        }
        if (itemSlot == -1) {
            return;
        }

        ItemStack resultItem = e.getClickedInventory().getItem(0).clone();
        int amountPerResult = resultItem.getAmount();
        int finalItemSlot = itemSlot;
        int finalPreCraftAmount = preCraftAmount;

        Bukkit.getScheduler().scheduleSyncDelayedTask(TournamentsPlugin.getInstance(), () -> {
            int postCraftAmount = e.getClickedInventory().getItem(finalItemSlot) == null ||
                    e.getClickedInventory().getItem(finalItemSlot).getType() == Material.AIR ?
                    0 : e.getClickedInventory().getItem(finalItemSlot).getAmount();

            if (finalPreCraftAmount - postCraftAmount != 0) {
                long total = (long) (finalPreCraftAmount - postCraftAmount) * amountPerResult;
                incrementObjective((Player) e.getWhoClicked(), new Objective(ObjectiveType.CRAFT), total);
                incrementObjective((Player) e.getWhoClicked(), new MaterialObjective(ObjectiveType.CRAFT, resultItem.getType()), total);
            }
        }, 0);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerEat(PlayerItemConsumeEvent e) {
        if (disabledWorlds.contains(e.getPlayer().getWorld().getUID())) return;
        if (disabledWorldsByType.get(ObjectiveType.EAT).contains(e.getPlayer().getWorld().getUID())) return;

        incrementObjective(e.getPlayer(), new Objective(ObjectiveType.EAT), 1);
        incrementObjective(e.getPlayer(), new MaterialObjective(ObjectiveType.EAT, e.getItem().getType()), 1);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerFishItem(PlayerFishEvent e) {
        if (disabledWorlds.contains(e.getPlayer().getWorld().getUID())) return;
        if (disabledWorldsByType.get(ObjectiveType.FISH).contains(e.getPlayer().getWorld().getUID())) return;

        Entity caughtEntity = e.getCaught();
        if (caughtEntity == null || e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        incrementObjective(e.getPlayer(), new Objective(ObjectiveType.FISH), 1);
        incrementObjective(e.getPlayer(), new MaterialObjective(ObjectiveType.FISH, ((Item) caughtEntity).getItemStack().getType()), 1);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKillEntity(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;

        if (disabledWorlds.contains(e.getEntity().getWorld().getUID())) return;
        if (disabledWorldsByType.get(ObjectiveType.EAT).contains(e.getEntity().getWorld().getUID())) return;

        incrementObjective(e.getEntity().getKiller(), new Objective(ObjectiveType.EAT), 1);
        incrementObjective(e.getEntity().getKiller(), new EntityTypeObjective(ObjectiveType.EAT, e.getEntityType()), 1);
    }

    // https://github.com/brcdev-minecraft/shopgui-api/blob/master/src/main/java/net/brcdev/shopgui/event/ShopPreTransactionEvent.java
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerSellItem(ShopPreTransactionEvent e) {
        if (e.getShopAction().equals(ShopManager.ShopAction.SELL) || e.getShopAction().equals(ShopManager.ShopAction.SELL_ALL)) {
            if (disabledWorlds.contains(e.getPlayer().getWorld().getUID())) return;

            ItemStack soldItem = e.getShopItem().getItem();

            int numSellingItem = getNumberInInventory(e.getPlayer(), soldItem);
            Bukkit.getScheduler().scheduleSyncDelayedTask(TournamentsPlugin.getInstance(), () -> {
                // If the amount of the sold item changed in the player's inventory
                if (numSellingItem != getNumberInInventory(e.getPlayer(), soldItem)) {
                    int amount = e.getAmount();

                    if (!disabledWorldsByType.get(ObjectiveType.EARN).contains(e.getPlayer().getWorld().getUID())) {
                        incrementObjective(e.getPlayer(), new Objective(ObjectiveType.EARN), (long) e.getPrice());
                    }

                    if (!disabledWorldsByType.get(ObjectiveType.SELL_ITEM).contains(e.getPlayer().getWorld().getUID())) {
                        incrementObjective(e.getPlayer(), new Objective(ObjectiveType.SELL_ITEM), amount);
                        incrementObjective(e.getPlayer(), new MaterialObjective(ObjectiveType.SELL_ITEM, soldItem.getType()), amount);
                    }
                }
            }, 1);
        }
    }

    private int getNumberInInventory(Player p, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0;

        int count = 0;
        for (ItemStack itemStack : p.getInventory().getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            if (itemStack.getType() == item.getType()) {
                count += itemStack.getAmount();
            }
        }
        return count;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRemoveFromFurnace(FurnaceExtractEvent e) {
        if (disabledWorlds.contains(e.getPlayer().getWorld().getUID())) return;
        if (disabledWorldsByType.get(ObjectiveType.SMELT).contains(e.getPlayer().getWorld().getUID())) return;

        incrementObjective(e.getPlayer(), new Objective(ObjectiveType.SMELT), e.getItemAmount());
        incrementObjective(e.getPlayer(), new MaterialObjective(ObjectiveType.SMELT, e.getItemType()), e.getItemAmount());
    }


    //*** OTHER METHODS ***//

    /**
     * Toggles the debug mode for objective debugging
     * @param sender The sender of the command to print the result to
     */
    public void toggleDebugMode(CommandSender sender) {
        isDebugging = !isDebugging;

        ObjectivesConfig.get().set("debug", isDebugging);
        ObjectivesConfig.save();

        if (isDebugging) {
            sender.sendMessage(ChatColor.YELLOW + String.valueOf(ChatColor.BOLD) + "[Tourn Debug] " + ChatColor.GREEN + "Is now enabled for admins");
        }
        else {
            sender.sendMessage(ChatColor.YELLOW + String.valueOf(ChatColor.BOLD) + "[Tourn Debug] " + ChatColor.RED + "Is now disabled");
        }
    }

}
