package com.github.gavvydizzle.tournamentsplugin.gui.leaderboard;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.tournaments.IndividualParticipant;
import com.github.gavvydizzle.tournamentsplugin.tournaments.IndividualTournament;
import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;
import com.github.mittenmc.serverutils.ItemStackUtils;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.PlayerHeads;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class IndividualLeaderboardInventory extends LeaderboardInventory {

    public IndividualLeaderboardInventory(Tournament tournament, FileConfiguration config) {
        super(tournament, config);
    }

    @Override
    public void openInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(player, inventorySize, inventoryName);
        for (int i = 0; i < inventorySize; i++) {
            inventory.setItem(i, filler);
        }

        IndividualTournament individualTournament = (IndividualTournament) tournament;

        Bukkit.getScheduler().runTaskAsynchronously(TournamentsPlugin.getInstance(), () -> {
            HashMap<String, String> map = new HashMap<>();

            for (int i = 0; i < Math.min(individualTournament.getSortedParticipants().size(), numPlacementsShown); i++) {
                IndividualParticipant individualParticipant = individualTournament.getSortedParticipants().get(i);

                ItemStack leaderboardItem = PlayerHeads.getHead(individualParticipant.getUniqueId());
                ItemMeta meta = leaderboardItem.getItemMeta();
                assert meta != null;
                meta.setDisplayName(placementItemName);
                meta.setLore(placementItemLore);
                leaderboardItem.setItemMeta(meta);

                map.put("{name}", individualParticipant.getName());
                map.put("{placement}", String.valueOf(i + 1));
                map.put("{total_placements}", String.valueOf(individualTournament.getSortedParticipants().size()));
                map.put("{score}", Numbers.withSuffix(individualParticipant.getScore()));

                ItemStackUtils.replacePlaceholders(leaderboardItem, map);
                inventory.setItem(i, leaderboardItem);
            }

            IndividualParticipant individualParticipant = individualTournament.getParticipant(player);

            if (individualParticipant != null) {

                ItemStack leaderboardItem = PlayerHeads.getHead(player.getUniqueId());
                ItemMeta meta = leaderboardItem.getItemMeta();
                assert meta != null;
                meta.setDisplayName(personalPlacementItemName);
                meta.setLore(personalPlacementItemLore);
                leaderboardItem.setItemMeta(meta);

                map.put("{name}", individualParticipant.getName());
                map.put("{placement}", String.valueOf(individualTournament.getPlacement(individualParticipant)));
                map.put("{total_placements}", String.valueOf(individualTournament.getSortedParticipants().size()));
                map.put("{score}", Numbers.withSuffix(individualParticipant.getScore()));

                ItemStackUtils.replacePlaceholders(leaderboardItem, map);
                inventory.setItem(personalPlacementItemSlot, leaderboardItem);
            }
        });

        try {
            inventory.setItem(backButtonSlot, TournamentsPlugin.getInstance().getInventoryManager().getBackButtonItem());
        } catch (Exception ignored) {}

        player.openInventory(inventory);
        TournamentsPlugin.getInstance().getInventoryManager().setClickableGUI(player, this);
    }

}
