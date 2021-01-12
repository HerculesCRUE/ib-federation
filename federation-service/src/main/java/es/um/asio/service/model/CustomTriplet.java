package es.um.asio.service.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomTriplet {

    private Map<String,CustomRDF> triplets;
    private Set<Node> nodes;

    public CustomTriplet(JsonObject jContent,Node node) {
        triplets = new LinkedHashMap<>();
        nodes = new HashSet<>();
        List<String> keys = new ArrayList(jContent.keySet());
        for (String key : keys) {
            triplets.put(key,new CustomRDF(jContent.get(key).getAsJsonObject()));
        }
        nodes.add(node);
    }

    public void addTriplet(JsonObject jContent,Node node) {
        List<String> keys = new ArrayList(jContent.keySet());
        for (String key : keys) {
            triplets.put(key,new CustomRDF(jContent.get(key).getAsJsonObject()));
        }
        nodes.add(node);
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public List<String> getVariables() {
        List<String> keys = new ArrayList<>();
        for (String key : triplets.keySet()) {
            if (!keys.contains(key))
                keys.add(key);
        }
        return keys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomTriplet that = (CustomTriplet) o;
        return triplets.equals(that.triplets) &&
                nodes.equals(that.nodes);
    }

    public JsonObject generateJsonResults() {
        JsonObject jResult = new JsonObject();
        for (Map.Entry<String, CustomRDF> tripleEntry :triplets.entrySet()) {
            jResult.add(tripleEntry.getKey(),tripleEntry.getValue().toJson());
        }
        JsonArray jNodes = new JsonArray();
        for (Node node: nodes) {
            jNodes.add(node.toJson());
        }
        jResult.add("nodes",jNodes);
        return jResult;
    }

    @Override
    public int hashCode() {
        return Objects.hash(triplets);
    }
}
