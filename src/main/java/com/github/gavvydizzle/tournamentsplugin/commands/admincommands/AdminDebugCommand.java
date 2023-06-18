package com.github.gavvydizzle.tournamentsplugin.commands.admincommands;

import com.github.gavvydizzle.tournamentsplugin.commands.AdminCommandManager;
import com.github.gavvydizzle.tournamentsplugin.objectives.ObjectiveManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class AdminDebugCommand extends SubCommand {

    private final ObjectiveManager objectiveManager;

    public AdminDebugCommand(AdminCommandManager commandManager, ObjectiveManager objectiveManager) {
        this.objectiveManager = objectiveManager;

        setName("debug");
        setDescription("Toggles objective debugging");
        setSyntax("/" + commandManager.getCommandDisplayName() + " debug");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(commandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        objectiveManager.toggleDebugMode(sender);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}