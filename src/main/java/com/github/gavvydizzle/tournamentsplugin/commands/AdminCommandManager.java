package com.github.gavvydizzle.tournamentsplugin.commands;

import com.github.gavvydizzle.tournamentsplugin.commands.admincommands.*;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AdminCommandManager implements TabExecutor {

    private final ArrayList<SubCommand> subcommands = new ArrayList<>();
    private final ArrayList<String> subcommandStrings = new ArrayList<>();

    public AdminCommandManager() {
        subcommands.add(new AdminDebugCommand());
        subcommands.add(new AdminDeleteTournamentCommand());
        //subcommands.add(new AdminForceUpdateCommand()); // Not used now because leaderboard update every time they are opened
        subcommands.add(new AdminHelpCommand());
        subcommands.add(new AdminRegenerateTournamentDatabaseCommand());
        subcommands.add(new AdminReloadCommand());
        subcommands.add(new AdminReloadTournamentCommand());
        subcommands.add(new AdminSetScoreCommand());
        subcommands.add(new AdminTestTournamentRewardCommand());

        for (SubCommand subCommand : subcommands) {
            subcommandStrings.add(subCommand.getName());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0) {
            for (int i = 0; i < getSubcommands().size(); i++) {
                if (args[0].equalsIgnoreCase(getSubcommands().get(i).getName())) {

                    SubCommand subCommand = subcommands.get(i);
                    subCommand.perform(sender, args);
                    return true;
                }
            }
            sender.sendMessage(ChatColor.RED + "Invalid command");
        }
        sender.sendMessage(ChatColor.YELLOW + "Use '/tournadmin help' to see a list of valid commands");

        return true;
    }

    public ArrayList<SubCommand> getSubcommands(){
        return subcommands;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            ArrayList<String> subcommandsArguments = new ArrayList<>();

            StringUtil.copyPartialMatches(args[0], subcommandStrings, subcommandsArguments);

            return subcommandsArguments;
        }
        else if (args.length >= 2) {
            for (SubCommand subcommand : subcommands) {
                if (args[0].equalsIgnoreCase(subcommand.getName())) {
                    return subcommand.getSubcommandArguments((Player) sender, args);
                }
            }
        }

        return null;
    }
}