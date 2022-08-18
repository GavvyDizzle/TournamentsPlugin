package com.github.gavvydizzle.tournamentsplugin.gui.leaderboard;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.tournaments.IndividualParticipant;
import com.github.gavvydizzle.tournamentsplugin.tournaments.IndividualTournament;
import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.PlayerHeads;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

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
            for (int i = 0; i < Math.min(individualTournament.getSortedParticipants().size(), numPlacementsShown); i++) {
                IndividualParticipant individualParticipant = individualTournament.getSortedParticipants().get(i);

                ItemStack leaderboardItem = PlayerHeads.getHead(individualParticipant.getUniqueId());

                ItemMeta meta = leaderboardItem.getItemMeta();
                assert meta != null;
                meta.setDisplayName(placementItemName.replace("{name}", individualParticipant.getName()));

                ArrayList<String> lore = new ArrayList<>(placementItemLore.size());
                for (String str : placementItemLore) {
                    lore.add(str.replace("{name}", individualParticipant.getName())
                            .replace("{placement}", "" + (i + 1))
                            .replace("{total_placements}", "" + individualTournament.getSortedParticipants().size())
                            .replace("{score}", "" + Numbers.withSuffix(individualParticipant.getScore())));
                }

                meta.setLore(lore);
                leaderboardItem.setItemMeta(meta);

                inventory.setItem(i, leaderboardItem);
            }

            IndividualParticipant individualParticipant = individualTournament.getParticipant(player);

            if (individualParticipant != null) {

                ItemStack leaderboardItem = PlayerHeads.getHead(player.getUniqueId());

                ItemMeta meta = leaderboardItem.getItemMeta();
                assert meta != null;
                meta.setDisplayName(personalPlacementItemName.replace("{name}", individualParticipant.getName()));

                ArrayList<String> lore = new ArrayList<>(personalPlacementItemLore.size());
                for (String str : personalPlacementItemLore) {
                    lore.add(str
                            .replace("{name}", individualParticipant.getName())
                            .replace("{placement}", "" + individualTournament.getPlacement(individualParticipant))
                            .replace("{total_placements}", "" + individualTournament.getSortedParticipants().size())
                            .replace("{score}", "" + Numbers.withSuffix(individualParticipant.getScore())));
                }

                meta.setDisplayName(meta.getDisplayName().replace("{name}", individualParticipant.getName()));
                meta.setLore(lore);
                leaderboardItem.setItemMeta(meta);
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
