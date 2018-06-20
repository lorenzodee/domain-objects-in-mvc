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
import domainobjectsmvc.domain.model.GeneratedIdEntity;
import domainobjectsmvc.domain.model.GeneratedIdEntityRepository;

@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@WebMvcTest(GeneratedIdEntitiesController.class)
@EnableSpringDataWebSupport
public class GeneratedIdEntitiesControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private GeneratedIdEntityRepository entityRepository;

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
		List<GeneratedIdEntity> entities = new LinkedList<>();
		Page<GeneratedIdEntity> entitiesPage = new PageImpl<>(entities);
		when(entityRepository.findAll(any(Pageable.class)))
			.thenReturn(entitiesPage);
		mvc.perform(get("/entities"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("entitiesPage", is(entitiesPage)))
			.andExpect(model().attribute("entities", is(entities)))
			.andExpect(model().attributeDoesNotExist("entity"))
			.andExpect(view().name("entities/list"));
	}

	@Test
	public void show() throws Exception {
		GeneratedIdEntity entity = new GeneratedIdEntity();
		when(entityRepository.findById(id))
			.thenReturn(Optional.of(entity));
		mvc.perform(get("/entities/{id}", id))
			.andExpect(status().isOk())
			.andExpect(model().attribute("entity", is(entity)))
			.andExpect(view().name("entities/show"));
		verify(entityRepository).findById(eq(id));
	}

	@Test
	public void showNotFound() throws Exception {
		when(entityRepository.findById(id))
			.thenReturn(Optional.empty());
		mvc.perform(get("/entities/{id}", id))
			.andExpect(status().isNotFound());
		verify(entityRepository).findById(eq(id));
	}

	@Test
	public void edit() throws Exception {
		GeneratedIdEntity entity = new GeneratedIdEntity();
		when(entityRepository.findById(id))
			.thenReturn(Optional.of(entity));
		mvc.perform(get("/entities/{id}", id).param("edit", ""))
			.andExpect(status().isOk())
			.andExpect(model().attribute("entity", is(entity)))
			.andExpect(view().name("entities/edit"));
	}

	@Test
	public void editNotFound() throws Exception {
		when(entityRepository.findById(id))
			.thenReturn(Optional.empty());
		mvc.perform(get("/entities/{id}", id).param("edit", ""))
			.andExpect(status().isNotFound());
		verify(entityRepository).findById(eq(id));
	}

	@Test
	public void update() throws Exception {
		GeneratedIdEntity entity = new GeneratedIdEntity();
		when(entityRepository.findById(id))
			.thenReturn(Optional.of(entity));
		mvc.perform(put("/entities/{id}", id))
			.andExpect(redirectedUrl("/entities"));
		verify(entityRepository).findById(eq(id));
		verify(entityRepository).save(eq(entity));
	}

	/*
	@Test
	public void updateWithErrors() throws Exception {
		GeneratedIdEntity entity = new GeneratedIdEntity();
		when(accountRepository.findById(id))
			.thenReturn(Optional.of(entity));
		mvc.perform(put("/entities/{id}", id))
			.andExpect(status().isOk())
			.andExpect(model().attribute("entity", is(entity)))
			.andExpect(view().name("entities/edit"));
	}
	*/

	@Test
	public void create() throws Exception {
		mvc.perform(get("/entities").param("create", ""))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("entity"))
			.andExpect(view().name("entities/create"));
	}

	@Test
	public void save() throws Exception {
		when(entityRepository.save(any(GeneratedIdEntity.class)))
			.then(AdditionalAnswers.returnsFirstArg());
		mvc.perform(post("/entities"))
			.andExpect(redirectedUrl("/entities"));
		verify(entityRepository, times(0)).findById(anyLong());
		verify(entityRepository).save(any(GeneratedIdEntity.class));
	}

	/*
	@Test
	public void saveWithErrors() throws Exception {
		mvc.perform(post("/entities"))
			.andExpect(redirectedUrl("/entities"));
	}
	*/

	@Test
	public void deleteExisting() throws Exception {
		GeneratedIdEntity entity = new GeneratedIdEntity();
		when(entityRepository.findById(id))
			.thenReturn(Optional.of(entity));
		mvc.perform(delete("/entities/{id}", id))
			.andExpect(redirectedUrl("/entities"));
		verify(entityRepository).findById(eq(id));
		verify(entityRepository).delete(eq(entity));
	}

	@Test
	public void deleteNotFound() throws Exception {
		GeneratedIdEntity entity = new GeneratedIdEntity();
		when(entityRepository.findById(id))
			.thenReturn(Optional.empty());
		mvc.perform(delete("/entities/{id}", id))
			.andExpect(status().isNotFound());
		verify(entityRepository).findById(eq(id));
	}

}
