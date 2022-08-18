package com.github.gavvydizzle.tournamentsplugin.objectives;

public enum ObjectiveType {
    CRAFT,
    EAT,
    EARN,
    FISH,
    KILL,
    MINE,
    MINE_RAW,
    SELL_ITEM,
    SMELT;

    public static ObjectiveType getObjectiveType(String str) {
        for (ObjectiveType objectiveType : ObjectiveType.values()) {
            if (objectiveType.toString().equalsIgnoreCase(str)) {
                return objectiveType;
            }
        }
        return null;
    }
}
