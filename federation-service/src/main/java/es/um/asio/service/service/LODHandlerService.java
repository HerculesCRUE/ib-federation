package es.um.asio.service.service;

import es.um.asio.service.model.TripleObjectLink;
import es.um.asio.service.model.TripleObjectSimplified;
import es.um.asio.service.service.impl.TextHandlerServiceImp;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LODHandlerService {

    Set<TripleObjectLink> findLinksInHandlers(List<String> dataSets, TripleObjectSimplified tos);
}
