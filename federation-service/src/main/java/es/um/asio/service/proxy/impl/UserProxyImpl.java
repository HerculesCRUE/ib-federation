package es.um.asio.service.proxy.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.izertis.abstractions.exception.NoSuchEntityException;
import es.um.asio.service.dto.UserDto;
import es.um.asio.service.filter.UserFilter;
import es.um.asio.service.mapper.UserMapper;
import es.um.asio.service.model.User;
import es.um.asio.service.proxy.UserProxy;
import es.um.asio.service.service.UserService;

/**
 * Proxy service implementation for {@link User}. Performs DTO conversion and permission checks.
 */
@Service
public class UserProxyImpl implements UserProxy {

    /**
     * Service layer.
     */
    @Autowired
    private UserService service;

    /**
     * DTO to entity mapper.
     */
    @Autowired
    private UserMapper mapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserDto> find(final String identifier) {
        return this.mapper.convertToDto(this.service.find(identifier));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<UserDto> findPaginated(final UserFilter filter, final Pageable pageable) {
        return this.mapper.convertToDto(this.service.findPaginated(filter, pageable));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserDto> findAll() {
        return this.mapper.convertToDto(this.service.findAll());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDto save(final UserDto entity) {
        return this.mapper.convertToDto(this.service.save(this.mapper.convertFromDto(entity)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserDto> save(final Iterable<UserDto> entities) {
        return this.mapper.convertToDto(this.service.save(this.mapper.convertFromDto(entities)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDto update(final UserDto entity) throws NoSuchEntityException {
        final User user = this.mapper.updateFromDto(entity, this.service.find(entity.getId())
                .orElseThrow(() -> new NoSuchEntityException(String.format("User %s not found", entity.getId()))));
        return this.mapper.convertToDto(this.service.update(user));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final UserDto entity) {
        this.service.delete(this.mapper.convertFromDto(entity));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final String identifier) {
        this.service.delete(identifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undelete(final String identifier) {
        this.service.undelete(identifier);
    }

}
