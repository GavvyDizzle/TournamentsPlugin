package com.github.gavvydizzle.tournamentsplugin.rewards;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;
import com.github.mittenmc.gangsplugin.gangs.Gang;
import com.github.mittenmc.serverutils.ColoredItems;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a group of rewards to be given at the end of a tournament
 */
public class TournamentRewards {

    private Tournament tournament;
    private final ArrayList<Reward> rewards;
    private final boolean isOpenableWhenPending, isOpenableWhenActive, isOpenableWhenComplete;
    private final String inventoryName;
    private final int inventorySize;
    private final ItemStack filler;
    private final int backButtonSlot;

    /**
     * Creates all the rewards for a given tournament
     * @param config The configuration file to parse for the rewards
     */
    public TournamentRewards(FileConfiguration config) {
        config.addDefault("rewardsInventory.name", "Tournament Rewards");
        config.addDefault("rewardsInventory.rows", 3);
        config.addDefault("rewardsInventory.filler", "black");
        config.addDefault("rewardsInventory.backButtonSlot", 18);
        config.addDefault("rewardsInventory.isOpenableWhenPending", false);
        config.addDefault("rewardsInventory.isOpenableWhenActive", true);
        config.addDefault("rewardsInventory.isOpenableWhenComplete", true);

        inventoryName = Colors.conv(config.getString("rewardsInventory.name"));
        inventorySize = config.getInt("rewardsInventory.rows") * 9;
        filler = ColoredItems.getGlassByName(config.getString("rewardsInventory.filler"));
        backButtonSlot = config.getInt("rewardsInventory.backButtonSlot");
        isOpenableWhenPending = config.getBoolean("rewardsInventory.isOpenableWhenPending");
        isOpenableWhenActive = config.getBoolean("rewardsInventory.isOpenableWhenActive");
        isOpenableWhenComplete = config.getBoolean("rewardsInventory.isOpenableWhenComplete");

        rewards = new ArrayList<>();

        if (config.getConfigurationSection("rewards") == null) {
            TournamentsPlugin.getInstance().getLogger().warning("You do not have a rewards section defined in the tournament with id: " + tournament.getId());
            return;
        }

        for (String str : Objects.requireNonNull(config.getConfigurationSection("rewards")).getKeys(false)) {
            String path = "rewards." + str;
            try {
                rewards.add(new Reward(
                        config.getInt(path + ".placements.min"),
                        config.getInt(path + ".placements.max"),
                        config.getStringList(path + ".commands"),
                        config.getInt(path + ".inventoryItem.slot"),
                        ConfigUtils.getMaterial(config.getString(path + ".inventoryItem.material")),
                        Colors.conv(config.getString(path + ".inventoryItem.name")),
                        Colors.conv(config.getStringList(path + ".inventoryItem.lore")),
                        config.getBoolean(path + ".inventoryItem.isGlowing")
                ));
            } catch (Exception e) {
                TournamentsPlugin.getInstance().getLogger().warning("Failed to load reward '" + str + "' from the tournament with id: " + tournament.getId() + ". It will not be added!");
                e.printStackTrace();
            }
        }

    }

    /**
     * Reward a player based on their placement
     * @param offlinePlayer The player to reward
     * @param placement The place they got in the tournament
     */
    public void rewardPlayer(OfflinePlayer offlinePlayer, int placement) {
        if (!offlinePlayer.hasPlayedBefore()) return;

        ArrayList<Reward> rewardsList = getRewardsByPlacement(placement);
        if (rewardsList.isEmpty()) return;

        for (Reward reward : rewardsList) {
            for (String cmd : reward.getCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player_name}", Objects.requireNonNull(offlinePlayer.getName())));
            }
        }
    }

    /**
     * Reward a gang's members based on its placement
     * @param gang The gang to reward
     * @param placement The place they got in the tournament
     */
    public void rewardGang(Gang gang, int placement) {
        if (gang == null) return;

        ArrayList<Reward> rewardsList = getRewardsByPlacement(placement);
        if (rewardsList.isEmpty()) return;

        for (UUID uuid : gang.getMembers()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (!offlinePlayer.hasPlayedBefore()) continue;

            for (Reward reward : rewardsList) {
                for (String cmd : reward.getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player_name}", Objects.requireNonNull(offlinePlayer.getName())));
                }
            }
        }
    }

    private ArrayList<Reward> getRewardsByPlacement(int placement) {
        ArrayList<Reward> rewardsList = new ArrayList<>();

        for (Reward reward : rewards) {
            if (reward.getPlacements().contains(placement)) {
                rewardsList.add(reward);
            }
        }
        return rewardsList;
    }


    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public ArrayList<Reward> getRewards() {
        return rewards;
    }

    public boolean isOpenableWhenPending() {
        return isOpenableWhenPending;
    }

    public boolean isOpenableWhenActive() {
        return isOpenableWhenActive;
    }

    public boolean isOpenableWhenComplete() {
        return isOpenableWhenComplete;
    }

    public String getInventoryName() {
        return inventoryName;
    }

    public int getInventorySize() {
        return inventorySize;
    }

    public ItemStack getFiller() {
        return filler;
    }

    public int getBackButtonSlot() {
        return backButtonSlot;
    }
}
