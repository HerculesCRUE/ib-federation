package es.um.asio.service.solr.repository;

import es.um.asio.service.filter.UserFilter;
import es.um.asio.service.solr.model.UserSolr;
import com.izertis.libraries.solr.query.FilteredQueryRepository;

/**
 * Custom Spring Data Solr repository for {@link UserSolr}
 */
public interface UserSolrRepositoryCustom extends FilteredQueryRepository<UserFilter, UserSolr> {

}
