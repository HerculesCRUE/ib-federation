package es.um.asio.audit.model;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Auditable.class)
public abstract class Auditable_ {

	public static volatile SingularAttribute<Auditable, Date> createdDate;
	public static volatile SingularAttribute<Auditable, Date> lastModifiedDate;
	public static volatile SingularAttribute<Auditable, String> createdBy;
	public static volatile SingularAttribute<Auditable, String> lastModifiedBy;

	public static final String CREATED_DATE = "createdDate";
	public static final String LAST_MODIFIED_DATE = "lastModifiedDate";
	public static final String CREATED_BY = "createdBy";
	public static final String LAST_MODIFIED_BY = "lastModifiedBy";

}

