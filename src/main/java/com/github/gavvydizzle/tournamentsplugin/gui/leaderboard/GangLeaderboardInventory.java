package com.github.gavvydizzle.tournamentsplugin.gui.leaderboard;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.tournaments.GangParticipant;
import com.github.gavvydizzle.tournamentsplugin.tournaments.GangTournament;
import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;
import com.github.gavvydizzle.tournamentsplugin.tournaments.TournamentTimeType;
import com.github.mittenmc.serverutils.ItemStackUtils;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class GangLeaderboardInventory extends LeaderboardInventory {

    public GangLeaderboardInventory(Tournament tournament, FileConfiguration config) {
        super(tournament, config);
    }

    @Override
    public void openInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(player, inventorySize, inventoryName);
        for (int i = 0; i < inventorySize; i++) {
            inventory.setItem(i, filler);
        }

        GangTournament gangTournament = (GangTournament) tournament;
        HashMap<String, String> map = new HashMap<>();

        if (tournament.getTimeType() == TournamentTimeType.COMPLETED) {
            for (int i = 0; i < Math.min(gangTournament.getSortedGangs().size(), numPlacementsShown); i++) {
                GangParticipant gangParticipant = gangTournament.getSortedGangs().get(i);

                ItemStack leaderboardItem = new ItemStack(Material.PAPER);
                ItemMeta meta = leaderboardItem.getItemMeta();
                assert meta != null;
                meta.setDisplayName(placementItemName);
                meta.setLore(placementItemLore);
                leaderboardItem.setItemMeta(meta);

                map.put("{name}", gangParticipant.getGangName());
                map.put("{placement}", String.valueOf(i + 1));
                map.put("{total_placements}", String.valueOf(gangTournament.getSortedGangs().size()));
                map.put("{score}", Numbers.withSuffix(gangParticipant.getScore()));

                ItemStackUtils.replacePlaceholders(leaderboardItem, map);
                inventory.setItem(i, leaderboardItem);
            }
        }
        else {
            for (int i = 0; i < Math.min(gangTournament.getSortedGangs().size(), numPlacementsShown); i++) {
                GangParticipant gangParticipant = gangTournament.getSortedGangs().get(i);
                if (gangParticipant.getGang() == null) continue;

                ItemStack leaderboardItem = gangParticipant.getGang().getIdentifierItem().clone();
                ItemMeta meta = leaderboardItem.getItemMeta();
                assert meta != null;
                meta.setDisplayName(placementItemName);
                meta.setLore(placementItemLore);
                leaderboardItem.setItemMeta(meta);

                map.put("{name}", gangParticipant.getGang().getName());
                map.put("{placement}", String.valueOf(i + 1));
                map.put("{total_placements}", String.valueOf(gangTournament.getSortedGangs().size()));
                map.put("{score}", Numbers.withSuffix(gangParticipant.getScore()));

                ItemStackUtils.replacePlaceholders(leaderboardItem, map);
                inventory.setItem(i, leaderboardItem);
            }

            GangParticipant gangParticipant = gangTournament.getParticipant(player);

            if (gangParticipant != null && gangParticipant.getGang() != null) {

                ItemStack leaderboardItem = gangParticipant.getGang().getIdentifierItem().clone();
                ItemMeta meta = leaderboardItem.getItemMeta();
                assert meta != null;
                meta.setDisplayName(personalPlacementItemName);
                meta.setLore(personalPlacementItemLore);
                leaderboardItem.setItemMeta(meta);

                map.put("{name}", gangParticipant.getGang().getName());
                map.put("{placement}", String.valueOf(gangTournament.getPlacement(gangParticipant)));
                map.put("{total_placements}", String.valueOf(gangTournament.getSortedGangs().size()));
                map.put("{score}", Numbers.withSuffix(gangParticipant.getScore()));

                ItemStackUtils.replacePlaceholders(leaderboardItem, map);
                inventory.setItem(personalPlacementItemSlot, leaderboardItem);
            }
        }

        try {
            inventory.setItem(backButtonSlot, TournamentsPlugin.getInstance().getInventoryManager().getBackButtonItem());
        } catch (Exception ignored) {}

        player.openInventory(inventory);
        TournamentsPlugin.getInstance().getInventoryManager().setClickableGUI(player, this);
    }

}