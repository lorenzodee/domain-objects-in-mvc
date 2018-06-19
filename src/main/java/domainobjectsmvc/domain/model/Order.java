package domainobjectsmvc.domain.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

/**
 * Sample domain entity with child entities that are not exposed as a mutable
 * list.
 *
 */
@Entity
@Table(name = "orders")
public class Order {

	@OneToMany(mappedBy="order")
	private Map<ProductId, OrderItem> items;

	public Order() {
		this.id = null;
	}

	protected Map<ProductId, OrderItem> getItemsInternal() {
		// Lazily initialize "items" field
		if (items == null) {
			this.items = new HashMap<>();
		}
		return items;
	}

	public void addItem(int quantity, ProductId productId) {
		Map<ProductId, OrderItem> items = getItemsInternal();
		OrderItem item = items.get(productId);
		if (item == null) {
			items.put(productId, new OrderItem(this, productId, quantity));
		} else {
			item.addQuantity(quantity);
		}
	}

	public void removeItem(ProductId productId) {
		getItemsInternal().remove(productId);
	}
	
	public Collection<OrderItem> getItems() {
		return Collections.unmodifiableCollection(
				getItemsInternal().values());
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
		Order other = (Order) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private final Long id;
	
	public Long getId() {
		return id;
	}

}
