package com.bleachmod.common.data;

import com.bleachmod.Reference;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CharacterData {
    private final java.util.Set<String> unlockedForms = new java.util.HashSet<>();
    public void unlockForm(String form) { unlockedForms.add(form); }
    public boolean isFormDiscovered(String form) { return "sealed".equals(form) || unlockedForms.contains(form); }
    private String race = "";
    private String selectedFormGroup = "";
    private String selectedForm = "";
    private String activeFormGroup = "";
    private String activeForm = "";
    private final Map<String, Double> formMasteries = new HashMap<>();

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race == null ? "" : race.toLowerCase(Locale.ROOT);
    }

    public String getSelectedFormGroup() {
        return selectedFormGroup;
    }

    public String getSelectedForm() {
        return selectedForm;
    }

    public void setSelectedForm(String group, String form) {
        this.selectedFormGroup = group == null ? "" : group.toLowerCase(Locale.ROOT);
        this.selectedForm = form == null ? "" : form.toLowerCase(Locale.ROOT);
    }

    public String getActiveFormGroup() {
        return activeFormGroup;
    }

    public String getActiveForm() {
        return activeForm;
    }

    public void setActiveForm(String group, String form) {
        this.activeFormGroup = group == null ? "" : group.toLowerCase(Locale.ROOT);
        this.activeForm = form == null ? "" : form.toLowerCase(Locale.ROOT);
    }

    public boolean hasActiveForm() {
        return !activeForm.isEmpty();
    }

    public double getMastery(String group, String form) {
        return formMasteries.getOrDefault(masteryKey(group, form), 0.0D);
    }

    public void setMastery(String group, String form, double value) {
        formMasteries.put(masteryKey(group, form), Double.isFinite(value) ? Math.max(0.0D, Math.min(100.0D, value)) : 0.0D);
    }

    public void addMastery(String group, String form, double amount, double max) {
        double next = Math.min(max, getMastery(group, form) + amount);
        setMastery(group, form, next);
    }

    public void initializeShinigami() {
        unlockForm("sealed");
        setRace(Reference.RACE_SHINIGAMI);
        setSelectedForm(Reference.GROUP_ZANPAKUTO, Reference.FORM_SEALED);
        setActiveForm(Reference.GROUP_ZANPAKUTO, Reference.FORM_SEALED);
    }

    public CompoundTag saveAppearance() {
        CompoundTag tag = new CompoundTag();
        tag.putString("race", race);
        tag.putString("activeFormGroup", activeFormGroup);
        tag.putString("activeForm", activeForm);
        return tag;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("race", race);
        tag.putString("selectedFormGroup", selectedFormGroup);
        tag.putString("selectedForm", selectedForm);
        tag.putString("activeFormGroup", activeFormGroup);
        tag.putString("activeForm", activeForm);
        CompoundTag masteries = new CompoundTag();
        formMasteries.forEach(masteries::putDouble);
        tag.put("formMasteries", masteries);
        CompoundTag unlocks = new CompoundTag();
        unlockedForms.forEach(form -> unlocks.putBoolean(form, true));
        tag.put("unlockedForms", unlocks);
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains("unlockedForms")) { unlockedForms.clear(); unlockedForms.addAll(tag.getCompound("unlockedForms").getAllKeys()); }
        if (tag.contains("race")) {
            race = tag.getString("race");
        }
        if (tag.contains("selectedFormGroup")) {
            selectedFormGroup = tag.getString("selectedFormGroup");
        }
        if (tag.contains("selectedForm")) {
            selectedForm = tag.getString("selectedForm");
        }
        if (tag.contains("activeFormGroup")) {
            activeFormGroup = tag.getString("activeFormGroup");
        }
        if (tag.contains("activeForm")) {
            activeForm = tag.getString("activeForm");
        }
        if (tag.contains("formMasteries")) {
            formMasteries.clear();
            CompoundTag masteries = tag.getCompound("formMasteries");
            for (String key : masteries.getAllKeys()) {
                formMasteries.put(key, masteries.getDouble(key));
            }
        }
    }

    private static String masteryKey(String group, String form) {
        return (group == null ? "" : group.toLowerCase(Locale.ROOT)) + ":"
                + (form == null ? "" : form.toLowerCase(Locale.ROOT));
    }
}
