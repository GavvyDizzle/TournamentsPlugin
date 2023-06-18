package com.github.gavvydizzle.tournamentsplugin.commands.admincommands;

import com.github.gavvydizzle.tournamentsplugin.commands.AdminCommandManager;
import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;
import com.github.gavvydizzle.tournamentsplugin.tournaments.TournamentManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class AdminRegenerateTournamentDatabaseCommand extends SubCommand {

    private final TournamentManager tournamentManager;

    public AdminRegenerateTournamentDatabaseCommand(AdminCommandManager commandManager, TournamentManager tournamentManager) {
        this.tournamentManager = tournamentManager;

        setName("regenerateTable");
        setDescription("Regenerates a tournament's data table in the database");
        setSyntax("/" + commandManager.getCommandDisplayName() + " regenerateTable <id>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(commandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        Tournament tournament = tournamentManager.getActiveTournamentByID(args[1]);

        if (tournament == null) {
            sender.sendMessage(ChatColor.RED + "No tournament exists with the id: " + args[1]);
            return;
        }

        tournamentManager.regenerateTournamentDatabaseTable(tournament);
        sender.sendMessage(ChatColor.GREEN + "Successfully regenerated the table for: " + tournament.getId());
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], tournamentManager.getAllTournamentIDs(), list);
        }

        return list;
    }

}