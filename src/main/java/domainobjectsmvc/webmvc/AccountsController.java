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

import domainobjectsmvc.domain.model.Account;

@Controller
@RequestMapping("/accounts")
public class AccountsController {

	private PagingAndSortingRepository<Account, Long> accountRepository;

	@Autowired
	public AccountsController(
			PagingAndSortingRepository<Account, Long> accountRepository) {
		this.accountRepository = accountRepository;
	}

	@GetMapping
	public String list(Pageable pageable, Model model) {
		Page<Account> accountsPage = accountRepository.findAll(pageable);
		model.addAttribute("accountsPage", accountsPage);
		model.addAttribute("accounts", accountsPage.getContent());
		return "accounts/list";
	}

	@ModelAttribute("account")
	public Account getAccount(
			@PathVariable(required=false) Long id,
			@RequestParam Map<String, String> params,
			HttpMethod httpMethod) {
		// Case 1: GET /accounts/{id}?edit, PUT /accounts/{id}, and DELETE /accounts/{id}
		if (id != null) {
			return accountRepository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException());
		}
		// Case 2: GET /accounts?create and POST /accounts
		if ((httpMethod == HttpMethod.GET && params.containsKey("create"))
				|| httpMethod == HttpMethod.POST) {
			return new Account(params.get("name"));
		}
		// Case 3: GET /accounts and all other GET requests
		return null;
	}

	@GetMapping("/{id}")
	public String show(@PathVariable Long id
			/* , @ModelAttribute Account account */) {
		return "accounts/show";
	}

	@GetMapping(path="/{id}", params="edit")
	public String edit(@PathVariable Long id
			/* , @ModelAttribute Account account */) {
		return "accounts/edit";
	}

	@PutMapping("/{id}")
	public String update(@PathVariable Long id,
			@ModelAttribute @Valid Account account, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "accounts/edit";
		}
		accountRepository.save(account);
		return "redirect:/accounts";
	}

	@GetMapping(params="create")
	public String create(
			/* @ModelAttribute Account account, Model model */) {
		// model.addAttribute("account", account);
		return "accounts/create";
	}

	@PostMapping
	public String save(
			@ModelAttribute @Valid Account account, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "accounts/edit";
		}
		accountRepository.save(account);
		return "redirect:/accounts";
	}

	@DeleteMapping("/{id}")
	public String delete(@ModelAttribute Account account) {
		accountRepository.delete(account);
		return "redirect:/accounts";
	}

}
