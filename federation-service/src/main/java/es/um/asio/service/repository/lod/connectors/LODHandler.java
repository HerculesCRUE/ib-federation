package es.um.asio.service.repository.lod.connectors;

import es.um.asio.service.config.LodDataSet;
import es.um.asio.service.model.TripleObjectLink;
import es.um.asio.service.model.TripleObjectSimplified;
import es.um.asio.service.service.impl.TextHandlerServiceImp;

import java.util.Set;

public interface LODHandler {

    public abstract Set<TripleObjectLink> findLink(TripleObjectSimplified tos, LodDataSet.Dataset dataset, TextHandlerServiceImp textHandlerService);
}
