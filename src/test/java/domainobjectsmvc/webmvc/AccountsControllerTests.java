package domainobjectsmvc.webmvc;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import domainobjectsmvc.domain.model.Account;
import domainobjectsmvc.domain.model.AccountRepository;

@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@WebMvcTest(AccountsController.class)
@EnableSpringDataWebSupport
public class AccountsControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private AccountRepository accountRepository;

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
		List<Account> accounts = new LinkedList<>();
		Page<Account> accountsPage = new PageImpl<>(accounts);
		when(accountRepository.findAll(any(Pageable.class)))
			.thenReturn(accountsPage);
		mvc.perform(get("/accounts"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("accountsPage", is(accountsPage)))
			.andExpect(model().attribute("accounts", is(accounts)))
			.andExpect(model().attributeDoesNotExist("account"))
			.andExpect(view().name("accounts/list"));
	}

	@Test
	public void show() throws Exception {
		Account account = new Account("test");
		when(accountRepository.findById(id))
			.thenReturn(Optional.of(account));
		mvc.perform(get("/accounts/{id}", id))
			.andExpect(status().isOk())
			.andExpect(model().attribute("account", is(account)))
			.andExpect(view().name("accounts/show"));
		verify(accountRepository).findById(eq(id));
	}

	@Test
	public void showNotFound() throws Exception {
		when(accountRepository.findById(id))
			.thenReturn(Optional.empty());
		mvc.perform(get("/accounts/{id}", id))
			.andExpect(status().isNotFound());
		verify(accountRepository).findById(eq(id));
	}

	@Test
	public void edit() throws Exception {
		Account account = new Account("test");
		when(accountRepository.findById(id))
			.thenReturn(Optional.of(account));
		mvc.perform(get("/accounts/{id}", id).param("edit", ""))
			.andExpect(status().isOk())
			.andExpect(model().attribute("account", is(account)))
			.andExpect(view().name("accounts/edit"));
		verify(accountRepository).findById(eq(id));
	}

	@Test
	public void editNotFound() throws Exception {
		when(accountRepository.findById(id))
			.thenReturn(Optional.empty());
		mvc.perform(get("/accounts/{id}", id).param("edit", ""))
			.andExpect(status().isNotFound());
		verify(accountRepository).findById(eq(id));
	}

	@Test
	public void update() throws Exception {
		Account account = new Account("test");
		when(accountRepository.findById(id))
			.thenReturn(Optional.of(account));
		mvc.perform(put("/accounts/{id}", id))
			.andExpect(redirectedUrl("/accounts"));
		verify(accountRepository).findById(eq(id));
		verify(accountRepository).save(eq(account));
	}

	/*
	@Test
	public void updateWithErrors() throws Exception {
		Account account = new Account("test");
		when(accountRepository.findById(id))
			.thenReturn(Optional.of(account));
		mvc.perform(put("/accounts/{id}", id))
			.andExpect(status().isOk())
			.andExpect(model().attribute("account", is(account)))
			.andExpect(view().name("accounts/edit"));
	}
	*/

	@Test
	public void create() throws Exception {
		mvc.perform(get("/accounts")
				.param("create", "")
				.param("name", "test"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("account"))
			.andExpect(view().name("accounts/create"));
	}

	@Test
	public void save() throws Exception {
		when(accountRepository.save(any(Account.class)))
			.then(AdditionalAnswers.returnsFirstArg());
		mvc.perform(post("/accounts")
				.param("name", "test"))
			.andExpect(redirectedUrl("/accounts"));
		verify(accountRepository).save(any(Account.class));
	}

	/*
	@Test
	public void saveWithErrors() throws Exception {
		mvc.perform(post("/accounts"))
			.andExpect(redirectedUrl("/accounts"));
	}
	*/

	@Test
	public void deleteExisting() throws Exception {
		Account account = new Account("test");
		when(accountRepository.findById(id))
			.thenReturn(Optional.of(account));
		mvc.perform(delete("/accounts/{id}", id))
			.andExpect(redirectedUrl("/accounts"));
		verify(accountRepository).findById(eq(id));
		verify(accountRepository).delete(eq(account));
	}

	@Test
	public void deleteNotFound() throws Exception {
		Account account = new Account("test");
		when(accountRepository.findById(id))
			.thenReturn(Optional.empty());
		mvc.perform(delete("/accounts/{id}", id))
			.andExpect(status().isNotFound());
		verify(accountRepository).findById(eq(id));
	}

}
