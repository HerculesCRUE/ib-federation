package es.um.asio.back.controller.federation;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import es.um.asio.service.exceptions.CustomFederationException;
import es.um.asio.service.service.FederationService;
import es.um.asio.service.validation.group.Create;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import util.Utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(FederationController.Mappings.BASE)
@Api(value = "Federation Module in Federation: Execute a federate SPARQL query")
public class FederationController {

    @Autowired
    FederationService federationService;

    @Value("${app.default-request-timeout}")
    Integer defaultTimeout;

    @PostMapping(Mappings.ALL)
    @ApiOperation("Execute federate SPARQL query in all nodes registered")
    public String executeQueryInAllNodes(
            @ApiParam(name = "tripleStore", value = "Triple Store of data", defaultValue = "fuseki", required = true)
            @RequestParam(required = true, defaultValue = "fuseki") @Validated(Create.class) final String tripleStore,
            @ApiParam(name = "nodeTimeout", value = "Node Timeout", defaultValue = "60000", required = false)
            @RequestParam(required = false) final String nodeTimeout,
            @ApiParam(name = "pageSize", value = "pageSize", defaultValue = "1000", required = false)
            @RequestParam(required = false) final String pageSize,
            @ApiParam(name = "query", value = "Query to execute in nodes", required = true)
            @RequestParam(required = true, defaultValue = "SELECT ?a ?b ?c WHERE {?a ?b ?c}") final String query
    ) throws IOException, URISyntaxException, JSONException {
        Integer limit = es.um.asio.service.util.Utils.extractLimitInSPARQL(query);
        JsonObject jResponse = federationService.executeQueryInAllNodes(query,tripleStore,Integer.valueOf(pageSize), (nodeTimeout==null)?defaultTimeout:Integer.valueOf(nodeTimeout), limit);
        return new GsonBuilder().setPrettyPrinting().create().toJson(jResponse);
    }

    @PostMapping(Mappings.NODES_LIST)
    @ApiOperation("Execute federate SPARQL query in nodes of list in param nodeList (coma separated)")
    public String executeQueryInNodesList(
            @ApiParam(name = "tripleStore", value = "Triple Store of data", defaultValue = "fuseki", required = true)
            @RequestParam(required = true, defaultValue = "fuseki") @Validated(Create.class) final String tripleStore,
            @ApiParam(name = "nodeTimeout", value = "Node Timeout", defaultValue = "60000", required = false)
            @RequestParam(required = false) final String nodeTimeout,
            @ApiParam(name = "pageSize", value = "pageSize", defaultValue = "1000", required = false, type = "Integer")
            @RequestParam(required = false) final String pageSize,
            @ApiParam(name = "nodeList", value = "Node List", defaultValue = "um, um2", required = true)
            @RequestParam(required = true) final String nodeList,
            @ApiParam(name = "query", value = "Query to execute in nodes", required = true)
            @RequestParam(required = true, defaultValue = "SELECT ?a ?b ?c WHERE {?a ?b ?c}") final String query
    ) throws IOException, URISyntaxException, JSONException {
        if (!Utils.isValidString(nodeList)) {
            throw new CustomFederationException("nodes list can not be null");
        }
        List<String> nodes = Arrays.asList(nodeList.replaceAll(" ","").split(","));
        Integer limit = es.um.asio.service.util.Utils.extractLimitInSPARQL(query);
        JsonObject jResponse = federationService.executeQueryInNodesList(query,tripleStore,nodes,Integer.valueOf(pageSize), (nodeTimeout==null)?defaultTimeout:Integer.valueOf(nodeTimeout),limit );
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
        protected static final String BASE = "/federation";

        /**
         * Mapping for list.
         */
        protected static final String ALL = "/nodes/all";

        /**
         * Mapping for list.
         */
        protected static final String NODES_LIST = "/nodes/list";

    }
}
