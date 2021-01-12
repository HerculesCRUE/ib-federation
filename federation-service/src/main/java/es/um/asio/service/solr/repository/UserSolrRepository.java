package es.um.asio.service.solr.repository;

import java.util.Date;

import org.springframework.data.solr.repository.SolrCrudRepository;

import es.um.asio.service.solr.model.UserSolr;

/**
 * Spring Data Solr repository for {@link UserSolr}
 */
public interface UserSolrRepository extends SolrCrudRepository<UserSolr, String>, UserSolrRepositoryCustom {
    /**
     * Delete all documents older than an specific date.
     *
     * @param indexDate
     *            Date.
     */
    void deleteByIndexDateLessThan(Date indexDate);
}
