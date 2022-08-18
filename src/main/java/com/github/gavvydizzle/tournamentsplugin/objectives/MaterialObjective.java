package com.github.gavvydizzle.tournamentsplugin.objectives;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaterialObjective extends Objective {

    private final Material material;

    public MaterialObjective(@NotNull ObjectiveType objectiveType, @NotNull Material material) {
        super (objectiveType);
        this.material = material;
    }

    @Override
    public boolean isMatch(@NotNull Objective o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof MaterialObjective)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        MaterialObjective obj = (MaterialObjective) o;

        return objectiveType == obj.objectiveType && material == obj.material;
    }

    @Override
    public String toString() {
        return objectiveType + " " + material.toString();
    }

}
