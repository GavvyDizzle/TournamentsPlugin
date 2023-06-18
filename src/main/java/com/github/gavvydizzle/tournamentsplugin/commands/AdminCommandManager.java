package com.github.gavvydizzle.tournamentsplugin.commands;

import com.github.gavvydizzle.tournamentsplugin.commands.admincommands.*;
import com.github.gavvydizzle.tournamentsplugin.objectives.ObjectiveManager;
import com.github.gavvydizzle.tournamentsplugin.tournaments.TournamentManager;
import com.github.mittenmc.serverutils.CommandManager;
import org.bukkit.command.PluginCommand;

public class AdminCommandManager extends CommandManager {

    public AdminCommandManager(PluginCommand command, TournamentManager tournamentManager, ObjectiveManager objectiveManager) {
        super(command);

        registerCommand(new AdminDebugCommand(this, objectiveManager));
        registerCommand(new AdminDeleteTournamentCommand(this, tournamentManager));
        registerCommand(new AdminHelpCommand(this));
        registerCommand(new AdminRegenerateTournamentDatabaseCommand(this, tournamentManager));
        registerCommand(new AdminReloadCommand(this, tournamentManager));
        registerCommand(new AdminReloadTournamentCommand(this, tournamentManager));
        registerCommand(new AdminSetScoreCommand(this, tournamentManager));
        registerCommand(new AdminTestTournamentRewardCommand(this, tournamentManager));
        sortCommands();
    }
}