package com.bleachmod.common.evolution;

import com.google.gson.JsonObject;

public class FormData {
    private String name;
    private int unlockOnSkillLevel;
    private String formRequisite = "";
    private String formRequisiteType = "all";
    private double unlockOnMastery;
    private double instantTransformOnMastery = 40.0D;
    private double allowFreeTransformOnMastery = 50.0D;
    private double energyDrain;
    private double maxMastery = 100.0D;
    private double passiveMasteryEveryFiveSeconds = 1.0D;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUnlockOnSkillLevel() {
        return unlockOnSkillLevel;
    }

    public void setUnlockOnSkillLevel(int unlockOnSkillLevel) {
        this.unlockOnSkillLevel = unlockOnSkillLevel;
    }

    public String getFormRequisite() {
        return formRequisite;
    }

    public void setFormRequisite(String formRequisite) {
        this.formRequisite = formRequisite == null ? "" : formRequisite;
    }

    public String getFormRequisiteType() {
        return formRequisiteType;
    }

    public void setFormRequisiteType(String formRequisiteType) {
        this.formRequisiteType = formRequisiteType == null ? "all" : formRequisiteType;
    }

    public double getUnlockOnMastery() {
        return unlockOnMastery;
    }

    public void setUnlockOnMastery(double unlockOnMastery) {
        this.unlockOnMastery = unlockOnMastery;
    }

    public double getInstantTransformOnMastery() {
        return instantTransformOnMastery;
    }

    public void setInstantTransformOnMastery(double instantTransformOnMastery) {
        this.instantTransformOnMastery = instantTransformOnMastery;
    }

    public double getAllowFreeTransformOnMastery() {
        return allowFreeTransformOnMastery;
    }

    public void setAllowFreeTransformOnMastery(double allowFreeTransformOnMastery) {
        this.allowFreeTransformOnMastery = allowFreeTransformOnMastery;
    }

    public double getEnergyDrain() {
        return energyDrain;
    }

    public void setEnergyDrain(double energyDrain) {
        this.energyDrain = energyDrain;
    }

    public double getMaxMastery() {
        return maxMastery;
    }

    public void setMaxMastery(double maxMastery) {
        this.maxMastery = maxMastery;
    }

    public double getPassiveMasteryEveryFiveSeconds() {
        return passiveMasteryEveryFiveSeconds;
    }

    public void setPassiveMasteryEveryFiveSeconds(double passiveMasteryEveryFiveSeconds) {
        this.passiveMasteryEveryFiveSeconds = passiveMasteryEveryFiveSeconds;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("unlockOnSkillLevel", unlockOnSkillLevel);
        json.addProperty("formRequisite", formRequisite);
        json.addProperty("formRequisiteType", formRequisiteType);
        json.addProperty("unlockOnMastery", unlockOnMastery);
        json.addProperty("instantTransformOnMastery", instantTransformOnMastery);
        json.addProperty("allowFreeTransformOnMastery", allowFreeTransformOnMastery);
        json.addProperty("energyDrain", energyDrain);
        json.addProperty("maxMastery", maxMastery);
        json.addProperty("passiveMasteryEveryFiveSeconds", passiveMasteryEveryFiveSeconds);
        return json;
    }

    public static FormData fromJson(String fallbackName, JsonObject json) {
        FormData data = new FormData();
        data.setName(json.has("name") ? json.get("name").getAsString() : fallbackName);
        if (json.has("unlockOnSkillLevel")) {
            data.setUnlockOnSkillLevel(json.get("unlockOnSkillLevel").getAsInt());
        }
        if (json.has("formRequisite")) {
            data.setFormRequisite(json.get("formRequisite").getAsString());
        }
        if (json.has("formRequisiteType")) {
            data.setFormRequisiteType(json.get("formRequisiteType").getAsString());
        }
        if (json.has("unlockOnMastery")) {
            data.setUnlockOnMastery(json.get("unlockOnMastery").getAsDouble());
        }
        if (json.has("instantTransformOnMastery")) {
            data.setInstantTransformOnMastery(json.get("instantTransformOnMastery").getAsDouble());
        }
        if (json.has("allowFreeTransformOnMastery")) {
            data.setAllowFreeTransformOnMastery(json.get("allowFreeTransformOnMastery").getAsDouble());
        }
        if (json.has("energyDrain")) {
            data.setEnergyDrain(json.get("energyDrain").getAsDouble());
        }
        if (json.has("maxMastery")) {
            data.setMaxMastery(json.get("maxMastery").getAsDouble());
        }
        if (json.has("passiveMasteryEveryFiveSeconds")) {
            data.setPassiveMasteryEveryFiveSeconds(json.get("passiveMasteryEveryFiveSeconds").getAsDouble());
        }
        return data;
    }
}
