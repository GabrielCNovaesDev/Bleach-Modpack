package com.bleachmod.common.evolution;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class FormGroup {
    private String groupName;
    private String formType;
    private final Map<String, FormData> forms = new LinkedHashMap<>();

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public Map<String, FormData> getForms() {
        return forms;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("groupName", groupName);
        json.addProperty("formType", formType);
        JsonObject formJson = new JsonObject();
        forms.forEach((name, data) -> formJson.add(name, data.toJson()));
        json.add("forms", formJson);
        return json;
    }
}
