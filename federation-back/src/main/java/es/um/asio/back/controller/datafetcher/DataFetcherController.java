package es.um.asio.back.controller.datafetcher;

import es.um.asio.service.config.DataSourceRepository;
import es.um.asio.service.config.LodDataSet;
import es.um.asio.service.model.TripleObjectSimplified;
import es.um.asio.service.service.DataFetcherService;
import es.um.asio.service.validation.group.Create;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(DataFetcherController.Mappings.BASE)
@Api(value = "Module Data-fetcher of Federation")
public class DataFetcherController {

    @Autowired
    DataFetcherService dataFetcherService;

    @Autowired
    LodDataSet lodDataSet;

    @Autowired
    DataSourceRepository dataSourceRepository;

    @GetMapping(Mappings.OBJECTS)
    @ApiOperation(value = "Get All Class in the Triple Store")
    public Set<String> getAllObjects(
        @ApiParam(name = "node", value = "Node of data", defaultValue = "um", required = true)
        @RequestParam(required = true, defaultValue = "um") @Validated(Create.class) final String node,
        @ApiParam(name = "service", value = "Service of SPARQL", defaultValue = "sparql-proxy", required = true)
        @RequestParam(required = true, defaultValue = "sparql-proxy") @Validated(Create.class) final String endpointSPARQService,
        @ApiParam(name = "tripleStore", value = "Triple Store of data", defaultValue = "sparql", required = true)
        @RequestParam(required = true, defaultValue = "sparql") @Validated(Create.class) final String tripleStore
    ) throws URISyntaxException, IOException {
        return dataFetcherService.getObjectsUris(
                node,endpointSPARQService,tripleStore
        );
    }

    @GetMapping(Mappings.INSTANCES)
    @ApiOperation(value = "Get All Instances in the Triple Store")
    public Set<TripleObjectSimplified> getAllInstances(
            @ApiParam(name = "node", value = "Node of data", defaultValue = "um", required = true)
            @RequestParam(required = true, defaultValue = "um") @Validated(Create.class) final String node,
            @ApiParam(name = "service", value = "Service of SPARQL", defaultValue = "sparql-proxy", required = true)
            @RequestParam(required = true, defaultValue = "sparql-proxy") @Validated(Create.class) final String endpointSPARQService,
            @ApiParam(name = "tripleStore", value = "Triple Store of data", defaultValue = "sparql", required = true)
            @RequestParam(required = true, defaultValue = "sparql") @Validated(Create.class) final String tripleStore,
            @ApiParam(name = "className", value = "The class of instances", required = true)
            @RequestParam(required = true) @Validated(Create.class) final String className
    ) throws URISyntaxException, IOException {
        return dataFetcherService.getTripleObjectSimplified(
                node,endpointSPARQService,tripleStore,className
        );
    }

    @GetMapping(Mappings.FIND_INSTANCE)
    @ApiOperation(value = "Find a instance in the Triple Store by Canonical URI")
    public TripleObjectSimplified getFindInstanceByURI(
            @ApiParam(name = "node", value = "Node of data", defaultValue = "um", required = true)
            @RequestParam(required = true, defaultValue = "um") @Validated(Create.class) final String node,
            @ApiParam(name = "service", value = "Service of SPARQL", defaultValue = "sparql-proxy", required = true)
            @RequestParam(required = true, defaultValue = "sparql-proxy") @Validated(Create.class) final String endpointSPARQService,
            @ApiParam(name = "tripleStore", value = "Triple Store of data", defaultValue = "sparql", required = true)
            @RequestParam(required = true, defaultValue = "sparql") @Validated(Create.class) final String tripleStore,
            @ApiParam(name = "className", value = "The class of instances", required = true)
            @RequestParam(required = true) @Validated(Create.class) final String className,
            @ApiParam(name = "uri", value = "URI of subject", required = true)
            @RequestParam(required = true) @Validated(Create.class) final String uri

    ) throws URISyntaxException, IOException {
        return dataFetcherService.findTripleObjectSimplifiedByURI(
                node,endpointSPARQService,tripleStore,className,uri
        );
    }



    /**
     * Mappgins.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Mappings {
        /**
         * Controller request mapping.
         */
        protected static final String BASE = "/data-fetcher";

        /**
         * Mapping for list.
         */
        protected static final String GREET = "/greet";

        /**
         * Mapping for list.
         */
        protected static final String OBJECTS = "/objects";

        /**
         * Mapping for list.
         */
        protected static final String INSTANCES = "/instances";

        /**
         * Mapping for list.
         */
        protected static final String FIND_INSTANCE = "/instance/find";

    }
}
