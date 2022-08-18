package com.github.gavvydizzle.tournamentsplugin.tournaments;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.gui.leaderboard.IndividualLeaderboardInventory;
import com.github.gavvydizzle.tournamentsplugin.objectives.Objective;
import com.github.gavvydizzle.tournamentsplugin.rewards.TournamentRewards;
import com.github.gavvydizzle.tournamentsplugin.sorters.IndividualParticipantSorter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class IndividualTournament extends Tournament {

    private final HashMap<UUID, IndividualParticipant> participants;
    private final HashMap<String, IndividualParticipant> participantsByName;
    private final ArrayList<IndividualParticipant> sortedParticipants;

    /**
     * Creates a new Tournament
     *
     * @param id               The ID of this tournament. This must be unique
     * @param displayName      The name of this tournament to display in menus
     * @param startDate        The start date (yyyy-MM-dd HH:mm)
     * @param endDate          The end date (yyyy-MM-dd HH:mm)
     * @param objective        The objective
     * @param tournamentReward The tournamentReward
     * @param pendingItem      The item to show in the inventory when this tournament has not started
     * @param activeItem       The item to show in the inventory when this tournament is currently happening
     * @param completedItem    The item to show in the inventory when this tournament is finished
     * @param file             The file that the tournament is being read from
     */
    public IndividualTournament(@NotNull String id, @NotNull String displayName, @NotNull Date startDate, @NotNull Date endDate, @NotNull Objective objective,
                                @NotNull TournamentRewards tournamentReward, ItemStack pendingItem, ItemStack activeItem, ItemStack completedItem, File file) {
        super(id, displayName, startDate, endDate, objective, tournamentReward, pendingItem, activeItem, completedItem, file);
        participants = new HashMap<>();
        participantsByName = new HashMap<>();
        sortedParticipants = new ArrayList<>();

        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        this.leaderboardInventory = new IndividualLeaderboardInventory(this, configuration);

        if (getTimeType() != TournamentTimeType.PENDING) {
            loadFromDatabase();
        }
    }


    @Override
    void createDatabase() {
        TournamentsPlugin.getInstance().getDatabase().createTournament(this);
    }

    @Override
    void loadFromDatabase() {
        createDatabase();
        sortedParticipants.addAll(TournamentsPlugin.getInstance().getDatabase().loadTournament(this));
        for (IndividualParticipant individualParticipant : sortedParticipants) {
            participants.put(individualParticipant.getUniqueId(), individualParticipant);
            participantsByName.put(individualParticipant.getName(), individualParticipant);
        }
        updateLeaderboard();
    }

    @Override
    void saveToDatabase() {
        TournamentsPlugin.getInstance().getDatabase().saveTournament(this);
    }

    @Override
    void updateLeaderboard() {
        sortedParticipants.sort(new IndividualParticipantSorter());
    }

    @Override
    public void incrementProgress(Player player, long amount) {
        if (participants.containsKey(player.getUniqueId())) {
            participants.get(player.getUniqueId()).incrementScore(amount);
        }
        else {
            IndividualParticipant individualParticipant = new IndividualParticipant(player.getUniqueId(), amount);
            participants.put(player.getUniqueId(), individualParticipant);
            participantsByName.put(individualParticipant.getName(), individualParticipant);
            sortedParticipants.add(individualParticipant);
        }
    }

    @Override
    public void runRewardCommands() {
        for (int i = 0; i < sortedParticipants.size(); i++) {
            tournamentRewards.rewardPlayer(sortedParticipants.get(i).offlinePlayer(), i + 1);
        }
    }

    @Override
    public Set<String> getParticipantNames() {
        return participantsByName.keySet();
    }

    @Nullable
    public IndividualParticipant getParticipantByName(String name) {
        return participantsByName.get(name);
    }

    public ArrayList<IndividualParticipant> getSortedParticipants() {
        return sortedParticipants;
    }

    public IndividualParticipant getParticipant(Player player) {
        return participants.get(player.getUniqueId());
    }

    public int getPlacement(IndividualParticipant individualParticipant) {
        return sortedParticipants.indexOf(individualParticipant) + 1;
    }

}
