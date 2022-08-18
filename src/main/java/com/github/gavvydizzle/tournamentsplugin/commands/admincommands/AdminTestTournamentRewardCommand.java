package com.github.gavvydizzle.tournamentsplugin.commands.admincommands;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.tournaments.Tournament;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows admins to test a tournament's reward for a specific placement
 */
public class AdminTestTournamentRewardCommand extends SubCommand {

    @Override
    public String getName() {
        return "testReward";
    }

    @Override
    public String getDescription() {
        return "Tests the reward for placement in a tournament";
    }

    @Override
    public String getSyntax() {
        return "/tournadmin testReward <tournament> <placement>";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        if (args.length < 3) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        Tournament tournament = TournamentsPlugin.getInstance().getTournamentManager().getTournamentByID(args[1]);

        if (tournament == null) {
            sender.sendMessage(ChatColor.RED + "No tournament exists with the id: " + args[1]);
            return;
        }

        int placement;
        try {
            placement = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "The placement you provided is invalid");
            return;
        }
        if (placement < 1) {
            sender.sendMessage(ChatColor.RED + "The placement must be a positive number");
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "You have given the rewards as if you placed #" + placement + " in " + tournament.getDisplayName() +
                " (" + tournament.getId() + ")");
        tournament.getTournamentRewards().rewardPlayer((OfflinePlayer) sender, placement);
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], TournamentsPlugin.getInstance().getTournamentManager().getAllTournamentIDs(), list);
        }
        else if (args.length == 3) {
            list.add("placement");
        }

        return list;
    }

}