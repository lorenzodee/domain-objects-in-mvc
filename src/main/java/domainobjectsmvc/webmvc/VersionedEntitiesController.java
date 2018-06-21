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

import domainobjectsmvc.domain.model.VersionedEntity;

@Controller
@RequestMapping("/versioned-entities")
public class VersionedEntitiesController {

	private PagingAndSortingRepository<VersionedEntity, Long> entityRepository;

	@Autowired
	public VersionedEntitiesController(
			PagingAndSortingRepository<VersionedEntity, Long> entityRepository) {
		this.entityRepository = entityRepository;
	}

	@GetMapping
	public String list(Pageable pageable, Model model) {
		Page<VersionedEntity> entitiesPage = entityRepository.findAll(pageable);
		model.addAttribute("entitiesPage", entitiesPage);
		model.addAttribute("entities", entitiesPage.getContent());
		return "versioned-entities/list";
	}

	@ModelAttribute("entity")
	public VersionedEntity populateModel(
			@PathVariable(required=false) Long id,
			@RequestParam Map<String, String> params,
			HttpMethod httpMethod) {
		// Case 1: GET /versioned-entities/{id}?edit,
		// PUT /versioned-entities/{id}, and DELETE /versioned-entities/{id}
		if (id != null) {
			VersionedEntity entity = entityRepository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException());
			/*
			int version = Integer.valueOf(params.get("version"));
			if (entity.getVersion() != version) {
				// throw new OptimisticLockingFailureException("...");
			}
			*/
			return entity;
		}
		// Case 2: GET /versioned-entities?create and POST /versioned-entities
		if ((httpMethod == HttpMethod.GET && params.containsKey("create"))
				|| httpMethod == HttpMethod.POST) {
			return new VersionedEntity();
		}
		// Case 3: GET /versioned-entities and all other GET requests
		return null;
	}

	/*
	@ResponseStatus(code=HttpStatus.PRECONDITION_FAILED)
	@ExceptionHandler({ OptimisticLockingFailureException.class })
	public void handleOptimisticLockingFailureException() {}
	*/

	@GetMapping("/{id}")
	public String show(@PathVariable Long id
			/* , @ModelAttribute("entity") VersionedEntity entity */) {
		return "versioned-entities/show";
	}

	@GetMapping(path="/{id}", params="edit")
	public String edit(@PathVariable Long id
			/* , @ModelAttribute("entity") VersionedEntity entity */) {
		return "versioned-entities/edit";
	}

	@PutMapping("/{id}")
	public String update(@PathVariable Long id,
			@ModelAttribute("entity") @Valid VersionedEntity entity, BindingResult bindingResult,
			@RequestParam int version) {
		if (entity.getVersion() != version) {
			bindingResult.reject("error.version",
					"This has been modified since you last retrieved it");
		}
		if (bindingResult.hasErrors()) {
			return "versioned-entities/edit";
		}
		entityRepository.save(entity);
		return "redirect:/versioned-entities";
	}

	@GetMapping(params="create")
	public String create(
			/* @ModelAttribute("entity") VersionedEntity entity, Model model */) {
		// model.addAttribute("entity", entity);
		return "versioned-entities/create";
	}

	@PostMapping
	public String save(
			@ModelAttribute("entity") @Valid VersionedEntity entity,
			BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "versioned-entities/edit";
		}
		entityRepository.save(entity);
		return "redirect:/versioned-entities";
	}

	@DeleteMapping("/{id}")
	public String delete(@ModelAttribute("entity") VersionedEntity entity) {
		entityRepository.delete(entity);
		return "redirect:/versioned-entities";
	}

}
