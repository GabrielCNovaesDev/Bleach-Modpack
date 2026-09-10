package com.bleachmod.common.quest;

public enum QuestType {
    SAGA,
    SIDEQUEST;

    public static QuestType fromName(String name) {
        if (name == null) {
            return SIDEQUEST;
        }
        return QuestType.valueOf(name.toUpperCase());
    }
}
