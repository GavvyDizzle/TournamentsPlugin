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

/**
 * Force updates one or all tournament leaderboards
 */
public class AdminForceUpdateCommand extends SubCommand {

    @Override
    public String getName() {
        return "forceUpdate";
    }

    @Override
    public String getDescription() {
        return "Force updates tournament(s)";
    }

    @Override
    public String getSyntax() {
        return "/tournadmin forceUpdate [tournament]";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length == 1) {
            TournamentsPlugin.getInstance().getTournamentManager().updateAllTournamentLeaderboards();
            sender.sendMessage(ChatColor.GREEN + "Successfully updated the leaderboard for all tournaments");
        }
        else {
            TournamentManager tournamentManager = TournamentsPlugin.getInstance().getTournamentManager();
            Tournament tournament = tournamentManager.getActiveTournamentByID(args[1]);

            if (tournament == null) {
                sender.sendMessage(ChatColor.RED + "No tournament exists with the id: " + args[1]);
                return;
            }
            tournamentManager.updateTournamentLeaderboard(tournament);
            sender.sendMessage(ChatColor.GREEN + "Successfully updated the leaderboard for the tournament: " + tournament.getId());
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], TournamentsPlugin.getInstance().getTournamentManager().getActiveTournamentIDs(), list);
        }

        return list;
    }

}