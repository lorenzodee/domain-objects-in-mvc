package domainobjectsmvc.domain.model;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface AccountRepository
		extends PagingAndSortingRepository<Account, Long> {

}
