package domainobjectsmvc.webmvc;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import domainobjectsmvc.domain.model.VersionedEntity;
import domainobjectsmvc.domain.model.VersionedEntityRepository;

@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@WebMvcTest(VersionedEntitiesController.class)
@EnableSpringDataWebSupport
public class VersionedEntitiesControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private VersionedEntityRepository entityRepository;

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
		List<VersionedEntity> entities = new LinkedList<>();
		Page<VersionedEntity> entitiesPage = new PageImpl<>(entities);
		when(entityRepository.findAll(any(Pageable.class)))
			.thenReturn(entitiesPage);
		mvc.perform(get("/versioned-entities"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("entitiesPage", is(entitiesPage)))
			.andExpect(model().attribute("entities", is(entities)))
			.andExpect(model().attributeDoesNotExist("entity"))
			.andExpect(view().name("versioned-entities/list"));
	}

	@Test
	public void update() throws Exception {
		final int version = 42;
		VersionedEntity entity = new VersionedEntity();
		ReflectionTestUtils.setField(entity, "version", version);
		when(entityRepository.findById(id))
			.thenReturn(Optional.of(entity));
		mvc.perform(put("/versioned-entities/{id}", id)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("version", String.valueOf(version)) // input type="hidden"
				.param("data", "Lorem ipsum"))
			.andExpect(redirectedUrl("/versioned-entities"));
		verify(entityRepository).findById(eq(id));
		verify(entityRepository).save(eq(entity));
		assertEquals("Lorem ipsum", entity.getData());
	}

	@Test
	public void updateWithOptimisticLockingFailure() throws Exception {
		final int version = 42;
		VersionedEntity entity = new VersionedEntity();
		ReflectionTestUtils.setField(entity, "version", version);
		when(entityRepository.findById(id))
			.thenReturn(Optional.of(entity));
		mvc.perform(put("/versioned-entities/{id}", id)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("version", String.valueOf(version - 1)) // input type="hidden"
				.param("data", "Lorem ipsum"))
			.andExpect(status().isOk())
			.andExpect(view().name("versioned-entities/edit"))
			.andExpect(model().hasErrors());
		verify(entityRepository).findById(eq(id));
		verify(entityRepository, times(0)).save(any(VersionedEntity.class));
	}

	// For the rest of the tests, please refer to other xxxControllerTests

}
