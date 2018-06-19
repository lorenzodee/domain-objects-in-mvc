package domainobjectsmvc.domain.model;

import java.io.Serializable;

import javax.persistence.*;

@SuppressWarnings("serial")
@Embeddable
public class OrderItemId implements Serializable {

	@Column(name="order_id")
	private final Long orderId;
	@Embedded
	@AttributeOverride(name="value", column=@Column(name="product_id"))
	private final ProductId productId;

	public OrderItemId(Long orderId, ProductId productId) {
		if (productId == null) {
			throw new IllegalArgumentException("Product ID must not be null");
		}
		this.orderId = orderId;
		this.productId = productId;
	}

	public Long getOrderId() {
		return orderId;
	}

	public ProductId getProductId() {
		return productId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((orderId == null) ? 0 : orderId.hashCode());
		result = prime * result + ((productId == null) ? 0 : productId.hashCode());
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
		OrderItemId other = (OrderItemId) obj;
		if (orderId == null) {
			if (other.orderId != null)
				return false;
		} else if (!orderId.equals(other.orderId))
			return false;
		if (productId == null) {
			if (other.productId != null)
				return false;
		} else if (!productId.equals(other.productId))
			return false;
		return true;
	}

	protected OrderItemId() {
		/* as required by ORM/JPA, not be design */
		this.orderId = null;
		this.productId = null;
	}

}
