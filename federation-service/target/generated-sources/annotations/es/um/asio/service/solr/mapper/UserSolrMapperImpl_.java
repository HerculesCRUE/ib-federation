package es.um.asio.service.solr.mapper;

import com.izertis.abstractions.search.PageImplHelper;
import es.um.asio.service.model.User;
import es.um.asio.service.solr.model.UserSolr;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor"
)
@Component
@Qualifier("delegate")
public class UserSolrMapperImpl_ implements UserSolrMapper {

    @Override
    public UserSolr convertToSolr(User entity) {
        if ( entity == null ) {
            return null;
        }

        UserSolr userSolr = new UserSolr();

        userSolr.setId( entity.getId() );
        userSolr.setName( entity.getName() );
        userSolr.setEmail( entity.getEmail() );
        userSolr.setEnabled( entity.isEnabled() );
        userSolr.setCredentialsNonExpired( entity.isCredentialsNonExpired() );
        userSolr.setAccountNonExpired( entity.isAccountNonExpired() );
        userSolr.setAccountNonLocked( entity.isAccountNonLocked() );
        userSolr.setUsername( entity.getUsername() );

        userSolr.setIndexDate( new java.util.Date() );

        return userSolr;
    }

    @Override
    public User convertFromSolr(UserSolr solrEntity) {
        if ( solrEntity == null ) {
            return null;
        }

        User user = new User();

        user.setId( solrEntity.getId() );
        user.setName( solrEntity.getName() );
        user.setEmail( solrEntity.getEmail() );
        user.setEnabled( solrEntity.isEnabled() );
        user.setCredentialsNonExpired( solrEntity.isCredentialsNonExpired() );
        user.setAccountNonExpired( solrEntity.isAccountNonExpired() );
        user.setAccountNonLocked( solrEntity.isAccountNonLocked() );
        user.setUsername( solrEntity.getUsername() );

        return user;
    }

    @Override
    public List<UserSolr> convertToSolr(Iterable<User> entities) {
        if ( entities == null ) {
            return null;
        }

        List<UserSolr> list = new ArrayList<UserSolr>();
        for ( User user : entities ) {
            list.add( convertToSolr( user ) );
        }

        return list;
    }

    @Override
    public List<User> convertFromSolr(Iterable<UserSolr> solrEntities) {
        if ( solrEntities == null ) {
            return null;
        }

        List<User> list = new ArrayList<User>();
        for ( UserSolr userSolr : solrEntities ) {
            list.add( convertFromSolr( userSolr ) );
        }

        return list;
    }

    @Override
    public PageImplHelper<User> convertFromSolr(Page<UserSolr> page) {
        if ( page == null ) {
            return null;
        }

        PageImplHelper<User> pageImplHelper = new PageImplHelper<User>();
        for ( UserSolr userSolr : page ) {
            pageImplHelper.add( convertFromSolr( userSolr ) );
        }

        return pageImplHelper;
    }
}
