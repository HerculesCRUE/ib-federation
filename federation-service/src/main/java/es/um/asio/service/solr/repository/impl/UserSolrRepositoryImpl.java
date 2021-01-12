package es.um.asio.service.solr.repository.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;

import es.um.asio.service.filter.UserFilter;
import es.um.asio.service.solr.model.UserSolr;
import es.um.asio.service.solr.repository.UserSolrRepositoryCustom;
import com.izertis.libraries.solr.autoconfigure.properties.SolrProperties;
import com.izertis.libraries.solr.query.impl.AbstractFilteredQueryRepositoryImpl;

/**
 * Custom Spring Data Solr repository implementation for {@link UserSolr}
 */
public class UserSolrRepositoryImpl extends AbstractFilteredQueryRepositoryImpl<UserFilter, UserSolr>
        implements UserSolrRepositoryCustom {

    /**
     * Solr Template
     */
    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * Solr properties.
     */
    @Autowired
    private SolrProperties solrProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<UserSolr> findAll(final UserFilter filter, final Pageable pageable) {
        return this.solrTemplate.queryForPage(this.solrProperties.getCollectionName(),
                new SimpleQuery(this.createCriteria(filter), pageable), UserSolr.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Criteria> createCriterias(final UserFilter filter) {
        final List<Criteria> criterias = new ArrayList<>();

        if (filter != null) {
            if (StringUtils.isNotBlank(filter.getEmail())) {
                criterias.add(new Criteria(UserSolr.Fields.EMAIL).contains(filter.getEmail()));
            }

            if (StringUtils.isNotBlank(filter.getName())) {
                criterias.add(new Criteria(UserSolr.Fields.NAME).contains(filter.getName()));
            }

            if (filter.getEnabled() != null) {
                criterias.add(new Criteria(UserSolr.Fields.ENABLED).is(filter.getEnabled()));
            }
        }

        return criterias;
    }
}
