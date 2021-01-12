package es.um.asio.service.solr.mapper;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.izertis.abstractions.search.PageImplHelper;
import es.um.asio.service.model.User;
import es.um.asio.service.solr.mapper.decorator.UserSolrMapperDecorator;
import es.um.asio.service.solr.model.UserSolr;

/**
 * MapStruct Mapper for {@link UserSolr}.
 */
@Mapper
@DecoratedWith(UserSolrMapperDecorator.class)
public interface UserSolrMapper extends BaseSolrMapper<User, UserSolr> {

    /**
     * Convert entity to Solr entity.
     *
     * @param entity
     *            the entity
     * @return the Solr entity
     */
    @Mapping(target = "indexDate", expression = "java(new java.util.Date())")
    @Override
    UserSolr convertToSolr(User entity);

    /**
     * Convert Solr entity to entity.
     *
     * @param solrEntity
     *            the Solr entity
     * @return the entity.
     */
    @InheritInverseConfiguration
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "passwordRecoveryHash", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "language", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Override
    User convertFromSolr(UserSolr solrEntity);

    /**
     * Convert entity iterable to Solr.
     *
     * @param entities
     *            the list of entitites
     * @return the list of Solr entities.
     */
    List<UserSolr> convertToSolr(Iterable<User> entities);

    /**
     * Convert application user list from solr.
     *
     * @param solrEntities
     *            the Solr entity list
     * @return the list of entities
     */
    List<User> convertFromSolr(Iterable<UserSolr> solrEntities);

    /**
     * Convert Solr entity page to entity.
     *
     * @param page
     *            Solr entity pge.
     * @return entity page
     */
    PageImplHelper<User> convertFromSolr(Page<UserSolr> page);

}
