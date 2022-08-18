package com.github.gavvydizzle.tournamentsplugin.storage;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;

import java.util.logging.Level;

public class Error {
    public static void execute(TournamentsPlugin plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    public static void close(TournamentsPlugin plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}
