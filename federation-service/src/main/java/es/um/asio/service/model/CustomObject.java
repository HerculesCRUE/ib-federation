package es.um.asio.service.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomObject {

    private CustomRDF first;
    private String firstName;
    private List<String> variables;

    Map<Integer,CustomTriplet> triplets;

    public CustomObject(JsonObject jContent,Node node) {
        this.triplets = new HashMap<>();
        this.variables = new ArrayList<>();
        List<String> keys = new ArrayList(jContent.keySet());
        if (keys.size()>=1) {
            this.firstName = keys.get(0);
            this.first = new CustomRDF(jContent.get(this.firstName).getAsJsonObject());
        }
        addTriplet(jContent,node);
    }

    public void addTriplet(JsonObject jContent,Node node) {
        CustomTriplet ct = new CustomTriplet(jContent,node);
        if (this.triplets.containsKey(ct.hashCode())) {
            ct = this.triplets.get(ct.hashCode());
        } else {
            this.triplets.put(ct.hashCode(),ct);
        }
        for (String key : ct.getVariables()) {
            if (!variables.contains(key))
                variables.add(key);
        }
        ct.addNode(node);
    }

    public JsonArray generateJsonResults() {
        JsonArray jResults = new JsonArray();
        for (CustomTriplet ct : triplets.values()) {
            jResults.add(ct.generateJsonResults());
        }
        return jResults;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomObject that = (CustomObject) o;
        return first.equals(that.first) &&
                firstName.equals(that.firstName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, firstName);
    }
}
