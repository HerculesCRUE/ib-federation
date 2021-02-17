package es.um.asio.back.controller.lod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.um.asio.service.model.TripleObjectLink;
import es.um.asio.service.model.TripleObjectSimplified;
import es.um.asio.service.service.LODHandlerService;
import es.um.asio.service.validation.group.Create;
import io.swagger.annotations.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(LODSearchController.Mappings.BASE)
@Api(value = "search similar objects in Cloud LOD")
public class LODSearchController {

    @Autowired
    LODHandlerService lodHandlerService;

    @PostMapping(Mappings.SEARCH)
    @ApiOperation("search similar objects in Cloud LOD with the object in body of the request")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "obj",
                    dataType = "TripleObjectSimplified",
                    value ="{" +
                            "\"localURI\": \"http://herc-iz-front-desa.atica.um.es/Articulo/94eb3174-c69b-3a2a-a3e3-ebf3d6605d8b\"," +
                                    "\"className\": \"Articulo\"," +
                                    "\"lastModification\": 1434060000000," +
                                    "\"tripleStore\":{" +
                                    "\"id\": null,"+
                                    "\"name\": \"sparql\","+
                                    "\"baseURL\": null,"+
                                    "\"user\": null,"+
                                    "\"password\": null,"+
                                    "\"node\":{" +
                                    "\"nodeName\": \"um\""+
                                    "}" +
                                    "}," +
                                    "\"attributes\":{" +
                                    "\"localId\": \"51260\","+
                                    "\"año\": \"2011\","+
                                    "\"coautoria\": \"N\","+
                                    "\"name\": \"Implementation of a new modular facility to detoxify agro-wastewater polluted with neonicotinoid insecticides in farms by solar photocatalysis\""+
                                    "}"+
                            "}",
                    examples = @io.swagger.annotations.Example(
                            value = {
                                    @ExampleProperty(value = "’obj‘：{\"localURI\": \"http://herc-iz-front-desa.atica.um.es/Articulo/94eb3174-c69b-3a2a-a3e3-ebf3d6605d8b\",\"className\": \"Articulo\",\"lastModification\": 1434060000000,\"tripleStore\":{\"id\": null,\"name\": \"sparql\",\"baseURL\": null,\"user\": null,\"password\": null,\"node\":{\"nodeName\": \"um\"}},\"attributes\":{\"localId\": \"51260\",\"año\": \"2011\",\"coautoria\": \"N\",\"name\": \"Implementation of a new modular facility to detoxify agro-wastewater polluted with neonicotinoid insecticides in farms by solar photocatalysis\"}}", mediaType = "application/json")
                            }))
    })
    public Set<TripleObjectLink> searchInLOD(
            @ApiParam(name = "dataSets", value = "LOD dataset to search links separated by commas", defaultValue = "SCOPUS", required = true)
            @RequestParam(required = true, defaultValue = "SCOPUS") @Validated(Create.class) final String dataSets,
            @RequestBody final Object obj
    )  {
        List<String> datasets = Arrays.asList(dataSets.trim().split(","));
        Gson gson = new Gson();
        JsonObject jTripleObject = gson.fromJson(gson.toJson(obj), JsonObject.class);
        TripleObjectSimplified tos = new TripleObjectSimplified(jTripleObject);
        return lodHandlerService.findLinksInHandlers(datasets,tos);
    }

    /**
     * Mappgins.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Mappings {
        /**
         * Controller request mapping.
         */
        protected static final String BASE = "/lod";

        /**
         * Controller request mapping.
         */
        protected static final String SEARCH = "/search";
    }
}
