package com.github.gavvydizzle.tournamentsplugin.commands.admincommands;

import com.github.gavvydizzle.tournamentsplugin.commands.AdminCommandManager;
import com.github.gavvydizzle.tournamentsplugin.tournaments.*;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class AdminSetScoreCommand extends SubCommand {

    private final TournamentManager tournamentManager;

    public AdminSetScoreCommand(AdminCommandManager commandManager, TournamentManager tournamentManager) {
        this.tournamentManager = tournamentManager;

        setName("setScore");
        setDescription("Set the score of a tournament participant");
        setSyntax("/" + commandManager.getCommandDisplayName() + " setScore <id> <participant> <score>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(commandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        Tournament tournament = tournamentManager.getActiveTournamentByID(args[1]);

        if (tournament == null) {
            sender.sendMessage(ChatColor.RED + "No tournament exists with the id: " + args[1]);
            return;
        }

        long score;
        try {
            score = Long.parseLong(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "The score you provided is invalid");
            return;
        }
        if (score < 0) {
            sender.sendMessage(ChatColor.RED + "The score cannot be negative");
            return;
        }

        if (tournament instanceof IndividualTournament) {
            IndividualParticipant individualParticipant = ((IndividualTournament) tournament).getParticipantByName(args[2]);
            if (individualParticipant == null) {
                sender.sendMessage(ChatColor.RED + "The player " + args[2] + " is not in this tournament");
                return;
            }

            individualParticipant.setScore(score);
            sender.sendMessage(ChatColor.GREEN + "Set the score of " + individualParticipant.getName() + " to " + Numbers.withSuffix(score) +
                    " in the individual tournament: " + tournament.getId());
        }
        else if (tournament instanceof GangTournament) {
            GangParticipant gangParticipant = ((GangTournament) tournament).getParticipantByName(args[2]);
            if (gangParticipant == null) {
                sender.sendMessage(ChatColor.RED + "The gang " + args[2] + " is not in this tournament");
                return;
            }

            gangParticipant.setScore(score);
            sender.sendMessage(ChatColor.GREEN + "Set the score of " + gangParticipant.getGangName() + " to " + Numbers.withSuffix(score) +
                    " in the gang tournament: " + tournament.getId());
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], tournamentManager.getActiveTournamentIDs(), list);
        }
        else if (args.length == 3) {
            Tournament tournament = tournamentManager.getActiveTournamentByID(args[1]);
            if (tournament == null) return list;

            StringUtil.copyPartialMatches(args[2], tournament.getParticipantNames(), list);
        }
        else if (args.length == 4) {
            list.add("score");
        }

        return list;
    }

}