package com.github.gavvydizzle.tournamentsplugin.tournaments;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.configs.Configuration;
import com.github.gavvydizzle.tournamentsplugin.objectives.Objective;
import com.github.gavvydizzle.tournamentsplugin.rewards.TournamentRewards;
import com.github.gavvydizzle.tournamentsplugin.utils.Utils;
import com.github.mittenmc.gangsplugin.events.GangDisbandEvent;
import com.github.mittenmc.gangsplugin.events.GangsLoadEvent;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.ConfigUtils;
import com.github.mittenmc.serverutils.RepeatingTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TournamentManager implements Listener {

    private static final int STATUS_CHECK_TICKS = 1200;
    private static final int AUTO_SAVE_DELAY_TICKS = 6000;
    private static final int EXTRA_SCHEDULED_TICKS = 10;

    private final ArrayList<Tournament> tournaments;
    private final ArrayList<Tournament> activeTournaments;

    public TournamentManager() {
        tournaments = new ArrayList<>();
        activeTournaments = new ArrayList<>();

        attemptToGenerateTournaments();
    }

    /**
     * Loads all tournaments if the gangs plugin cannot be found
     */
    private void attemptToGenerateTournaments() {
        if (!TournamentsPlugin.getInstance().getServer().getPluginManager().isPluginEnabled("GangsPlugin")) {
            initializeTournaments();
            TournamentsPlugin.getInstance().getLogger().info("Tournaments loaded without Gangs hook");
        }
    }

    /**
     * Loads all tournaments and begins the saving and status tasks
     */
    private void initializeTournaments() {
        generateTournaments();
        startSavingLoop();
        startTournamentStatusLoop();
    }

    // Loads all tournaments once gangs have loaded. This event will only be fired if the gangs plugin is active
    @EventHandler
    private void onGangsLoad(GangsLoadEvent e) {
        initializeTournaments();
        TournamentsPlugin.getInstance().getInventoryManager().getTournamentListManager().updateLists();
        TournamentsPlugin.getInstance().getLogger().info("Tournaments loaded with Gangs hook");
    }

    private void startSavingLoop() {
        new RepeatingTask(TournamentsPlugin.getInstance(), AUTO_SAVE_DELAY_TICKS, AUTO_SAVE_DELAY_TICKS) {
            @Override
            public void run() {
                saveAllTournaments();
            }
        };
    }

    private void startTournamentStatusLoop() {
        // The first call should be done immediately to guarantee that tournaments completed while the server was down get completed right away
        // Check every 1min. If a tourney is going to end in the next minute, set it off on its own delay

        new RepeatingTask(TournamentsPlugin.getInstance(), 0, STATUS_CHECK_TICKS) {
            @Override
            public void run() {
                scheduleTournamentActions();
            }
        };
    }

    private void scheduleTournamentActions() {
        long ticksSinceEpoch = System.currentTimeMillis() / 50;

        for (Tournament tournament : tournaments) {
            if (tournament.getTimeType() == TournamentTimeType.PENDING &&
                    tournament.getStartDate().getTime() / 50 - ticksSinceEpoch < STATUS_CHECK_TICKS) {
                scheduleTournamentToStart(tournament, tournament.getStartDate().getTime() / 50 - ticksSinceEpoch);
            }
            else if (tournament.getTimeType() == TournamentTimeType.ACTIVE &&
                    tournament.getEndDate().getTime() / 50 - ticksSinceEpoch < STATUS_CHECK_TICKS) {
                scheduleTournamentToEnd(tournament, tournament.getEndDate().getTime() / 50 - ticksSinceEpoch);
            }
        }
    }

    private void scheduleTournamentToStart(Tournament tournament, long ticks) {
        if (tournament.hasNoScheduledTask()) {
            int taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(TournamentsPlugin.getInstance(), () -> {
                tournament.startTournament();
                TournamentsPlugin.getInstance().getInventoryManager().getTournamentListManager().updateLists();
                tournament.setTaskID(-1);
            }, ticks + EXTRA_SCHEDULED_TICKS);

            tournament.setTaskID(taskID);
        }
    }

    private void scheduleTournamentToEnd(Tournament tournament, long ticks) {
        if (tournament.hasNoScheduledTask()) {
            int taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(TournamentsPlugin.getInstance(), () -> {
                tournament.endTournament();
                TournamentsPlugin.getInstance().getInventoryManager().getTournamentListManager().updateLists();
                tournament.setTaskID(-1);
            }, ticks + EXTRA_SCHEDULED_TICKS);

            tournament.setTaskID(taskID);
        }
    }

    public void saveAllTournaments() {
        for (Tournament tournament : activeTournaments) {
            tournament.saveToDatabase();
        }
    }

    public void reloadTournaments() {
        saveAllTournaments();
        generateTournaments();
    }

    /**
     * This will delete the existing database and regenerate it with the existing data.
     * This is generally useful when needing to fix the table associated with this tournament.
     * Since all data is stored in memory, this will not lose any data.
     * @param tournament The tournament to delete
     */
    public void regenerateTournamentDatabaseTable(Tournament tournament) {
        TournamentsPlugin.getInstance().getDatabase().deleteTable(tournament);
        tournament.createDatabase();
        tournament.saveToDatabase();
    }

    /**
     * Completely deletes this tournament.
     * This will delete the config file associated with the tournament and its table from the database.
     * @param tournament The tournament to delete
     */
    public void deleteTournament(Tournament tournament) {
        tournaments.remove(tournament);
        activeTournaments.remove(tournament);
        tournament.cancelTask();

        TournamentsPlugin.getInstance().getDatabase().deleteTable(tournament);
        tournament.getConfigFile().delete();

        TournamentsPlugin.getInstance().getInventoryManager().getTournamentListManager().updateLists();
    }

    /**
     * Reloads this tournament from its config
     * @param tournament The tournament to reload
     */
    public boolean reloadTournament(Tournament tournament) {
        tournament.saveToDatabase();
        tournaments.remove(tournament);
        activeTournaments.remove(tournament);
        tournament.cancelTask();

        Tournament newTournament = generateTournament(tournament);
        if (newTournament == null) return false;


        tournaments.add(newTournament);

        if (newTournament.getTimeType() == TournamentTimeType.ACTIVE) {
            activeTournaments.add(newTournament);
        }

        long ticksSinceEpoch = System.currentTimeMillis() / 50;
        if (newTournament.getTimeType() == TournamentTimeType.PENDING &&
                newTournament.getStartDate().getTime() / 50 - ticksSinceEpoch < STATUS_CHECK_TICKS) {
            scheduleTournamentToStart(newTournament, newTournament.getStartDate().getTime() / 50 - ticksSinceEpoch);
        }
        else if (newTournament.getTimeType() == TournamentTimeType.ACTIVE &&
                newTournament.getEndDate().getTime() / 50 - ticksSinceEpoch < STATUS_CHECK_TICKS) {
            scheduleTournamentToEnd(newTournament, newTournament.getEndDate().getTime() / 50 - ticksSinceEpoch);
        }
        else { // Only call this here because the scheduleTournamentToX() methods call this method
            TournamentsPlugin.getInstance().getInventoryManager().getTournamentListManager().updateLists();
        }

        return true;
    }

    /**
     * Generate the given tournament
     * @param tournament The tournament
     */
    private Tournament generateTournament(Tournament tournament) {
        final FileConfiguration config = YamlConfiguration.loadConfiguration(tournament.getConfigFile());

        config.addDefault("id", "");
        config.addDefault("displayName", "name_me_please");
        config.addDefault("type", "INDIVIDUAL");

        Date date = new Date(System.currentTimeMillis() + 86400000);
        config.addDefault("startDate", Configuration.dateFormat.format(date));
        date.setTime(date.toInstant().getEpochSecond() + 604800000);
        config.addDefault("endDate", Configuration.dateFormat.format(date));

        config.addDefault("objective.type", "MINE");
        config.addDefault("objective.material", "STONE");
        config.addDefault("objective.entityType", "");

        try {
            config.save(tournament.getConfigFile());
        }
        catch (IOException e) {
            System.out.println("Could not save file");
            e.printStackTrace();
        }


        TournamentType tournamentType = TournamentType.getTournamentType(config.getString("type"));
        if (tournamentType == null) {
            TournamentsPlugin.getInstance().getLogger().warning("Invalid tournament type given in " + tournament.getConfigFile().getName() + ". The only valid types are INDIVIDUAL and GANG");
            return null;
        }

        String id = config.getString("id");
        if (id == null || id.trim().isEmpty()) {
            TournamentsPlugin.getInstance().getLogger().warning("The id is empty in " + tournament.getConfigFile().getName() + ". Please define it!");
            return null;
        }

        // Check for duplicate ID
        if (getAllTournamentIDs().contains(id)) {
            TournamentsPlugin.getInstance().getLogger().warning("The id '" + id + "' defined in " + tournament.getConfigFile().getName() + " is duplicated elsewhere. To be safe, this tournament will not be loaded.");
            return null;
        }

        String displayName = Colors.conv(config.getString("displayName"));

        Date startDate, endDate;
        Objective objective;
        TournamentRewards tournamentReward;

        try {
            startDate = Configuration.dateFormat.parse(config.getString("startDate"));
            endDate = Configuration.dateFormat.parse(config.getString("endDate"));

            objective = Utils.generateObjective(
                    config.getString("objective.type"),
                    config.getString("objective.material"),
                    config.getString("objective.entityType")
            );
            if (objective == null) {
                TournamentsPlugin.getInstance().getLogger().warning("Failed to generate the objective in " + tournament.getConfigFile().getName());
                return null;
            }

            tournamentReward = new TournamentRewards(config);

        } catch (Exception e) {
            TournamentsPlugin.getInstance().getLogger().severe("Failed to load the Tournament under " + tournament.getConfigFile().getName());
            e.printStackTrace();
            return null;
        }

        ItemStack pending, active, completed;

        pending = new ItemStack(ConfigUtils.getMaterial(config.getString("gui.pendingItem.material")));
        ItemMeta meta = pending.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("gui.pendingItem.name")));
        meta.setLore(Colors.conv(config.getStringList("gui.pendingItem.lore")));
        if (config.getBoolean("gui.pendingItem.isGlowing")) {
            meta.addEnchant(Enchantment.ARROW_FIRE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        pending.setItemMeta(meta);

        active = new ItemStack(ConfigUtils.getMaterial(config.getString("gui.activeItem.material")));
        meta = active.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("gui.activeItem.name")));
        meta.setLore(Colors.conv(config.getStringList("gui.activeItem.lore")));
        if (config.getBoolean("gui.activeItem.isGlowing")) {
            meta.addEnchant(Enchantment.ARROW_FIRE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        active.setItemMeta(meta);

        completed = new ItemStack(ConfigUtils.getMaterial(config.getString("gui.completedItem.material")));
        meta = completed.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("gui.completedItem.name")));
        meta.setLore(Colors.conv(config.getStringList("gui.completedItem.lore")));
        if (config.getBoolean("gui.completedItem.isGlowing")) {
            meta.addEnchant(Enchantment.ARROW_FIRE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        completed.setItemMeta(meta);

        if (tournamentType == TournamentType.INDIVIDUAL) {
            return new IndividualTournament(id, displayName, startDate, endDate, objective, tournamentReward, pending, active, completed, tournament.getConfigFile());
        }
        else if (tournamentType == TournamentType.GANG) {
            return new GangTournament(id, displayName, startDate, endDate, objective, tournamentReward, pending, active, completed, tournament.getConfigFile());
        }

        return null;
    }

    /**
     * 1. Removes any start and/or save tasks of all tournaments
     * 2. Clears the tournament lists
     * 3. Parses all .yml files in /tournaments to load in the tournaments
     * 4. Attempts to schedule start and/or save tasks for the new tournaments
     */
    public void generateTournaments() {
        for (Tournament tournament : tournaments) {
            tournament.cancelTask();
        }

        tournaments.clear();
        activeTournaments.clear();

        try {
            File folder = new File(TournamentsPlugin.getInstance().getDataFolder(), "tournaments");
            TournamentGenerator tournamentGenerator = new TournamentGenerator(folder);
            tournaments.addAll(tournamentGenerator.getTournaments());
            populateActiveTournamentList();
        }
        catch (Exception e) {
            TournamentsPlugin.getInstance().getLogger().severe("Failed to load Tournaments");
            TournamentsPlugin.getInstance().getLogger().severe(e.getMessage());
        }

        scheduleTournamentActions();
    }

    private void populateActiveTournamentList() {
        for (Tournament tournament : tournaments) {
            if (tournament.getTimeType() == TournamentTimeType.ACTIVE) {
                activeTournaments.add(tournament);
            }
        }
    }

    @EventHandler
    private void onGangDisband(GangDisbandEvent e) {
        for (Tournament tournament : activeTournaments) {
            if (tournament instanceof GangTournament) {
                ((GangTournament) tournament).removeGangFromTournament(e.getGang());
            }
        }
    }

    public void updateTournamentLeaderboard(Tournament tournament) {
        tournament.updateLeaderboard();
    }

    public void updateAllTournamentLeaderboards() {
        for (Tournament tournament : activeTournaments) {
            tournament.updateLeaderboard();
        }
    }

    public List<String> getAllTournamentIDs() {
        List<String> list = new ArrayList<>(tournaments.size());
        for (Tournament tournament : tournaments) {
            list.add(tournament.getId());
        }
        return list;
    }

    @Nullable
    public Tournament getTournamentByID(String id) {
        for (Tournament tournament : tournaments) {
            if (tournament.getId().equalsIgnoreCase(id)) {
                return tournament;
            }
        }
        return null;
    }

    public List<String> getActiveTournamentIDs() {
        List<String> list = new ArrayList<>(activeTournaments.size());
        for (Tournament tournament : activeTournaments) {
            list.add(tournament.getId());
        }
        return list;
    }

    @Nullable
    public Tournament getActiveTournamentByID(String id) {
        for (Tournament tournament : activeTournaments) {
            if (tournament.getId().equalsIgnoreCase(id)) {
                return tournament;
            }
        }
        return null;
    }


    public void setTournamentActive(Tournament tournament) {
        activeTournaments.add(tournament);
    }

    public void removeActiveTournament(Tournament tournament) {
        activeTournaments.remove(tournament);
    }

    public ArrayList<Tournament> getTournaments() {
        return tournaments;
    }

    public ArrayList<Tournament> getActiveTournaments() {
        return activeTournaments;
    }

}
