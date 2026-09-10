package com.bleachmod.common.quest;

import com.google.gson.JsonObject;

public class Saga {
    private String id;
    private String name;
    private String previousSaga = "";
    private String questFolder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreviousSaga() {
        return previousSaga;
    }

    public void setPreviousSaga(String previousSaga) {
        this.previousSaga = previousSaga == null ? "" : previousSaga;
    }

    public String getQuestFolder() {
        return questFolder;
    }

    public void setQuestFolder(String questFolder) {
        this.questFolder = questFolder;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name);
        JsonObject requirements = new JsonObject();
        requirements.addProperty("previousSaga", previousSaga);
        json.add("requirements", requirements);
        json.addProperty("questFolder", questFolder);
        return json;
    }

    public static Saga fromJson(JsonObject json) {
        Saga saga = new Saga();
        saga.setId(json.get("id").getAsString());
        saga.setName(json.get("name").getAsString());
        if (json.has("requirements") && json.getAsJsonObject("requirements").has("previousSaga")) {
            saga.setPreviousSaga(json.getAsJsonObject("requirements").get("previousSaga").getAsString());
        }
        saga.setQuestFolder(json.get("questFolder").getAsString());
        return saga;
    }
}
