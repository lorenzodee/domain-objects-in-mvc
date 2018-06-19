package domainobjectsmvc.domain.model;

import javax.persistence.*;

@Entity
public class Product {

	@EmbeddedId
	private ProductId id;

	public Product(ProductId id) {
		if (id == null) {
			throw new IllegalArgumentException(
					"ID must not be null");
		}
		this.id = id;
	}

	public ProductId getId() {
		return id;
	}

}
