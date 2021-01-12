package es.um.asio.service.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import es.um.asio.service.filter.UserFilter;
import es.um.asio.service.model.User;
import es.um.asio.service.repository.UserRepository;
import es.um.asio.service.service.UserService;
import es.um.asio.service.solr.mapper.UserSolrMapper;
import es.um.asio.service.solr.model.UserSolr;
import es.um.asio.service.solr.repository.UserSolrRepository;
import com.izertis.libraries.solr.annotation.Indexable;
import com.izertis.libraries.solr.annotation.IndexableClass;

/**
 * Service implementation to handle {@link User} entity related operations
 */
@IndexableClass
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class UserServiceImpl implements UserService, UserDetailsService {

    /**
     * Spring Data repository for {@link User}.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Spring Data Solr repository for {@link UserSolr}
     */
    @Autowired(required = false)
    private UserSolrRepository userSolrRepository;

    /**
     * Solr enabled
     */
    @Value("${app.solr.enabled:#{false}}")
    private boolean solrEnabled;

    /**
     * MapStruct Mapper for {@link UserSolr}.
     */
    @Autowired(required = false)
    private UserSolrMapper userSolrMapper;

    /**
     * {@inheritDoc}
     */
    @Indexable(User.class)
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public User save(final User entity) {
        return this.userRepository.save(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Indexable(User.class)
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public List<User> save(final Iterable<User> entities) {
        return this.userRepository.saveAll(entities);
    }

    /**
     * {@inheritDoc}
     */
    @Indexable(User.class)
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public User update(final User entity) {
        return this.userRepository.save(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Indexable(User.class)
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public void delete(final User entity) {
        this.userRepository.setAccountNonLocked(false, entity.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Indexable(User.class)
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public void delete(final String identifier) {
        this.userRepository.setAccountNonLocked(false, identifier);
    }

    /**
     * {@inheritDoc}
     */
    @Indexable(User.class)
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public void undelete(final String identifier) {
        this.userRepository.setAccountNonLocked(true, identifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<User> find(final String identifier) {
        return this.userRepository.findById(identifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<User> findPaginated(final UserFilter filter, final Pageable pageable) {
        Page<User> page;

        if (this.solrEnabled) {
            page = this.userSolrMapper.convertFromSolr(this.userSolrRepository.findAll(filter, pageable));
        } else {
            page = this.userRepository.findAll(filter, pageable);
        }

        return page;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> findAll() {
        return this.userRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDetails loadUserByUsername(final String username) {
        return this.userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(String.format("User with username %s not found", username)));
    }
}
