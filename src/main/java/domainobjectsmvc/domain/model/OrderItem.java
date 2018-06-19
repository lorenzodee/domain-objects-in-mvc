package domainobjectsmvc.domain.model;

import javax.persistence.*;

@Entity
@Table(name="order_items")
public class OrderItem {

	private int quantity;
	// private ... price;

	OrderItem(Order order, ProductId productId, int quantity) {
		if (order == null) {
			throw new IllegalArgumentException("Order must not be null");
		}
		if (productId == null) {
			throw new IllegalArgumentException("ProductId must not be null");
		}
		this.order = order;
		this.orderItemId = new OrderItemId(order.getId(), productId);
		setQuantity(quantity);
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException("Quantity must be greater than zero");
		}
		this.quantity = quantity;
	}
	
	public void addQuantity(int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException("Added quantity must be greater than zero");
		}
		this.quantity += quantity;
	}

	public ProductId getProductId() {
		return orderItemId.getProductId();
	}

	@EmbeddedId
	@AttributeOverrides({
		@AttributeOverride(name="orderId", column=@Column(name="order_id")),
		@AttributeOverride(name="productId.value", column=@Column(name="product_id"))
	})
	private OrderItemId orderItemId;

	@ManyToOne(optional=false)
	@MapsId("orderId")
	private Order order;

	protected OrderItem() {
		/* as required by ORM/JPA, not by design */
	}

	@Override
	public String toString() {
		return "OrderItem [quantity=" + quantity
				+ ", orderId=" + orderItemId.getOrderId()
				+ ", productId=" + orderItemId.getProductId() + "]";
	}

}
