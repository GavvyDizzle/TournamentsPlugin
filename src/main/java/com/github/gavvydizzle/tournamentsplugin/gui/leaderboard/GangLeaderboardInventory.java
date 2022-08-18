package com.github.gavvydizzle.tournamentsplugin.gui.leaderboard;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.tournaments.GangParticipant;
import com.github.gavvydizzle.tournamentsplugin.tournaments.GangTournament;
import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;
import com.github.gavvydizzle.tournamentsplugin.tournaments.TournamentTimeType;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

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

        if (tournament.getTimeType() == TournamentTimeType.COMPLETED) {
            ItemStack paper = new ItemStack(Material.PAPER);

            for (int i = 0; i < Math.min(gangTournament.getSortedGangs().size(), numPlacementsShown); i++) {
                GangParticipant gangParticipant = gangTournament.getSortedGangs().get(i);

                ItemStack leaderboardItem = paper.clone();

                ItemMeta meta = leaderboardItem.getItemMeta();
                assert meta != null;
                meta.setDisplayName(placementItemName.replace("{name}", gangParticipant.getGangName()));

                ArrayList<String> lore = new ArrayList<>(placementItemLore.size());
                for (String str : placementItemLore) {
                    lore.add(str.replace("{name}", gangParticipant.getGangName())
                            .replace("{placement}", "" + (i + 1))
                            .replace("{total_placements}", "" + gangTournament.getSortedGangs().size())
                            .replace("{score}", "" + gangParticipant.getScore()));
                }

                meta.setLore(lore);
                leaderboardItem.setItemMeta(meta);

                inventory.setItem(i, leaderboardItem);
            }
        }
        else {
            for (int i = 0; i < Math.min(gangTournament.getSortedGangs().size(), numPlacementsShown); i++) {
                GangParticipant gangParticipant = gangTournament.getSortedGangs().get(i);

                assert gangParticipant.getGang() != null;
                ItemStack leaderboardItem = gangParticipant.getGang().getIdentifierItem().clone();

                ItemMeta meta = leaderboardItem.getItemMeta();
                assert meta != null;
                meta.setDisplayName(placementItemName.replace("{name}", gangParticipant.getGang().getName()));

                ArrayList<String> lore = new ArrayList<>(placementItemLore.size());
                for (String str : placementItemLore) {
                    lore.add(str.replace("{name}", gangParticipant.getGang().getName())
                            .replace("{placement}", "" + (i + 1))
                            .replace("{total_placements}", "" + gangTournament.getSortedGangs().size())
                            .replace("{score}", "" + Numbers.withSuffix(gangParticipant.getScore())));
                }

                meta.setLore(lore);
                leaderboardItem.setItemMeta(meta);

                inventory.setItem(i, leaderboardItem);
            }

            GangParticipant gangParticipant = gangTournament.getParticipant(player);

            if (gangParticipant != null) {

                assert gangParticipant.getGang() != null;
                ItemStack leaderboardItem = gangParticipant.getGang().getIdentifierItem().clone();

                ItemMeta meta = leaderboardItem.getItemMeta();
                assert meta != null;
                meta.setDisplayName(personalPlacementItemName.replace("{name}", gangParticipant.getGang().getName()));

                ArrayList<String> lore = new ArrayList<>(personalPlacementItemLore.size());
                for (String str : personalPlacementItemLore) {
                    lore.add(str.replace("{name}", gangParticipant.getGang().getName())
                            .replace("{placement}", "" + gangTournament.getPlacement(gangParticipant))
                            .replace("{total_placements}", "" + gangTournament.getSortedGangs().size())
                            .replace("{score}", "" + Numbers.withSuffix(gangParticipant.getScore())));
                }

                meta.setLore(lore);
                leaderboardItem.setItemMeta(meta);

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