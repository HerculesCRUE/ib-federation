package es.um.asio.service.solr.mapper;

import es.um.asio.service.model.User;
import es.um.asio.service.solr.mapper.decorator.UserSolrMapperDecorator;
import es.um.asio.service.solr.model.UserSolr;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor"
)
@Component
@Primary
public class UserSolrMapperImpl extends UserSolrMapperDecorator implements UserSolrMapper {

    @Autowired
    @Qualifier("delegate")
    private UserSolrMapper delegate;

    @Override
    public UserSolr convertToSolr(User entity)  {
        return delegate.convertToSolr( entity );
    }

    @Override
    public User convertFromSolr(UserSolr solrEntity)  {
        return delegate.convertFromSolr( solrEntity );
    }

    @Override
    public List<UserSolr> convertToSolr(Iterable<User> entities)  {
        return delegate.convertToSolr( entities );
    }

    @Override
    public List<User> convertFromSolr(Iterable<UserSolr> solrEntities)  {
        return delegate.convertFromSolr( solrEntities );
    }
}
