package com.github.gavvydizzle.tournamentsplugin.commands.admincommands;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.configs.Configuration;
import com.github.gavvydizzle.tournamentsplugin.configs.ObjectivesConfig;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminReloadCommand extends SubCommand {

    private final List<String> subReloadList = Arrays.asList("disabledWorlds", "gui", "tournaments");

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads this plugin";
    }

    @Override
    public String getSyntax() {
        return "/tournadmin reload";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        TournamentsPlugin.getInstance().reloadConfig();

        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("disabledWorlds")) {
                Configuration.reload();
                reloadDisabledWorlds();
            }
            else if (args[1].equalsIgnoreCase("gui")) {
                Configuration.reload();
                reloadGUIs();
                TournamentsPlugin.getInstance().getInventoryManager().getTournamentListManager().reloadLists();
            }
            else if (args[1].equalsIgnoreCase("tournaments")) {
                Configuration.reload();
                reloadTournaments();
            }
            else {
                sender.sendMessage(ChatColor.RED + "Invalid sub-argument. Nothing was reloaded");
            }
        }
        else {
            Configuration.reload();
            reloadGUIs();
            reloadTournaments();
            reloadDisabledWorlds();
        }
        sender.sendMessage(ChatColor.GREEN + "[TournamentsPlugin] Reloaded");
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        ArrayList<String> list = new ArrayList<>();
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], subReloadList, list);
        }
        return list;
    }

    private void reloadDisabledWorlds() {
        ObjectivesConfig.reload();
        TournamentsPlugin.getInstance().getObjectiveManager().reload();
    }

    private void reloadGUIs() {
        TournamentsPlugin.getInstance().getInventoryManager().reload();
    }

    private void reloadTournaments() {
        TournamentsPlugin.getInstance().getTournamentManager().reloadTournaments();
        TournamentsPlugin.getInstance().getInventoryManager().getTournamentListManager().reloadLists();
    }

}
