package es.um.asio.service.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(User.class)
public abstract class User_ extends es.um.asio.audit.model.Auditable_ {

	public static volatile SingularAttribute<User, String> passwordRecoveryHash;
	public static volatile SingularAttribute<User, String> country;
	public static volatile SingularAttribute<User, String> address;
	public static volatile SingularAttribute<User, String> city;
	public static volatile SingularAttribute<User, Boolean> credentialsNonExpired;
	public static volatile SetAttribute<User, Role> roles;
	public static volatile SingularAttribute<User, String> language;
	public static volatile SingularAttribute<User, Integer> version;
	public static volatile SingularAttribute<User, Boolean> enabled;
	public static volatile SingularAttribute<User, String> password;
	public static volatile SingularAttribute<User, String> name;
	public static volatile SingularAttribute<User, Boolean> accountNonExpired;
	public static volatile SingularAttribute<User, String> id;
	public static volatile SingularAttribute<User, String> email;
	public static volatile SingularAttribute<User, Boolean> accountNonLocked;
	public static volatile SingularAttribute<User, String> username;

	public static final String PASSWORD_RECOVERY_HASH = "passwordRecoveryHash";
	public static final String COUNTRY = "country";
	public static final String ADDRESS = "address";
	public static final String CITY = "city";
	public static final String CREDENTIALS_NON_EXPIRED = "credentialsNonExpired";
	public static final String ROLES = "roles";
	public static final String LANGUAGE = "language";
	public static final String VERSION = "version";
	public static final String ENABLED = "enabled";
	public static final String PASSWORD = "password";
	public static final String NAME = "name";
	public static final String ACCOUNT_NON_EXPIRED = "accountNonExpired";
	public static final String ID = "id";
	public static final String EMAIL = "email";
	public static final String ACCOUNT_NON_LOCKED = "accountNonLocked";
	public static final String USERNAME = "username";

}

