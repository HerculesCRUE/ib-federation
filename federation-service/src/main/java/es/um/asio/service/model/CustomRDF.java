package es.um.asio.service.model;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import util.Utils;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomRDF {

    private String type;
    private String dataType;
    private String value;

    public CustomRDF(JsonObject jContent) {
        if (jContent.has("type"))
            this.type = jContent.get("type").getAsString();
        if (jContent.has("dataType"))
            this.dataType = jContent.get("dataType").getAsString();
        if (jContent.has("value"))
            this.value = jContent.get("value").getAsString();
    }

    public JsonObject toJson(){
        JsonObject jObject = new JsonObject();
        if (Utils.isValidString(type))
            jObject.addProperty("type",this.type);
        if (Utils.isValidString(dataType))
            jObject.addProperty("dataType",this.dataType);
        if (Utils.isValidString(value))
            jObject.addProperty("value",this.value);
        return jObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomRDF customRDF = (CustomRDF) o;
        return type.equals(customRDF.type) &&
                Objects.equals(dataType, customRDF.dataType) &&
                value.equals(customRDF.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, dataType, value);
    }
}
