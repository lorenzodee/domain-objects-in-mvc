package domainobjectsmvc.domain.model;

import javax.persistence.*;

/**
 * Sample domain entity that does not provide a public default constructor (i.e.
 * does not provide a public zero-arguments constructor).
 *
 */
@Entity
@Table(name="accounts")
public class Account {

	private final String name;

	public Account(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Name must not be null or empty");
		}
		this.name = name;
		this.id = null;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private final Long id;

	public Long getId() {
		return id;
	}

	protected Account() {
		/* as required by ORM/JPA, not be design */
		this.name = null;
		this.id = null;
	}

}
