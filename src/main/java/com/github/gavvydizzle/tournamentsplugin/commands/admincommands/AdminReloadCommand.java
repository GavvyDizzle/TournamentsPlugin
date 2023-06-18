package com.github.gavvydizzle.tournamentsplugin.commands.admincommands;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.commands.AdminCommandManager;
import com.github.gavvydizzle.tournamentsplugin.configs.Configuration;
import com.github.gavvydizzle.tournamentsplugin.configs.ObjectivesConfig;
import com.github.gavvydizzle.tournamentsplugin.tournaments.TournamentManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminReloadCommand extends SubCommand {

    private final List<String> subReloadList = Arrays.asList("disabledWorlds", "gui", "tournaments");
    private final TournamentManager tournamentManager;

    public AdminReloadCommand(AdminCommandManager commandManager, TournamentManager tournamentManager) {
        this.tournamentManager = tournamentManager;

        setName("reload");
        setDescription("Reloads this plugin");
        setSyntax("/" + commandManager.getCommandDisplayName() + " reload [arg]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(commandManager.getPermissionPrefix() + getName().toLowerCase());
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
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
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
        tournamentManager.reloadTournaments();
        TournamentsPlugin.getInstance().getInventoryManager().getTournamentListManager().reloadLists();
    }

}
