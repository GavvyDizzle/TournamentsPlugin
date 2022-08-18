package com.github.gavvydizzle.tournamentsplugin.commands.admincommands;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminDebugCommand extends SubCommand {

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public String getDescription() {
        return "Toggles objective debugging";
    }

    @Override
    public String getSyntax() {
        return "/tournadmin debug";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        TournamentsPlugin.getInstance().getObjectiveManager().toggleDebugMode(sender);
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return new ArrayList<>();
    }

}