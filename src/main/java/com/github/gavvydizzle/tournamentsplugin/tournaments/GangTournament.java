package com.github.gavvydizzle.tournamentsplugin.tournaments;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.gui.leaderboard.GangLeaderboardInventory;
import com.github.gavvydizzle.tournamentsplugin.objectives.Objective;
import com.github.gavvydizzle.tournamentsplugin.rewards.TournamentRewards;
import com.github.gavvydizzle.tournamentsplugin.sorters.GangParticipantSorter;
import com.github.mittenmc.gangsplugin.gangs.Gang;
import com.github.mittenmc.gangsplugin.gangs.GangMember;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class GangTournament extends Tournament {

    private final HashMap<Gang, GangParticipant> gangs; //TODO - Figure our a different data structure when a gang tournament is completed
    private final ArrayList<GangParticipant> sortedGangs;

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
    public GangTournament(@NotNull String id, @NotNull String displayName, @NotNull Date startDate, @NotNull Date endDate, @NotNull Objective objective,
                          @NotNull TournamentRewards tournamentReward, ItemStack pendingItem, ItemStack activeItem, ItemStack completedItem, File file) {
        super(id, displayName, startDate, endDate, objective, tournamentReward, pendingItem, activeItem, completedItem, file);
        this.gangs = new HashMap<>();
        this.sortedGangs = new ArrayList<>();


        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        this.leaderboardInventory = new GangLeaderboardInventory(this, configuration);

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
        sortedGangs.addAll(TournamentsPlugin.getInstance().getDatabase().loadTournament(this));
        for (GangParticipant gangParticipant : sortedGangs) {
            gangs.put(gangParticipant.getGang(), gangParticipant);
        }
        updateLeaderboard();
    }

    @Override
    void saveToDatabase() {
        TournamentsPlugin.getInstance().getDatabase().saveTournament(this);
    }

    @Override
    void updateLeaderboard() {
        sortedGangs.sort(new GangParticipantSorter());
    }

    @Override
    public void incrementProgress(Player player, long amount) {
        GangMember gangMember = TournamentsPlugin.getInstance().getGangsAPI().getGangMember(player);
        if (gangMember == null) return;

        if (gangs.containsKey(gangMember.getGang())) {
            gangs.get(gangMember.getGang()).incrementScore(amount);
        }
        else {
            GangParticipant gangParticipant = new GangParticipant(gangMember.getGang(), amount);
            gangs.put(gangMember.getGang(), gangParticipant);
            sortedGangs.add(gangParticipant);
        }
    }

    @Override
    public void runRewardCommands() {
        for (int i = 0; i < sortedGangs.size(); i++) {
            tournamentRewards.rewardGang(sortedGangs.get(i).getGang(), i + 1);
        }
    }

    @Override
    public Set<String> getParticipantNames() {
        Set<String> list = new HashSet<>();
        for (GangParticipant gangParticipant : sortedGangs) {
            list.add(gangParticipant.getGangName());
        }
        return list;
    }

    @Nullable
    public GangParticipant getParticipantByName(String name) {
        for (GangParticipant gangParticipant : sortedGangs) {
            if (gangParticipant.getGangName().equalsIgnoreCase(name)) {
                return gangParticipant;
            }
        }
        return null;
    }

    /**
     * Removes the gang from the tournament only if this is an ACTIVE tournament
     * @param gang The gang to remove
     */
    public void removeGangFromTournament(Gang gang) {
        if (getTimeType() == TournamentTimeType.ACTIVE) {
            TournamentsPlugin.getInstance().getDatabase().removeParticipant(this, gangs.remove(gang));
        }
    }


    public ArrayList<GangParticipant> getSortedGangs() {
        return sortedGangs;
    }

    public GangParticipant getParticipant(Player player) {
        for (GangParticipant gangParticipant : sortedGangs) {
            if (gangParticipant.getGang() != null && gangParticipant.getGang().isPlayerInGang(player.getUniqueId())) {
                return gangParticipant;
            }
        }
        return null;
    }

    public int getPlacement(GangParticipant gangParticipant) {
        return sortedGangs.indexOf(gangParticipant) + 1;
    }
}
