package domainobjectsmvc.webmvc;

import java.util.LinkedList;
import java.util.List;

import javax.validation.Valid;

import domainobjectsmvc.domain.model.Order;
import domainobjectsmvc.domain.model.OrderItem;
import domainobjectsmvc.domain.model.ProductId;

public class OrderForm {
	
	public static OrderForm fromDomainEntity(Order order) {
		return new OrderForm(order);
	}

	private final Order order;

	private List<OrderFormItem> formItems;
	
	protected OrderForm(Order order) {
		this.order = order;
		this.formItems = new LinkedList<>();
		if (order != null) {
			// Initialize form data from order domain entity
			for (OrderItem orderItem : order.getItems()) {
				OrderFormItem formItem = new OrderFormItem();
				formItem.setQuantity(orderItem.getQuantity());
				formItem.setProductId(orderItem.getProductId().getValue());
				this.formItems.add(formItem);
			}
		}
	}

	public Order getOrder() {
		return order;
	}

	@Valid
	public List<OrderFormItem> getItems() {
		return formItems;
	}

	public void setItems(List<OrderFormItem> items) {
		this.formItems = items;
	}

	public Order toDomainEntity() {
		// Apply form data to order domain entity
		for (OrderFormItem formItem : formItems) {
			order.addItem(formItem.getQuantity(),
					new ProductId(formItem.getProductId()));
		}
		return order;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((order == null) ? 0 : order.hashCode());
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
		OrderForm other = (OrderForm) obj;
		if (order == null) {
			if (other.order != null)
				return false;
		} else if (!order.equals(other.order))
			return false;
		return true;
	}

}
