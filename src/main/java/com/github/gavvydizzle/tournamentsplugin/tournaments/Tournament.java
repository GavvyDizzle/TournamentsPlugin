package com.github.gavvydizzle.tournamentsplugin.tournaments;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.gui.RewardInventory;
import com.github.gavvydizzle.tournamentsplugin.gui.leaderboard.LeaderboardInventory;
import com.github.gavvydizzle.tournamentsplugin.objectives.Objective;
import com.github.gavvydizzle.tournamentsplugin.rewards.TournamentRewards;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Date;
import java.util.Set;

/**
 * Defines a tournament which defined an Objective that is meant to be met during a period of time
 */
public abstract class Tournament {

    private final String id, displayName;
    private final Date startDate, endDate;
    private final Objective objective;
    private int taskID;

    protected LeaderboardInventory leaderboardInventory;
    private final RewardInventory rewardInventory;
    protected final TournamentRewards tournamentRewards;
    private final ItemStack pendingItem, activeItem, completedItem;

    private final File config;

    /**
     * Creates a new Tournament
     * @param id                The ID of this tournament. This must be unique
     * @param displayName       The name of this tournament to display in menus
     * @param startDate         The start date (yyyy-MM-dd HH:mm)
     * @param endDate           The end date (yyyy-MM-dd HH:mm)
     * @param objective         The objective
     * @param tournamentRewards The tournamentRewards
     * @param pendingItem       The item to show in the inventory when this tournament has not started
     * @param activeItem        The item to show in the inventory when this tournament is currently happening
     * @param completedItem     The item to show in the inventory when this tournament is finished
     * @param file             The file that the tournament is being read from
     */
    public Tournament(@NotNull String id, @NotNull String displayName, @NotNull Date startDate, @NotNull Date endDate, @NotNull Objective objective,
                      @NotNull TournamentRewards tournamentRewards, ItemStack pendingItem, ItemStack activeItem, ItemStack completedItem,
                      File file) {
        this.id = id;
        this.displayName = displayName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.objective = objective;
        this.taskID = -1;
        this.tournamentRewards = tournamentRewards;
        this.pendingItem = pendingItem;
        this.activeItem = activeItem;
        this.completedItem = completedItem;
        this.config = file;

        tournamentRewards.setTournament(this);
        rewardInventory = new RewardInventory(tournamentRewards);
    }

    /**
     * Creates a table in the database for this tournament if one does not exist
     */
    abstract void createDatabase();

    /**
     * Loads the tournament from the database
     */
    abstract void loadFromDatabase();

    /**
     * Saves the tournament info to the database
     */
    abstract void saveToDatabase();

    /**
     * Updates the contents of the leaderboard
     */
    abstract void updateLeaderboard();

    /**
     * Opens the leaderboard
     */
    public void openLeaderboard(Player player) {
        updateLeaderboard();
        leaderboardInventory.openInventory(player);
    }

    /**
     * Opens the rewards menu
     */
    public void openRewardInventory(Player player) {
        rewardInventory.openInventory(player);
    }

    /**
     * Handles when the tournament first becomes active
     */
    public void startTournament() {
        TournamentsPlugin.getInstance().getTournamentManager().setTournamentActive(this);
        loadFromDatabase();
    }

    /**
     * Handles when the tournament ends
     */
    public void endTournament() {
        saveToDatabase();
        runRewardCommands();
        TournamentsPlugin.getInstance().getTournamentManager().removeActiveTournament(this);
    }

    /**
     * Increases the score for this player.
     * If this player was not in the tournament, they will be added by calling this method on them for the first time.
     * @param player The player
     * @param amount The amount to increase by
     */
    public abstract void incrementProgress(Player player, long amount);

    /**
     * Runs the commands that are set to run at the completion of the tournament.
     * These commands will be run on offline players only!
     */
    public abstract void runRewardCommands();

    /**
     * @return A list of all participant names in this tournament
     */
    public abstract Set<String> getParticipantNames();

    /**
     * PENDING means the tournament has not started yet
     * ACTIVE means the tournament is currently active
     * COMPLETED means the tournament has ended
     * @return The classification of this tournament's status based on its start and end time
     */
    public TournamentTimeType getTimeType() {
        if (System.currentTimeMillis() < startDate.getTime()) {
            return TournamentTimeType.PENDING;
        }
        else if (System.currentTimeMillis() > endDate.getTime()) {
            return TournamentTimeType.COMPLETED;
        }
        else {
            return TournamentTimeType.ACTIVE;
        }
    }

    public String getTimeRemainingUntilStart() {
        return Numbers.getTimeFormatted((int) ((startDate.getTime() - System.currentTimeMillis()) / 1000), "Started");
    }

    public String getTimeRemainingUntilEnd() {
        return Numbers.getTimeFormatted((int) ((endDate.getTime() - System.currentTimeMillis()) / 1000), "Ended");
    }

    public void cancelTask() {
        if (taskID != -1) {
            Bukkit.getScheduler().cancelTask(taskID);
            taskID = -1;
        }
    }

    public boolean hasNoScheduledTask() {
        return taskID == -1;
    }

    public void setTaskID(int id) {
        this.taskID = id;
    }


    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Objective getObjective() {
        return objective;
    }

    public TournamentRewards getTournamentRewards() {
        return tournamentRewards;
    }

    public ItemStack getPendingItem() {
        return pendingItem;
    }

    public ItemStack getActiveItem() {
        return activeItem;
    }

    public ItemStack getCompletedItem() {
        return completedItem;
    }

    public File getConfigFile() {
        return config;
    }

}
