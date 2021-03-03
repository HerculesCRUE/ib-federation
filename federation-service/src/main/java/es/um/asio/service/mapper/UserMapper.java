package es.um.asio.service.mapper;

import java.util.List;

import es.um.asio.audit.abstractions.search.PageImplHelper;
import org.mapstruct.DecoratedWith;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

import es.um.asio.service.dto.UserDto;
import es.um.asio.service.mapper.decorator.UserMapperDecorator;
import es.um.asio.service.model.User;

/**
 * MapStruct Mapper for {@link User}.
 */
@Mapper
@DecoratedWith(UserMapperDecorator.class)
public interface UserMapper extends BaseMapper<User, UserDto> {

    /**
     * Convert entity to DTO.
     *
     * @param entity
     *            the entity
     * @return the DTO
     */
    @Mapping(target = "passwordChanged", expression = "java(false)")
    @Override
    UserDto convertToDto(User entity);

    /**
     * Convert entity list to DTO.
     *
     * @param entities
     *            the list of entitites
     * @return the list
     */
    List<UserDto> convertToDto(List<User> entities);

    /**
     * Convert entity page to DTO.
     *
     * @param page
     *            entity pge.
     * @return DTO page
     */
    PageImplHelper<UserDto> convertToDto(Page<User> page);

    /**
     * Convert DTO to entity.
     *
     * @param dto
     *            the DTO
     * @return the entity.
     */
    @Mapping(target = "passwordRecoveryHash", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @InheritInverseConfiguration
    @Override
    User convertFromDto(UserDto dto);

    /**
     * Convert DTO list to entity.
     *
     * @param entities
     *            the list of DTOs
     * @return the list
     */
    Iterable<User> convertFromDto(Iterable<UserDto> dto);

    /**
     * Update entity from DTO.
     *
     * @param dto
     *            the DTO
     * @param entity
     *            the entity
     * @return the entity
     */
    @Mapping(target = "passwordRecoveryHash", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User updateFromDto(UserDto dto, @MappingTarget User entity);
}
