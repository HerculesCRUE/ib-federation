package es.um.asio.service.service.impl;

import es.um.asio.service.config.LodDataSet;
import es.um.asio.service.model.TripleObjectLink;
import es.um.asio.service.model.TripleObjectSimplified;
import es.um.asio.service.repository.lod.connectors.CrossRefHandler;
import es.um.asio.service.repository.lod.connectors.LODHandler;
import es.um.asio.service.repository.lod.connectors.SCOPUSHandler;
import es.um.asio.service.repository.lod.connectors.WikidataHandler;
import es.um.asio.service.service.LODHandlerService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LODHandlerServiceImp implements LODHandlerService {

    @Autowired
    LodDataSet lds;

    @Autowired
    TextHandlerServiceImp textHandlerService;

    @Override
    public Set<TripleObjectLink> findLinksInHandlers(List<String> dataSets, TripleObjectSimplified tos) {
        Set<TripleObjectLink> results = new HashSet<>();
        for (String dataset : dataSets) {
            Optional<LodDataSet.Dataset> ods = lds.getDatasetByName(dataset);
            if (ods.get()!=null) { // Si contiene datos
                DatasetEnum dse = DatasetEnum.findByName(dataset);
                try {
                    LodDataSet.Dataset pds = ods.get().getPrunedDatasetByClassName(tos.getClassName());
                    if (dse != null && pds.getConnections().size() > 0) {
                        LODHandler handler = dse.getHandler();
                        Set<TripleObjectLink> resultsAux = handler.findLink(tos, pds,textHandlerService);
                        results.addAll(resultsAux);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return results;
    }

    @Getter
    public enum DatasetEnum {
        SCOPUS("SCOPUS", new SCOPUSHandler()),
        CROSSREF("CROSSREF", new CrossRefHandler()),
        WIKIDATA("WIKIDATA", new WikidataHandler());

        private String name;
        private LODHandler handler;

        DatasetEnum(String name, LODHandler handler) {
            this.name = name;
            this.handler = handler;
        }

        public static DatasetEnum findByName(String name) {
            for (DatasetEnum dse : DatasetEnum.values()) {
                if(dse.getName().equals(name))
                    return dse;
            }
            return null;
        }

    }
}
