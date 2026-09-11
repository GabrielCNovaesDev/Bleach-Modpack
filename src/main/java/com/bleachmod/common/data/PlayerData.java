package com.bleachmod.common.data;

import com.bleachmod.Reference;
import com.bleachmod.common.quest.PlayerQuestData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class PlayerData {
    private final CharacterData character = new CharacterData();
    private final ResourcesData resources = new ResourcesData();
    private final SkillsData skills = new SkillsData();
    private final StatusData status = new StatusData();
    private final PlayerQuestData playerQuestData = new PlayerQuestData();
    private boolean dataLoaded;
    private final AttributeData attributes = new AttributeData();
    public AttributeData getAttributes() { return attributes; }
    public void refreshDerivedResources() {
        double derived = Reference.BASE_REIATSU + 20.0D * attributes.level(AttributeData.RESERVE);
        resources.setMaxReiatsu((float)Math.min(Float.MAX_VALUE, derived));
    }

    public double getBattlePower() {
        long totalRanks = 0;
        for (String id : AttributeData.IDS) totalRanks += attributes.level(id);
        return totalRanks * (resources.getMaxReiatsu() / 10.0D);
    }

    public CharacterData getCharacter() {
        return character;
    }

    public ResourcesData getResources() {
        return resources;
    }

    public SkillsData getSkills() {
        return skills;
    }

    public StatusData getStatus() {
        return status;
    }

    public PlayerQuestData getPlayerQuestData() {
        return playerQuestData;
    }

    public boolean isDataLoaded() {
        return dataLoaded;
    }

    public void setDataLoaded(boolean dataLoaded) {
        this.dataLoaded = dataLoaded;
    }

    public void updateTransformationSkillLimits(String race) {
        if (Reference.RACE_SHINIGAMI.equals(race) || race == null || race.isBlank()) {
            skills.registerOrUpdate(Reference.SKILL_ZANPAKUTO, Reference.ZANPAKUTO_MAX_LEVEL);
        }
    }

    public void initializeShinigami() {
        character.initializeShinigami();
        status.setHasCreatedCharacter(true);
        updateTransformationSkillLimits(Reference.RACE_SHINIGAMI);
        resources.setMaxReiatsu(Reference.BASE_REIATSU);
        resources.setCurrentReiatsu(Reference.BASE_REIATSU);
        resources.setActionCharge(0);
    }

    public void resetTransientState() {
        resources.setActionCharge(0);
        status.setActionCharging(false);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schemaVersion", 3);
        tag.put("attributes", attributes.save());
        tag.put("character", character.save());
        tag.put("resources", resources.save());
        tag.put("skills", skills.save());
        tag.put("status", status.save());
        tag.put("playerQuestData", playerQuestData.save());
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains("character")) {
            character.load(tag.getCompound("character"));
        }
        if (tag.contains("resources")) {
            resources.load(tag.getCompound("resources"));
        }
        if (tag.contains("skills")) {
            skills.load(tag.getCompound("skills"));
        }
        if (tag.contains("status")) {
            status.load(tag.getCompound("status"));
        }
        if (tag.contains("playerQuestData")) {
            playerQuestData.load(tag.getCompound("playerQuestData"));
        }
        if (tag.contains("attributes")) attributes.load(tag.getCompound("attributes"));
        updateTransformationSkillLimits(character.getRace());
        if (tag.contains("character") && !tag.getCompound("character").contains("unlockedForms")) {
            character.unlockForm("sealed");
            if (skills.getLevel("zanpakuto") >= 1 || character.getMastery("zanpakuto", "shikai") > 0) character.unlockForm("shikai");
            if (skills.getLevel("zanpakuto") >= 2 || character.getMastery("zanpakuto", "bankai") > 0) character.unlockForm("bankai");
        }
        dataLoaded = true;
    }

    public void copyFrom(PlayerData other) {
        load(other.save());
    }

    public static Optional<PlayerData> get(ServerPlayer player) {
        return player.getCapability(PlayerCapability.INSTANCE).resolve();
    }
}
