package es.um.asio.service.model;

import com.google.gson.JsonObject;
import jdk.jshell.execution.Util;
import lombok.*;
import util.Utils;

import java.util.LinkedHashMap;

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
    private LinkedHashMap<String, Object> attributes;

    public TripleObjectSimplified(String nodeName, String tripleStore, String className, String id) {
        this.node = nodeName;
        this.tripleStore = tripleStore;
        this.className = className;
        this.id = id;
        this.attributes = new LinkedHashMap<>();
    }

    public void addAttribute(String key, String value) {
        if (key.equals("id")) {
            key = "localId";
        }
        attributes.put(key, Utils.isValidString(value)?value:null);
    }
}
