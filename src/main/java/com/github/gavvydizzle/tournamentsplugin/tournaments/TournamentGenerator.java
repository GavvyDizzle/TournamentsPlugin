package com.github.gavvydizzle.tournamentsplugin.tournaments;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.configs.Configuration;
import com.github.gavvydizzle.tournamentsplugin.objectives.Objective;
import com.github.gavvydizzle.tournamentsplugin.rewards.TournamentRewards;
import com.github.gavvydizzle.tournamentsplugin.utils.Utils;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;

public class TournamentGenerator {

    ArrayList<String> tournamentIds;
    private final HashSet<String> blacklistedIds;
    private final ArrayList<Tournament> tournaments;

    public TournamentGenerator(File file) {
        tournamentIds = new ArrayList<>();
        this.blacklistedIds = new HashSet<>();
        this.tournaments = new ArrayList<>();

        createFolder();
        parseFolderAndLoadConfigs(file);
        parseFolderAndLoadBlacklist(file);
        parseFolderAndLoadTournaments(file);
    }

    private void createFolder() {
        File folder = new File(TournamentsPlugin.getInstance().getDataFolder(), "tournaments");
        folder.mkdir();
    }

    private void parseFolderAndLoadConfigs(final File folder) {
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                parseFolderAndLoadConfigs(fileEntry);
            }
            else {
                if (!fileEntry.getName().endsWith(".yml")) continue;

                try {
                    generateDefaultConfigs(fileEntry);
                } catch (Exception e) {
                    TournamentsPlugin.getInstance().getLogger().severe("Failed to load tournament: " + fileEntry.getName() + "!");
                    e.printStackTrace();
                }
            }
        }
    }

    private void parseFolderAndLoadBlacklist(final File folder) {
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                parseFolderAndLoadBlacklist(fileEntry);
            }
            else {
                if (!fileEntry.getName().endsWith(".yml")) continue;

                try {
                    generateBlacklist(fileEntry);
                } catch (Exception e) {
                    TournamentsPlugin.getInstance().getLogger().severe("Failed to load tournament: " + fileEntry.getName() + "!");
                    e.printStackTrace();
                }
            }
        }
    }

    private void parseFolderAndLoadTournaments(final File folder) {
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                parseFolderAndLoadTournaments(fileEntry);
            }
            else {
                if (!fileEntry.getName().endsWith(".yml")) continue;

                try {
                    Tournament tournament = generateTournament(fileEntry);
                    if (tournament != null) tournaments.add(tournament);
                } catch (Exception e) {
                    TournamentsPlugin.getInstance().getLogger().severe("Failed to load tournament: " + fileEntry.getName() + "!");
                    e.printStackTrace();
                }
            }
        }
    }

    private void generateDefaultConfigs(File file) {
        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.addDefault("id", "");
        config.addDefault("displayName", "name_me_please");
        config.addDefault("type", "INDIVIDUAL");

        Date date = new Date(System.currentTimeMillis() + 86400000);
        config.addDefault("startDate", Configuration.dateFormat.format(date));
        date.setTime(date.toInstant().getEpochSecond() + 604800000);
        config.addDefault("endDate", Configuration.dateFormat.format(date));

        config.addDefault("objective.type", "MINE");
        config.addDefault("objective.material", "STONE");
        config.addDefault("objective.entityType", "");

        try {
            config.save(file);
        }
        catch (IOException e) {
            System.out.println("Could not save file");
            e.printStackTrace();
        }
    }

    private void generateBlacklist(File file) {
        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        String id = config.getString("id");
        if (id == null || id.trim().isEmpty()) {
            TournamentsPlugin.getInstance().getLogger().warning("The id is empty in " + file.getName() + ". Please define it!");
            return;
        }

        if (tournamentIds.contains(id)) {
            blacklistedIds.add(id);
        }
        else {
            tournamentIds.add(id);
        }
    }

    /**
     * Generated a tournament from the config file provided.
     * @param file The yaml file to parse
     * @return The tournament if created successfully, null otherwise.
     */
    private Tournament generateTournament(File file) {
        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        TournamentType tournamentType = TournamentType.getTournamentType(config.getString("type"));
        if (tournamentType == null) {
            TournamentsPlugin.getInstance().getLogger().warning("Invalid tournament type given in " + file.getName() + ". The only valid types are INDIVIDUAL and GANG");
            return null;
        }

        String id = config.getString("id");
        if (id == null || id.trim().isEmpty()) {
            TournamentsPlugin.getInstance().getLogger().warning("The id is empty in " + file.getName() + ". Please define it!");
            return null;
        }

        // Check for duplicate ID
        if (blacklistedIds.contains(id)) {
            TournamentsPlugin.getInstance().getLogger().warning("The id '" + id + "' defined in " + file.getName() + " is duplicated elsewhere. To be safe, all tournaments with this id will not be loaded.");
            return null;
        }

        String displayName = Colors.conv(config.getString("displayName"));

        Date startDate, endDate;
        Objective objective;
        TournamentRewards tournamentReward;

        try {
            startDate = Configuration.dateFormat.parse(config.getString("startDate"));
            endDate = Configuration.dateFormat.parse(config.getString("endDate"));

            objective = Utils.generateObjective(
                    config.getString("objective.type"),
                    config.getString("objective.material"),
                    config.getString("objective.entityType")
            );
            if (objective == null) {
                TournamentsPlugin.getInstance().getLogger().warning("Failed to generate the objective in " + file.getName());
                return null;
            }

            tournamentReward = new TournamentRewards(config);

        } catch (Exception e) {
            TournamentsPlugin.getInstance().getLogger().severe("Failed to load the Tournament under " + file.getName());
            e.printStackTrace();
            return null;
        }

        ItemStack pending, active, completed;

        pending = new ItemStack(ConfigUtils.getMaterial(config.getString("gui.pendingItem.material")));
        ItemMeta meta = pending.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("gui.pendingItem.name")));
        meta.setLore(Colors.conv(config.getStringList("gui.pendingItem.lore")));
        if (config.getBoolean("gui.pendingItem.isGlowing")) {
            meta.addEnchant(Enchantment.ARROW_FIRE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        pending.setItemMeta(meta);

        active = new ItemStack(ConfigUtils.getMaterial(config.getString("gui.activeItem.material")));
        meta = active.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("gui.activeItem.name")));
        meta.setLore(Colors.conv(config.getStringList("gui.activeItem.lore")));
        if (config.getBoolean("gui.activeItem.isGlowing")) {
            meta.addEnchant(Enchantment.ARROW_FIRE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        active.setItemMeta(meta);

        completed = new ItemStack(ConfigUtils.getMaterial(config.getString("gui.completedItem.material")));
        meta = completed.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("gui.completedItem.name")));
        meta.setLore(Colors.conv(config.getStringList("gui.completedItem.lore")));
        if (config.getBoolean("gui.completedItem.isGlowing")) {
            meta.addEnchant(Enchantment.ARROW_FIRE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        completed.setItemMeta(meta);

        if (tournamentType == TournamentType.INDIVIDUAL) {
            return new IndividualTournament(id, displayName, startDate, endDate, objective, tournamentReward, pending, active, completed, file);
        }
        else if (tournamentType == TournamentType.GANG) {
            return new GangTournament(id, displayName, startDate, endDate, objective, tournamentReward, pending, active, completed, file);
        }

        return null;
    }




    public ArrayList<Tournament> getTournaments() {
        return tournaments;
    }
}
