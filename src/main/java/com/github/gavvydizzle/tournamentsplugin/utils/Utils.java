package com.github.gavvydizzle.tournamentsplugin.utils;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.objectives.EntityTypeObjective;
import com.github.gavvydizzle.tournamentsplugin.objectives.MaterialObjective;
import com.github.gavvydizzle.tournamentsplugin.objectives.Objective;
import com.github.gavvydizzle.tournamentsplugin.objectives.ObjectiveType;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class Utils {

    public static Objective generateObjective(String type, String material, String entityType) {
        if (material == null) material = "";
        if (entityType == null) entityType = "";

        ObjectiveType objectiveType = ObjectiveType.getObjectiveType(type);
        if (objectiveType == null) {
            TournamentsPlugin.getInstance().getLogger().warning("The objective type " + type.toUpperCase() + " is not a valid type!");
            return null;
        }

        if (material.trim().isEmpty() && entityType.trim().isEmpty()) {
            return new Objective(objectiveType);
        }

        if (!material.trim().isEmpty() && !entityType.trim().isEmpty()) {
            TournamentsPlugin.getInstance().getLogger().warning("You can only define the material OR entityType for an objective!");
            return null;
        }
        else if (!material.trim().isEmpty()) {
            Material mat = Material.getMaterial(material.toUpperCase());
            if (mat == null) {
                TournamentsPlugin.getInstance().getLogger().warning("The material " + material.toUpperCase() + " is not a valid type!");
                return null;
            }
            return new MaterialObjective(objectiveType, mat);
        }
        else {
            try {
                EntityType et = EntityType.valueOf(material.toUpperCase());
                return new EntityTypeObjective(objectiveType, et);
            } catch (Exception e) {
                TournamentsPlugin.getInstance().getLogger().warning("The entityType " + entityType.toUpperCase() + " is not a valid type!");
                return null;
            }
        }
    }

}
