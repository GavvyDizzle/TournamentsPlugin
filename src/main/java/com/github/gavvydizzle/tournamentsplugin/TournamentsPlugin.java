package com.github.gavvydizzle.tournamentsplugin;

import com.github.gavvydizzle.tournamentsplugin.commands.AdminCommandManager;
import com.github.gavvydizzle.tournamentsplugin.commands.PlayerCommandManager;
import com.github.gavvydizzle.tournamentsplugin.configs.Configuration;
import com.github.gavvydizzle.tournamentsplugin.gui.InventoryManager;
import com.github.gavvydizzle.tournamentsplugin.objectives.ObjectiveManager;
import com.github.gavvydizzle.tournamentsplugin.storage.Database;
import com.github.gavvydizzle.tournamentsplugin.storage.SQLite;
import com.github.gavvydizzle.tournamentsplugin.tournaments.TournamentManager;
import com.github.mittenmc.gangsplugin.api.GangsAPI;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TournamentsPlugin extends JavaPlugin {

    private static TournamentsPlugin instance;
    private GangsAPI gangsAPI;

    private Database database;

    private TournamentManager tournamentManager;
    private ObjectiveManager objectiveManager;
    private InventoryManager inventoryManager;

    private AdminCommandManager adminCommandManager;

    @Override
    public void onEnable() {
        instance = this;
        try {
            gangsAPI = new GangsAPI();
        } catch (Exception ignored) {}

        database = new SQLite(this);

        Configuration.reload();

        inventoryManager = new InventoryManager();
        tournamentManager = new TournamentManager();
        inventoryManager.getTournamentListManager().reloadLists();
        objectiveManager = new ObjectiveManager(tournamentManager);

        getServer().getPluginManager().registerEvents(tournamentManager, this);
        getServer().getPluginManager().registerEvents(objectiveManager, this);
        getServer().getPluginManager().registerEvents(inventoryManager, this);

        adminCommandManager = new AdminCommandManager();
        Objects.requireNonNull(getCommand("tourn")).setExecutor(new PlayerCommandManager());
        Objects.requireNonNull(getCommand("tournadmin")).setExecutor(adminCommandManager);
    }

    @Override
    public void onDisable() {
        try {
            if (tournamentManager != null) {
                tournamentManager.saveAllTournaments();
                this.getLogger().info("Saved all tournaments on shutdown");
            }
        } catch (Exception e) {
            this.getLogger().severe("Failed to save all tournaments on shutdown");
            e.printStackTrace();
        }
    }

    public static TournamentsPlugin getInstance() {
        return instance;
    }

    public GangsAPI getGangsAPI() {
        return gangsAPI;
    }

    public TournamentManager getTournamentManager() {
        return tournamentManager;
    }

    public ObjectiveManager getObjectiveManager() {
        return objectiveManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public AdminCommandManager getAdminCommandManager() {
        return adminCommandManager;
    }

    public Database getDatabase() {
        return database;
    }

}
