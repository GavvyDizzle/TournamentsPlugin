package com.github.gavvydizzle.tournamentsplugin.commands.admincommands;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;
import com.github.gavvydizzle.tournamentsplugin.tournaments.TournamentManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class AdminRegenerateTournamentDatabaseCommand extends SubCommand {

    @Override
    public String getName() {
        return "regenerateTable";
    }

    @Override
    public String getDescription() {
        return "Regenerates a tournament's data table in the database";
    }

    @Override
    public String getSyntax() {
        return "/tournadmin regenerateTable <tournament>";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        TournamentManager tournamentManager = TournamentsPlugin.getInstance().getTournamentManager();
        Tournament tournament = tournamentManager.getActiveTournamentByID(args[1]);

        if (tournament == null) {
            sender.sendMessage(ChatColor.RED + "No tournament exists with the id: " + args[1]);
            return;
        }

        tournamentManager.regenerateTournamentDatabaseTable(tournament);
        sender.sendMessage(ChatColor.GREEN + "Successfully regenerated the table for: " + tournament.getId());
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], TournamentsPlugin.getInstance().getTournamentManager().getAllTournamentIDs(), list);
        }

        return list;
    }

}