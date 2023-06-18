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

public class AdminDeleteTournamentCommand extends SubCommand {

    private final TournamentManager tournamentManager;

    public AdminDeleteTournamentCommand(AdminCommandManager commandManager, TournamentManager tournamentManager) {
        this.tournamentManager = tournamentManager;

        setName("delete");
        setDescription("Completely delete a tournament");
        setSyntax("/" + commandManager.getCommandDisplayName() + " delete <id>");
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

        tournamentManager.deleteTournament(tournament);
        sender.sendMessage(ChatColor.YELLOW + "Successfully deleted the tournament '" + tournament.getId() + "' and all of its data.");
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