package es.um.asio.service.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import es.um.asio.service.config.LodDataSet;
import lombok.*;
import util.Utils;

import java.util.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TripleObjectLink {

    private String id;
    private String datasetName;
    private String baseURL;
    private String remoteName;
    private String localClassName;
    private Map<String,String> mapper;
    private Map<String,String> prefixes;
    private TripleObjectSimplified origin;
    private List<LinkedTreeMap<String, Object>> links;
    private LinkedTreeMap<String, Object> attributes;

    public TripleObjectLink(String id, String datasetName, String baseURL, String remoteName, String localClassName) {
        this.id = id;
        this.datasetName = datasetName;
        this.baseURL = baseURL;
        this.remoteName = remoteName;
        this.localClassName = localClassName;
        this.prefixes = new HashMap<>();
        this.mapper = new HashMap<>();
        this.links = new ArrayList<>();
        this.attributes = new LinkedTreeMap<>();
    }

    public TripleObjectLink(String datasetName, String baseURL, String remoteName, String localClassName) {
        this.datasetName = datasetName;
        this.baseURL = baseURL;
        this.remoteName = remoteName;
        this.localClassName = localClassName;
        this.prefixes = new HashMap<>();
        this.mapper = new HashMap<>();
        this.links = new ArrayList<>();
        this.attributes = new LinkedTreeMap<>();
    }

    public void populateMapper(List<LodDataSet.Dataset.Connection.Mapping.LocalClass.Mapper> mappers) {
        for (LodDataSet.Dataset.Connection.Mapping.LocalClass.Mapper m : mappers) {
            mapper.put(m.getRemoteAttribute(),m.getLocalAttribute());
        }
    }

    public void populateLinks(JsonArray jLinks) {
        for (JsonElement jeLink: jLinks) {
            LinkedTreeMap<String, Object> link = new LinkedTreeMap<>();
            JsonObject jLink = jeLink.getAsJsonObject();
            if (jLink.has("type")) {
                link.put("type",jLink.get("type").getAsString());
            }
            if (jLink.has("link")) {
                link.put("link",jLink.get("link").getAsString());
            }
            links.add(link);
        }
    }

    public void populateAttributes(JsonObject jAttrs) {
        this.attributes = new Gson().fromJson(jAttrs.toString(), LinkedTreeMap.class);
    }

    public void populatePrefixes(JsonObject jPrefixes) {
        for (Map.Entry<String,JsonElement>  att: jPrefixes.entrySet()) {
            this.prefixes.put(att.getKey(),att.getValue().getAsString());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripleObjectLink that = (TripleObjectLink) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
