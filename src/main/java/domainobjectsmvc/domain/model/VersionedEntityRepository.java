package domainobjectsmvc.domain.model;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface VersionedEntityRepository
		extends PagingAndSortingRepository<VersionedEntity, Long> {

}
