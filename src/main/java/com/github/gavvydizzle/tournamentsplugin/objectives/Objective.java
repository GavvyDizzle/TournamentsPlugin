package com.github.gavvydizzle.tournamentsplugin.objectives;

import org.jetbrains.annotations.NotNull;

public class Objective {

    protected final ObjectiveType objectiveType;

    public Objective(@NotNull ObjectiveType objectiveType) {
        this.objectiveType = objectiveType;
    }

    /**
     * Determines if this objective is equal to another by checking the type
     * @param obj The objective to check this one against
     * @return True if the ObjectiveType matches for both
     */
    public boolean isMatch(@NotNull Objective obj) {
        return objectiveType == obj.objectiveType && !(obj instanceof MaterialObjective) && !(obj instanceof EntityTypeObjective);
    }

    @Override
    public String toString() {
        return objectiveType.toString();
    }

}