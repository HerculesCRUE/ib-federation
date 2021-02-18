package es.um.asio.back.controller.endpointSPARL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import es.um.asio.back.controller.datafetcher.DataFetcherController;
import es.um.asio.service.service.EndPointSparqlService;
import es.um.asio.service.validation.group.Create;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
    public Map<String,Object> executeQueryInTripleStore(
            @RequestHeader( value = "Authorization", required = false) final String authorization,
            @ApiParam(name = "type", value = "Triple Store by name to fetch data [ fuseki , wikibase ]", defaultValue = "fuseki", required = true)
            @PathVariable(required = true) @Validated(Create.class) final String type,
            @ApiParam(name = "query", value = "Query to execute in Endpoint SPARQL", defaultValue = "SELECT ?a ?b ?c WHERE { ?a ?b ?c}", required = true)
            @RequestParam(required = true, defaultValue = "sparql-proxy") @Validated(Create.class) final String query,
            @ApiParam(name = "nodeTimeout", value = "Node Timeout", defaultValue = "60000", required = false)
            @RequestParam(required = false) final String nodeTimeout,
            @ApiParam(name = "pageSize", value = "pageSize", defaultValue = "1000", required = false)
            @RequestParam(required = false) final String pageSize
    ) throws URISyntaxException, IOException {

        JsonObject jResponse = endPointSparqlService.executeQuery(authorization,query,type,Integer.valueOf(pageSize), (nodeTimeout==null)?defaultTimeout:Integer.valueOf(nodeTimeout) );
        return new Gson().fromJson(jResponse.toString(), LinkedTreeMap.class);
        //return new GsonBuilder().setPrettyPrinting().create().toJson(jResponse);
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
