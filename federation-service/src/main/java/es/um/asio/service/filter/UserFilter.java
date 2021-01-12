package es.um.asio.service.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import com.izertis.abstractions.filter.AbstractJpaSpecification;
import com.izertis.abstractions.filter.EntityFilter;
import es.um.asio.service.model.User;
import es.um.asio.service.model.User_;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Filter for {@link User}.
 */
@Getter
@Setter
@NoArgsConstructor
public class UserFilter extends AbstractJpaSpecification<User> implements EntityFilter {

    /**
     * Version ID.
     */
    private static final long serialVersionUID = 6371198451272564828L;

    /**
     * Email
     */
    private String email;

    /**
     * User enabled or not.
     */
    private Boolean enabled;

    /**
     * Name.
     */
    private String name;

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate toPredicate(final Root<User> root, final CriteriaQuery<?> query,
            final CriteriaBuilder criteriaBuilder) {

        final List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(this.email)) {
            predicates.add(this.createContainsIgnoreCase(root, criteriaBuilder, User_.EMAIL, this.email));
        }

        if (StringUtils.isNotBlank(this.name)) {
            predicates.add(this.createContainsIgnoreCase(root, criteriaBuilder, User_.NAME, this.name));
        }

        if (this.enabled != null) {
            predicates.add(this.createEquals(root, criteriaBuilder, User_.ACCOUNT_NON_LOCKED, this.enabled));
        }

        return criteriaBuilder.and(predicates.stream().toArray(Predicate[]::new));
    }
}
