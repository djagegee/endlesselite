package de.shadow.endlessbook;

public final class QuestEntitlement {
    public static final String REQUIRED_QUEST = "getting_started";
    private QuestEntitlement() {}
    public static boolean mayOwnBook(boolean questCompleted) { return questCompleted; }
}
