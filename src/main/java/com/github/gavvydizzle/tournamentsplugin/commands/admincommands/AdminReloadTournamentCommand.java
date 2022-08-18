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
 * Load a tournament by its .yml file
 */
public class AdminReloadTournamentCommand extends SubCommand {

    @Override
    public String getName() {
        return "reloadTournament";
    }

    @Override
    public String getDescription() {
        return "Reloads a single tournament from its config";
    }

    @Override
    public String getSyntax() {
        return "/tournadmin reloadTournament <tournament>";
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

        if (tournamentManager.reloadTournament(tournament)) {
            sender.sendMessage(ChatColor.GREEN + "Tournament successfully reloaded: " + tournament.getId());
        }
        else {
            sender.sendMessage(ChatColor.RED + "Something went wrong when reloading " + tournament.getConfigFile() + ". Please check the console for an error message.");
        }
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