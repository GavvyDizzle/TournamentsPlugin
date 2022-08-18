package com.github.gavvydizzle.tournamentsplugin.commands;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerCommandManager implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (!(sender instanceof Player)) return true;

        TournamentsPlugin.getInstance().getInventoryManager().openTopInventory((Player) sender);

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        return new ArrayList<>();
    }
}