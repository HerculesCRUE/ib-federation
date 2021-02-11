package es.um.asio.service.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import jdk.jshell.execution.Util;
import lombok.*;
import util.Utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TripleObjectSimplified {

    private String id;
    private String localURI;
    private String className;
    private String node;
    private String tripleStore;
    private long lastModification;
    private LinkedTreeMap<String, Object> attributes;

    public TripleObjectSimplified(String nodeName, String tripleStore, String className, String id) {
        this.node = nodeName;
        this.tripleStore = tripleStore;
        this.className = className;
        this.id = id;
        this.attributes = new LinkedTreeMap<>();
    }

    public TripleObjectSimplified(JsonObject jsonObject) {
        if (jsonObject.has("id"))
            this.id = jsonObject.get("id").getAsString();
        if (jsonObject.has("localURI"))
            this.localURI = jsonObject.get("localURI").getAsString();
        if (jsonObject.has("className"))
            this.className = jsonObject.get("className").getAsString();
        if (jsonObject.has("tripleStore")) {
            if (jsonObject.get("tripleStore").getAsJsonObject().has("name")) {
                this.tripleStore = jsonObject.get("tripleStore").getAsJsonObject().get("name").getAsString();
            }
            if (jsonObject.get("tripleStore").getAsJsonObject().has("node")) {
                if (jsonObject.get("tripleStore").getAsJsonObject().get("node").getAsJsonObject().has("nodeName")) {
                    this.node = jsonObject.get("tripleStore").getAsJsonObject().get("node").getAsJsonObject().get("nodeName").getAsString();
                }
            }
        }
        if (jsonObject.has("lastModification"))
            this.lastModification = jsonObject.get("lastModification").getAsLong();
        if (jsonObject.has("attributes"))
            this.attributes = new Gson().fromJson(jsonObject.get("attributes").getAsJsonObject().toString(), LinkedTreeMap.class);
    }

    public Object getAttributeValue(LinkedTreeMap<String, Object> attributes, String key) {
        for (Map.Entry<String, Object> eAtt : attributes.entrySet()) {
            if (eAtt.getKey().equalsIgnoreCase(key)) {
                return eAtt.getValue();
            } else if (!Utils.isPrimitive(eAtt.getValue())) {
                Object o = getAttributeValue((LinkedTreeMap<String, Object>) eAtt.getValue(),key);
                if (o!=null)
                    return o;
            }
        }
        return null;
    }

    public Object getAttributeValue(LinkedTreeMap<String, Object> attributes, List<String> keys) {
        for (Map.Entry<String, Object> eAtt : attributes.entrySet()) {
            if (keys.contains(eAtt.getKey())) {
                return eAtt.getValue();
            } else if (!Utils.isPrimitive(eAtt.getValue())) {
                Object o = getAttributeValue((LinkedTreeMap<String, Object>) eAtt.getValue(),keys);
                if (o!=null)
                    return o;
            }
        }
        return null;
    }

    public void addAttribute(String key, String value) {
        if (key.equals("id")) {
            key = "localId";
        }
        attributes.put(key, Utils.isValidString(value)?value:null);
    }
}
