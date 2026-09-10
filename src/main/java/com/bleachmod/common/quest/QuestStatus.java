package com.bleachmod.common.quest;

public enum QuestStatus {
    NOT_STARTED(0),
    FAILED(1),
    ACCEPTED(2),
    SUCCESS(3);

    private final int rank;

    QuestStatus(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }

    public static QuestStatus fromName(String name) {
        if (name == null || name.isBlank()) {
            return NOT_STARTED;
        }
        try {
            return QuestStatus.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return NOT_STARTED;
        }
    }
}
