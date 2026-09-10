package com.bleachmod.common.quest.objectives;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;

public abstract class QuestObjective {
    private final ObjectiveType type;
    private final int required;

    protected QuestObjective(ObjectiveType type, int required) {
        this.type = type;
        this.required = Math.max(1, required);
    }

    public ObjectiveType getType() {
        return type;
    }

    public int getRequired() {
        return required;
    }

    public abstract Component describe();

    public abstract JsonObject toJson();

    public enum ObjectiveType {
        KILL,
        ITEM
    }
}
