package domainobjectsmvc.domain.model;

import java.io.Serializable;

import javax.persistence.*;

@SuppressWarnings("serial")
@Embeddable
public class ProductId implements Serializable {

	@Column(name="product_id")
	private final String value;

	public ProductId(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Value must not be null or empty");
		}
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		ProductId other = (ProductId) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	protected ProductId() {
		/* as required by ORM/JPA, not by design */
		this.value = null;
	}

}
