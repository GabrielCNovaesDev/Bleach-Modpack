package com.bleachmod.common.evolution;

import com.bleachmod.Reference;
import com.bleachmod.common.data.PlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TransformationsHelper {
    private TransformationsHelper() {
    }

    public static boolean isUnlocked(PlayerData data, String group, String form) {
        if (data.getCharacter().getRace().isEmpty()) {
            return false;
        }
        FormGroup formGroup = FormRegistry.getGroup(data.getCharacter().getRace(), group);
        if (formGroup == null) {
            return false;
        }
        FormData formData = formGroup.getForms().get(form.toLowerCase(Locale.ROOT));
        if (formData == null) {
            return false;
        }
        String skillName = formGroup.getFormType() == null || formGroup.getFormType().isBlank()
                ? group : formGroup.getFormType();
        if (!data.getSkills().isUnlockedAtLevel(skillName, formData.getUnlockOnSkillLevel())) {
            return false;
        }
        return meetsMasteryRequisites(data, formData);
    }

    public static boolean isSelectable(PlayerData data, String group, String form) {
        List<String> selectable = getSelectableFormNames(data, group);
        return selectable.contains(form.toLowerCase(Locale.ROOT));
    }

    public static List<FormData> getUnlockedForms(PlayerData data, String group) {
        List<FormData> unlocked = new ArrayList<>();
        FormGroup formGroup = FormRegistry.getGroup(data.getCharacter().getRace(), group);
        if (formGroup == null) {
            return unlocked;
        }
        for (FormData form : formGroup.getForms().values()) {
            if (isUnlocked(data, group, form.getName())) {
                unlocked.add(form);
            }
        }
        return unlocked;
    }

    public static List<String> getSelectableFormNames(PlayerData data, String group) {
        List<FormData> unlocked = getUnlockedForms(data, group);
        List<String> names = new ArrayList<>();
        if (unlocked.isEmpty()) {
            return names;
        }
        names.add(unlocked.get(0).getName().toLowerCase(Locale.ROOT));
        String active = data.getCharacter().getActiveForm();
        int activeIndex = -1;
        for (int i = 0; i < unlocked.size(); i++) {
            if (unlocked.get(i).getName().equalsIgnoreCase(active)) {
                activeIndex = i;
                break;
            }
        }
        for (int i = 1; i < unlocked.size(); i++) {
            FormData form = unlocked.get(i);
            double mastery = data.getCharacter().getMastery(group, form.getName());
            boolean nextInChain = i == activeIndex + 1 || (activeIndex < 0 && i == 1);
            if (nextInChain || mastery >= form.getAllowFreeTransformOnMastery()) {
                names.add(form.getName().toLowerCase(Locale.ROOT));
            }
        }
        return names;
    }

    public static FormData getSelectedFormData(PlayerData data) {
        String group = data.getCharacter().getSelectedFormGroup();
        String form = data.getCharacter().getSelectedForm();
        if (group.isEmpty() || form.isEmpty()) {
            group = Reference.GROUP_ZANPAKUTO;
            form = Reference.FORM_SEALED;
        }
        return FormRegistry.getForm(data.getCharacter().getRace(), group, form);
    }

    public static FormData getActiveFormData(PlayerData data) {
        return FormRegistry.getForm(data.getCharacter().getRace(),
                data.getCharacter().getActiveFormGroup(),
                data.getCharacter().getActiveForm());
    }

    public static boolean needsFreeTransformMastery(PlayerData data) {
        String selectedGroup = data.getCharacter().getSelectedFormGroup();
        String activeGroup = data.getCharacter().getActiveFormGroup();
        if (selectedGroup.equalsIgnoreCase(activeGroup) || selectedGroup.isEmpty()) {
            return false;
        }
        FormData selected = getSelectedFormData(data);
        return selected != null && data.getCharacter().getMastery(selectedGroup, selected.getName()) < selected.getAllowFreeTransformOnMastery();
    }

    public static List<String> orderedFormNames(String race, String group) {
        FormGroup formGroup = FormRegistry.getGroup(race, group);
        if (formGroup == null) {
            return List.of();
        }
        return new ArrayList<>(formGroup.getForms().keySet());
    }

    public static String previousForm(PlayerData data) {
        List<String> names = orderedFormNames(data.getCharacter().getRace(), data.getCharacter().getActiveFormGroup());
        String active = data.getCharacter().getActiveForm();
        int index = names.indexOf(active);
        if (index <= 0) {
            return Reference.FORM_SEALED;
        }
        return names.get(index - 1);
    }

    private static boolean meetsMasteryRequisites(PlayerData data, FormData form) {
        if (form.getFormRequisite() == null || form.getFormRequisite().isBlank()) {
            return true;
        }
        String[] tokens = form.getFormRequisite().split(",");
        boolean all = !"any".equalsIgnoreCase(form.getFormRequisiteType());
        boolean anyMet = false;
        for (String token : tokens) {
            String trimmed = token.trim();
            int dot = trimmed.indexOf('.');
            if (dot < 0) {
                continue;
            }
            String group = trimmed.substring(0, dot);
            String name = trimmed.substring(dot + 1);
            boolean met = data.getCharacter().getMastery(group, name) >= form.getUnlockOnMastery();
            if (all && !met) {
                return false;
            }
            anyMet |= met;
        }
        return all || anyMet;
    }
}
