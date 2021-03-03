package es.um.asio.service.mapper;

import es.um.asio.audit.abstractions.search.PageImplHelper;
import es.um.asio.service.dto.UserDto;
import es.um.asio.service.model.Role;
import es.um.asio.service.model.User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor"
)
@Component
@Qualifier("delegate")
public class UserMapperImpl_ implements UserMapper {

    @Override
    public UserDto convertToDto(User entity) {
        if ( entity == null ) {
            return null;
        }

        UserDto userDto = new UserDto();

        userDto.setId( entity.getId() );
        userDto.setName( entity.getName() );
        userDto.setEmail( entity.getEmail() );
        userDto.setEnabled( entity.isEnabled() );
        userDto.setCredentialsNonExpired( entity.isCredentialsNonExpired() );
        userDto.setAccountNonExpired( entity.isAccountNonExpired() );
        userDto.setAccountNonLocked( entity.isAccountNonLocked() );
        userDto.setPassword( entity.getPassword() );
        userDto.setUsername( entity.getUsername() );
        userDto.setCountry( entity.getCountry() );
        userDto.setCity( entity.getCity() );
        userDto.setLanguage( entity.getLanguage() );
        userDto.setAddress( entity.getAddress() );
        Set<Role> set = entity.getRoles();
        if ( set != null ) {
            userDto.setRoles( new HashSet<Role>( set ) );
        }
        userDto.setVersion( entity.getVersion() );

        userDto.setPasswordChanged( false );

        return userDto;
    }

    @Override
    public List<UserDto> convertToDto(List<User> entities) {
        if ( entities == null ) {
            return null;
        }

        List<UserDto> list = new ArrayList<UserDto>( entities.size() );
        for ( User user : entities ) {
            list.add( convertToDto( user ) );
        }

        return list;
    }

    @Override
    public PageImplHelper<UserDto> convertToDto(Page<User> page) {
        if ( page == null ) {
            return null;
        }

        PageImplHelper<UserDto> pageImplHelper = new PageImplHelper<UserDto>();
        for ( User user : page ) {
            pageImplHelper.add( convertToDto( user ) );
        }

        return pageImplHelper;
    }

    @Override
    public User convertFromDto(UserDto dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setId( dto.getId() );
        user.setName( dto.getName() );
        user.setEmail( dto.getEmail() );
        user.setEnabled( dto.isEnabled() );
        user.setCredentialsNonExpired( dto.isCredentialsNonExpired() );
        user.setAccountNonExpired( dto.isAccountNonExpired() );
        user.setAccountNonLocked( dto.isAccountNonLocked() );
        user.setPassword( dto.getPassword() );
        user.setUsername( dto.getUsername() );
        user.setCountry( dto.getCountry() );
        user.setCity( dto.getCity() );
        user.setLanguage( dto.getLanguage() );
        user.setAddress( dto.getAddress() );
        Set<Role> set = dto.getRoles();
        if ( set != null ) {
            user.setRoles( new HashSet<Role>( set ) );
        }
        user.setVersion( dto.getVersion() );

        return user;
    }

    @Override
    public Iterable<User> convertFromDto(Iterable<UserDto> dto) {
        if ( dto == null ) {
            return null;
        }

        ArrayList<User> iterable = new ArrayList<User>();
        for ( UserDto userDto : dto ) {
            iterable.add( convertFromDto( userDto ) );
        }

        return iterable;
    }

    @Override
    public User updateFromDto(UserDto dto, User entity) {
        if ( dto == null ) {
            return null;
        }

        entity.setId( dto.getId() );
        entity.setName( dto.getName() );
        entity.setEmail( dto.getEmail() );
        entity.setEnabled( dto.isEnabled() );
        entity.setCredentialsNonExpired( dto.isCredentialsNonExpired() );
        entity.setAccountNonExpired( dto.isAccountNonExpired() );
        entity.setAccountNonLocked( dto.isAccountNonLocked() );
        entity.setPassword( dto.getPassword() );
        entity.setUsername( dto.getUsername() );
        entity.setCountry( dto.getCountry() );
        entity.setCity( dto.getCity() );
        entity.setLanguage( dto.getLanguage() );
        entity.setAddress( dto.getAddress() );
        if ( entity.getRoles() != null ) {
            Set<Role> set = dto.getRoles();
            if ( set != null ) {
                entity.getRoles().clear();
                entity.getRoles().addAll( set );
            }
            else {
                entity.setRoles( null );
            }
        }
        else {
            Set<Role> set = dto.getRoles();
            if ( set != null ) {
                entity.setRoles( new HashSet<Role>( set ) );
            }
        }
        entity.setVersion( dto.getVersion() );

        return entity;
    }
}
