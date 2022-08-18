package com.github.gavvydizzle.tournamentsplugin.objectives;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class EntityTypeObjective extends Objective {

    private final EntityType entityType;

    public EntityTypeObjective(@NotNull ObjectiveType objectiveType, @NotNull EntityType entityType) {
        super(objectiveType);
        this.entityType = entityType;
    }

    @Override
    public boolean isMatch(@NotNull Objective o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof EntityTypeObjective)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        EntityTypeObjective obj = (EntityTypeObjective) o;

        return objectiveType == obj.objectiveType && entityType == obj.entityType;
    }

    @Override
    public String toString() {
        return objectiveType + " " + entityType.toString();
    }

}
