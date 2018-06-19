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

import domainobjectsmvc.domain.model.GeneratedIdEntity;

@Controller
@RequestMapping("/entities")
public class GeneratedIdEntitiesController {

	private PagingAndSortingRepository<GeneratedIdEntity, Long> entityRepository;

	@Autowired
	public GeneratedIdEntitiesController(
			PagingAndSortingRepository<GeneratedIdEntity, Long> entityRepository) {
		this.entityRepository = entityRepository;
	}

	@GetMapping
	public String list(Pageable pageable, Model model) {
		Page<GeneratedIdEntity> entitiesPage = entityRepository.findAll(pageable);
		model.addAttribute("entitiesPage", entitiesPage);
		model.addAttribute("entities", entitiesPage.getContent());
		return "entities/list";
	}

	@ModelAttribute("entity")
	public GeneratedIdEntity populateModel(
			@PathVariable(required=false) Long id,
			@RequestParam Map<String, String> params,
			HttpMethod httpMethod) {
		// Case 1: GET /entities/{id}?edit, PUT /entities/{id}, and DELETE /entities/{id}
		if (id != null) {
			return entityRepository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException());
		}
		// Case 2: GET /entities?create and POST /entities
		if ((httpMethod == HttpMethod.GET && params.containsKey("create"))
				|| httpMethod == HttpMethod.POST) {
			return new GeneratedIdEntity();
		}
		// Case 3: GET /entities and all other GET requests
		return null;
	}

	@GetMapping("/{id}")
	public String show(@PathVariable Long id
			/* , @ModelAttribute GeneratedIdEntity entity */) {
		return "entities/show";
	}

	@GetMapping(path="/{id}", params="edit")
	public String edit(@PathVariable Long id
			/* , @ModelAttribute GeneratedIdEntity entity */) {
		return "entities/edit";
	}

	@PutMapping("/{id}")
	public String update(@PathVariable Long id,
			@ModelAttribute @Valid GeneratedIdEntity entity, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "entities/edit";
		}
		entityRepository.save(entity);
		return "redirect:/entities";
	}

	@GetMapping(params="create")
	public String create(GeneratedIdEntity entity, Model model) {
		model.addAttribute("entity", entity);
		return "entities/create";
	}

	@PostMapping
	public String save(
			@ModelAttribute @Valid GeneratedIdEntity entity, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "entities/edit";
		}
		entityRepository.save(entity);
		return "redirect:/entities";
	}

	@DeleteMapping("/{id}")
	public String delete(@ModelAttribute GeneratedIdEntity entity) {
		entityRepository.delete(entity);
		return "redirect:/entities";
	}

}
