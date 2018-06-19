package domainobjectsmvc.webmvc;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import domainobjectsmvc.domain.model.Order;

@Controller
@RequestMapping("/orders")
public class OrdersController {

	private PagingAndSortingRepository<Order, Long> orderRepository;

	@Autowired
	public OrdersController(
			PagingAndSortingRepository<Order, Long> orderRepository) {
		this.orderRepository = orderRepository;
	}

	@GetMapping
	public String list(Pageable pageable, Model model) {
		Page<Order> ordersPage = orderRepository.findAll(pageable);
		model.addAttribute("ordersPage", ordersPage);
		model.addAttribute("orders", ordersPage.getContent());
		return "orders/list";
	}

	@ModelAttribute
	public OrderForm populateModel(
			@PathVariable(required=false) Long id,
			@RequestParam Map<String, String> params,
			HttpMethod httpMethod) {
		// Case 1: GET /orders/{id}?edit, PUT /orders/{id}, and DELETE /orders/{id}
		if (id != null) {
			Order order = orderRepository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException());
			return OrderForm.fromDomainEntity(order);
		}
		// Case 2: GET /orders?create and POST /orders
		if ((httpMethod == HttpMethod.GET && params.containsKey("create"))
				|| httpMethod == HttpMethod.POST) {
			return OrderForm.fromDomainEntity(new Order());
		}
		// Case 3: GET /orders and all other GET requests
		return null;
	}

	@GetMapping("/{id}")
	public String show(@PathVariable Long id
			/* , @ModelAttribute OrderForm orderForm */) {
		return "orders/show";
	}

	@GetMapping(path="/{id}", params="edit")
	public String edit(@PathVariable Long id
			/* , @ModelAttribute OrderForm orderForm */) {
		return "orders/edit";
	}

	@PutMapping("/{id}")
	public String update(@PathVariable Long id,
			@ModelAttribute @Valid OrderForm orderForm, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "orders/edit";
		}
		orderRepository.save(orderForm.toDomainEntity());
		return "redirect:/orders";
	}

	@GetMapping(params="create")
	public String create(
			/* @ModelAttribute OrderForm orderForm, Model model */) {
		// model.addAttribute("orderForm", orderForm);
		return "orders/create";
	}

	@PostMapping
	public String save(
			@ModelAttribute @Valid OrderForm orderForm, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "orders/edit";
		}
		orderRepository.save(orderForm.toDomainEntity());
		return "redirect:/orders";
	}

	@DeleteMapping("/{id}")
	public String delete(@ModelAttribute OrderForm orderForm) {
		orderRepository.delete(orderForm.toDomainEntity());
		return "redirect:/orders";
	}

}
