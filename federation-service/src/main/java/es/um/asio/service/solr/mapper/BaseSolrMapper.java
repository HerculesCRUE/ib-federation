package es.um.asio.service.solr.mapper;

import java.util.Optional;

/**
 * MapStruct base mapper.
 *
 * @param <E>
 *            the entity type.
 * @param <S>
 *            the Solr entity type.
 */
public interface BaseSolrMapper<E, S> {
    /**
     * Convert entity to Solr entity.
     *
     * @param entity
     *            the entity
     * @return the Solr entity
     */
    S convertToSolr(E entity);

    /**
     * Convert Solr entity to entity.
     *
     * @param solrEntity
     *            the Solr entity
     * @return the entity.
     */
    E convertFromSolr(S solrEntity);

    /**
     * Convert {@link Optional} to Solr entity
     *
     * @param optional
     *            the Optional.
     * @return the Solr entity.
     */
    default Optional<S> convertToSolr(final Optional<E> optional) {
        return Optional.ofNullable(this.convertToSolr(optional.orElse(null)));
    }

    /**
     * Convert {@link Optional} to Solr entity
     *
     * @param optional
     *            the Optional.
     * @return the Solr entity.
     */
    default Optional<E> convertFromSolr(final Optional<S> optional) {
        return Optional.ofNullable(this.convertFromSolr(optional.orElse(null)));
    }
}
