package es.um.asio.service.model;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URL;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Node {
    private String name;
    private URL url;

    public JsonObject toJson() {
        JsonObject jNode = new JsonObject();
        jNode.addProperty("node",name);
        jNode.addProperty("url",url.toString());
        return jNode;
    }
}
