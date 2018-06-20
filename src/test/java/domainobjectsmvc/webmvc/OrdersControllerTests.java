package domainobjectsmvc.webmvc;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import domainobjectsmvc.domain.model.Account;
import domainobjectsmvc.domain.model.AccountRepository;
import domainobjectsmvc.domain.model.Order;
import domainobjectsmvc.domain.model.OrderRepository;
import domainobjectsmvc.domain.model.ProductId;
import domainobjectsmvc.domain.model.GeneratedIdEntity;
import domainobjectsmvc.domain.model.GeneratedIdEntityRepository;

@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@WebMvcTest(OrdersController.class)
@EnableSpringDataWebSupport
public class OrdersControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private OrderRepository orderRepository;

	private Long id;

	@Before
	public void setUp() throws Exception {
		this.id = 123L;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void list() throws Exception {
		List<Order> orders = new LinkedList<>();
		Page<Order> ordersPage = new PageImpl<>(orders);
		when(orderRepository.findAll(any(Pageable.class)))
			.thenReturn(ordersPage);
		mvc.perform(get("/orders"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("ordersPage", is(ordersPage)))
			.andExpect(model().attribute("orders", is(orders)))
			.andExpect(model().attributeDoesNotExist("order"))
			.andExpect(view().name("orders/list"));
		verify(orderRepository).findAll(any(Pageable.class));
	}

	@Test
	public void show() throws Exception {
		Order order = new Order();
		when(orderRepository.findById(id))
			.thenReturn(Optional.of(order));
		OrderForm orderForm = OrderForm.fromDomainEntity(order);
		mvc.perform(get("/orders/{id}", id))
			.andExpect(status().isOk())
			.andExpect(model().attribute(
					"orderForm", is(orderForm)))
			.andExpect(view().name("orders/show"));
		verify(orderRepository).findById(eq(id));
	}

	@Test
	public void showNotFound() throws Exception {
		when(orderRepository.findById(id))
			.thenReturn(Optional.empty());
		mvc.perform(get("/orders/{id}", id))
			.andExpect(status().isNotFound());
		verify(orderRepository).findById(eq(id));
	}

	@Test
	public void edit() throws Exception {
		Order order = new Order();
		when(orderRepository.findById(id))
			.thenReturn(Optional.of(order));
		OrderForm orderForm = OrderForm.fromDomainEntity(order);
		mvc.perform(get("/orders/{id}", id).param("edit", ""))
			.andExpect(status().isOk())
			.andExpect(model().attribute("orderForm", is(orderForm)))
			.andExpect(view().name("orders/edit"));
		verify(orderRepository).findById(eq(id));
	}

	@Test
	public void editNotFound() throws Exception {
		when(orderRepository.findById(id))
			.thenReturn(Optional.empty());
		mvc.perform(get("/orders/{id}", id).param("edit", ""))
			.andExpect(status().isNotFound());
		verify(orderRepository).findById(eq(id));
	}

	@Test
	public void update() throws Exception {
		Order order = new Order();
		when(orderRepository.findById(id))
			.thenReturn(Optional.of(order));
		OrderForm orderForm = OrderForm.fromDomainEntity(order);
		mvc.perform(put("/orders/{id}", id)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("items[0].productId", "xxx")
				.param("items[0].quantity", "3")
				.param("items[1].productId", "yyy")
				.param("items[1].quantity", "2")
				.param("items[2].productId", "zzz")
				.param("items[2].quantity", "5"))
			.andExpect(redirectedUrl("/orders"));
		assertThat(order.getItems(), hasSize(3));
		assertThat(order.getItems(), hasItem(allOf(
				hasProperty("productId", equalTo(new ProductId("xxx"))),
				hasProperty("quantity", equalTo(3)))));
		assertThat(order.getItems(), hasItem(allOf(
				hasProperty("productId", equalTo(new ProductId("yyy"))),
				hasProperty("quantity", equalTo(2)))));
		assertThat(order.getItems(), hasItem(allOf(
				hasProperty("productId", equalTo(new ProductId("zzz"))),
				hasProperty("quantity", equalTo(5)))));
		verify(orderRepository).findById(eq(id));
		verify(orderRepository).save(eq(order));
	}

	@Test
	public void updateWithErrors() throws Exception {
		Order order = new Order();
		when(orderRepository.findById(id))
			.thenReturn(Optional.of(order));
		OrderForm orderForm = OrderForm.fromDomainEntity(order);
		mvc.perform(put("/orders/{id}", id)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("items[0].productId", "xxx")
				.param("items[0].quantity", "-1"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("orderForm", "items[0].quantity"))
			.andExpect(model().attribute("orderForm", is(orderForm)))
			.andExpect(view().name("orders/edit"));
		verify(orderRepository).findById(eq(id));
	}

	@Test
	public void create() throws Exception {
		mvc.perform(get("/orders").param("create", ""))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("orderForm"))
			.andExpect(view().name("orders/create"));
	}

	@Test
	public void save() throws Exception {
		when(orderRepository.save(any(Order.class)))
			.then(AdditionalAnswers.returnsFirstArg());
		mvc.perform(post("/orders"))
			.andExpect(redirectedUrl("/orders"));
		verify(orderRepository).save(any(Order.class));
	}

	/*
	@Test
	public void saveWithErrors() throws Exception {
		mvc.perform(post("/orders"))
			.andExpect(view().name("orders/create"));
	}
	*/

	@Test
	public void deleteExisting() throws Exception {
		Order entity = new Order();
		when(orderRepository.findById(id))
			.thenReturn(Optional.of(entity));
		mvc.perform(delete("/orders/{id}", id))
			.andExpect(redirectedUrl("/orders"));
		verify(orderRepository).findById(eq(id));
		verify(orderRepository).delete(eq(entity));
	}

	@Test
	public void deleteNotFound() throws Exception {
		Order entity = new Order();
		when(orderRepository.findById(id))
			.thenReturn(Optional.empty());
		mvc.perform(delete("/orders/{id}", id))
			.andExpect(status().isNotFound());
		verify(orderRepository).findById(eq(id));
	}

}
