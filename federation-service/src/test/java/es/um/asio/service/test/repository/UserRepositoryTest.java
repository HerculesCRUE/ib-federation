package es.um.asio.service.test.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import es.um.asio.service.model.User;
import es.um.asio.service.repository.UserRepository;
import es.um.asio.service.service.UserService;

/**
 * Test for {@link UserService}
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTest {
    /**
     * Entity Manager
     */
    @Autowired
    private TestEntityManager entityManager;
    
    /**
     * User repository
     */
    @Autowired
    private UserRepository userRepository;
    
    @Test
    public void whenFindByUsername_thenReturnUser() {
        // given
        User user = new User();
        user.setUsername("john");
        entityManager.persist(user);
        entityManager.flush();
        
        // when
        Optional<User> found = userRepository.findByUsername(user.getUsername());
        
        //then
        assertThat(found.get().getUsername()).isEqualTo(user.getUsername());
    }
    
    @Test
    public void whenSetAccountNonLocked_thenUserMustBeLocked() {
        // given
        User user = new User();
        user.setUsername("john");
        user.setAccountNonLocked(false);
        entityManager.persist(user);
        entityManager.flush();
        entityManager.detach(user);
        
        // when
        userRepository.setAccountNonLocked(true, user.getId());
        
        //then
        User found = userRepository.getOne(user.getId());
        assertThat(found.isAccountNonLocked()).isTrue();
        entityManager.detach(found);
        
        // when
        userRepository.setAccountNonLocked(false, user.getId());
        
        //then
        found = userRepository.getOne(user.getId());
        assertThat(found.isAccountNonLocked()).isFalse();
        entityManager.detach(found);
    }
    
}
