package com.github.gavvydizzle.tournamentsplugin.configs;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.TimeZone;

public class Configuration {

    public static SimpleDateFormat dateFormat;
    public static SimpleDateFormat prettyDateFormat;

    public static void reload() {
        FileConfiguration config = TournamentsPlugin.getInstance().getConfig();
        config.options().copyDefaults(true);
        config.addDefault("dateFormat", "yyyy-MM-dd HH:mm");
        config.addDefault("prettyDateFormat", "EEE, d MMM yyyy HH:mm z");
        config.addDefault("timeZone", TimeZone.getDefault().getID());
        TournamentsPlugin.getInstance().saveConfig();

        try {
            if (config.getString("dateFormat") == null || Objects.requireNonNull(config.getString("dateFormat")).trim().equals("")) {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            }
            else {
                dateFormat = new SimpleDateFormat(Objects.requireNonNull(config.getString("dateFormat")));
            }
        } catch (Exception e) {
            TournamentsPlugin.getInstance().getLogger().warning("The date format '" + config.getString("dateFormat") + "'" +
                    " cannot be used. You will be using the default date format: 'yyyy-MM-dd HH:mm'");
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }

        try {
            if (config.getString("prettyDateFormat") == null || Objects.requireNonNull(config.getString("prettyDateFormat")).trim().equals("")) {
                prettyDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm z");
            }
            else {
                prettyDateFormat = new SimpleDateFormat(Objects.requireNonNull(config.getString("prettyDateFormat")));
            }
        } catch (Exception e) {
            TournamentsPlugin.getInstance().getLogger().warning("The pretty date format '" + config.getString("prettyDateFormat") + "'" +
                    " cannot be used. You will be using the default pretty date format: 'EEE, d MMM yyyy HH:mm z'");
            prettyDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm z");
        }

        try {
            if (config.getString("timeZone") == null || Objects.requireNonNull(config.getString("timeZone")).trim().equals("")) {
                dateFormat.setTimeZone(TimeZone.getDefault());
                prettyDateFormat.setTimeZone(TimeZone.getDefault());
            }
            else {
                dateFormat.setTimeZone(TimeZone.getTimeZone(config.getString("timeZone")));
                prettyDateFormat.setTimeZone(TimeZone.getTimeZone(config.getString("timeZone")));
            }
        }
        catch (Exception e) {
            TournamentsPlugin.getInstance().getLogger().warning("The timezone '" + config.getString("timeZone") + "'" +
                    " could not be found. Your machine's default timezone will be used.");
            dateFormat.setTimeZone(TimeZone.getDefault());
            prettyDateFormat.setTimeZone(TimeZone.getDefault());
        }
    }

}
