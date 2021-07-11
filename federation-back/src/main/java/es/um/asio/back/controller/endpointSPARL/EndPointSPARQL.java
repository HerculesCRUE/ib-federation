package es.um.asio.back.controller.endpointSPARL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import es.um.asio.back.controller.datafetcher.DataFetcherController;
import es.um.asio.service.service.EndPointSparqlService;
import es.um.asio.service.validation.group.Create;
import io.swagger.annotations.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(EndPointSPARQL.Mappings.BASE)
@Api(value = "Module for EndPointSparql")
public class EndPointSPARQL {

    @Autowired
    EndPointSparqlService endPointSparqlService;

    @Value("${app.default-request-timeout}")
    Integer defaultTimeout;

    @GetMapping("/{type}")
    @ApiOperation(value = "End Point Sparql to fetch data at local node and triple store type by name")
    public String executeQueryInTripleStore(
            @RequestHeader( value = "Authorization", required = false) final String authorization,
            @ApiParam(name = "type", value = "Triple Store by name to fetch data [ fuseki , wikibase ]", defaultValue = "fuseki", required = true)
            @PathVariable(required = true) @Validated(Create.class) final String type,
            @ApiParam(name = "nodeTimeout", value = "Node Timeout", defaultValue = "60000", required = false)
            @RequestParam(required = false) final String nodeTimeout,
            @ApiParam(name = "pageSize", value = "pageSize", defaultValue = "5001", required = false)
            @RequestParam(required = false) final String pageSize,
            @ApiParam(name = "query", value = "The query SPARQL", defaultValue = "SELECT * WHERE {?a ?b ?c} limit 10000", required = false)
            @RequestParam(required = false) final String query

    ) throws URISyntaxException, IOException {

        JsonObject jResponse = endPointSparqlService.executeQuery(authorization,query,type,Integer.valueOf(pageSize), (nodeTimeout==null)?defaultTimeout:Integer.valueOf(nodeTimeout) );
        //return new Gson().fromJson(jResponse.toString(), LinkedTreeMap.class);
        return new GsonBuilder().setPrettyPrinting().create().toJson(jResponse);
    }

    @PostMapping(path = "/{type}", consumes = "text/plain; charset: utf-8")
    @ApiOperation(value = "End Point Sparql to fetch data at local node and triple store type by name")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "query",
                    examples = @io.swagger.annotations.Example(
                            value = {
                                    @ExampleProperty(value = "SELECT ?a ?b ?c WHERE { ?a ?b ?c }", mediaType = "text/plain")
                            }))
    })
    public String executeQueryPostInTripleStore(
            @RequestHeader( value = "Authorization", required = false) final String authorization,
            @ApiParam(name = "type", value = "Triple Store by name to fetch data [ fuseki , wikibase ]", defaultValue = "fuseki", required = true)
            @PathVariable(required = true) @Validated(Create.class) final String type,
            @ApiParam(name = "nodeTimeout", value = "Node Timeout", defaultValue = "60000", required = false)
            @RequestParam(required = false) final String nodeTimeout,
            @ApiParam(name = "pageSize", value = "pageSize", defaultValue = "5000", required = false)
            @RequestParam(required = false) final String pageSize,
            @RequestBody(required = true) String query
    ) throws URISyntaxException, IOException {

        JsonObject jResponse = endPointSparqlService.executeQuery(authorization,query,type,Integer.valueOf(pageSize), (nodeTimeout==null)?defaultTimeout:Integer.valueOf(nodeTimeout) );
        // return new Gson().fromJson(jResponse.toString(), LinkedTreeMap.class);
        return new GsonBuilder().setPrettyPrinting().create().toJson(jResponse);
    }



    /**
     * Mappgins.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Mappings {
        /**
         * Controller request mapping.
         */
        protected static final String BASE = "/endpoint-sparql";


    }
}
