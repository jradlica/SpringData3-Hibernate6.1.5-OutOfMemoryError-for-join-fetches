
package pl.radlica.springData3Hibernate6OutOfMemoryError.SpringData3Hibernate61.OutOfMemoryError;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TestEntityRepo extends JpaRepository<TestEntity, Long>, JpaSpecificationExecutor<TestEntity> { }
