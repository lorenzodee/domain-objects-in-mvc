package domainobjectsmvc.domain.model;

import javax.persistence.*;

/**
 * Sample domain entity with a generated ID field that does not have a setter
 * method.
 *
 */
@Entity
@Table(name="entities")
public class GeneratedIdEntity {

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private final Long id;
	// other fields not included

	public GeneratedIdEntity() {
		// other fields would have been arguments
		// and initialized here
		this.id = null;
	}

	public Long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		GeneratedIdEntity other = (GeneratedIdEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
