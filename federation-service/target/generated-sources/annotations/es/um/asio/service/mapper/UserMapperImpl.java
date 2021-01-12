package es.um.asio.service.mapper;

import es.um.asio.service.dto.UserDto;
import es.um.asio.service.mapper.decorator.UserMapperDecorator;
import es.um.asio.service.model.User;
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
public class UserMapperImpl extends UserMapperDecorator implements UserMapper {

    @Autowired
    @Qualifier("delegate")
    private UserMapper delegate;

    @Override
    public UserDto convertToDto(User entity)  {
        return delegate.convertToDto( entity );
    }

    @Override
    public List<UserDto> convertToDto(List<User> entities)  {
        return delegate.convertToDto( entities );
    }

    @Override
    public Iterable<User> convertFromDto(Iterable<UserDto> dto)  {
        return delegate.convertFromDto( dto );
    }
}
