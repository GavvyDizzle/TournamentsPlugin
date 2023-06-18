package com.github.gavvydizzle.tournamentsplugin.commands.admincommands;

import com.github.gavvydizzle.tournamentsplugin.commands.AdminCommandManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminHelpCommand extends SubCommand {

    private final AdminCommandManager commandManager;

    public AdminHelpCommand(AdminCommandManager commandManager) {
        this.commandManager = commandManager;

        setName("help");
        setDescription("Opens this help menu");
        setSyntax("/" + commandManager.getCommandDisplayName() + " help");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(commandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        player.sendMessage("-----(Tournaments Admin Commands)-----");
        ArrayList<SubCommand> subCommands = commandManager.getSubcommands();
        for (SubCommand subCommand : subCommands) {
            player.sendMessage(ChatColor.GOLD + subCommand.getSyntax() + " - " + ChatColor.YELLOW + subCommand.getDescription());
        }
        player.sendMessage("-----(Tournaments Admin Commands)-----");
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}