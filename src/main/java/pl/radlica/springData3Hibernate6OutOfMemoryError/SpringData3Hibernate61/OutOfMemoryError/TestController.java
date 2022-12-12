package pl.radlica.springData3Hibernate6OutOfMemoryError.SpringData3Hibernate61.OutOfMemoryError;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static jakarta.persistence.criteria.JoinType.LEFT;
import static java.util.Objects.nonNull;

@RestController
class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);
    public static final String TYPE_A = "Type-A";
    public static final String TYPE_B = "Type-B";

    private final TestEntityRepo testEntityRepo;

    private final byte[] byteArrayOfSize1MB;

    TestController(TestEntityRepo testEntityRepo, @Value("classpath:1MB.file") Resource resource) {
        this.testEntityRepo = testEntityRepo;
        this.byteArrayOfSize1MB = getByteArrayOfSize1MB(resource);
    }


    @GetMapping("/create-new-entity")
    public void createNewEntity(@RequestParam(value = "n", required = false) Long numberOfEntities) {

        if (nonNull(numberOfEntities)) {
            for (int i = 0; i < numberOfEntities; i++) {
                log.info("Saving : {}/{}", i, numberOfEntities);
                testEntityRepo.save(newEntityWithRandomValues());
            }
        } else {
            testEntityRepo.save(new TestEntity());
        }
    }

    @GetMapping("/list-entities")
    public Page<TestEntity> listEntities(@RequestParam(value = "joinType") String joinType,
                                         Pageable pageable) {

        Specification specWithJoin = Specification.where((Specification<Object>) (root, query, criteriaBuilder) -> {
            query.distinct(true);
            Join<Object, Object> join = root.join("relatedEntities", LEFT);
            return criteriaBuilder.equal(join.get("type"), TYPE_A);
        });

        Specification specWithFetchJoin = Specification.where((Specification<Object>) (root, query, criteriaBuilder) -> {
            query.distinct(true);

            // if the result type is a Long it means that it's a count query
            Path<Object> join = query.getResultType().equals(Long.class) ? root.join("relatedEntities", LEFT) : (Path<Object>) root.fetch("relatedEntities", LEFT);
            return criteriaBuilder.equal((join).get("type"), TYPE_A);
        });

        Specification specWithoutJoin = Specification.where(
                (Specification<Object>) (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(criteriaBuilder.literal(1), criteriaBuilder.literal(1))
        );

        Specification specToBeUsed = switch (joinType) {
            case "none" -> specWithoutJoin;
            case "regular" -> specWithJoin;
            case "fetch" -> specWithFetchJoin;
            default -> throw new RuntimeException("Unsupported join type: " + joinType);
        };

        return testEntityRepo.findAll(specToBeUsed, pageable);
    }

    private TestEntity newEntityWithRandomValues() {
        TestEntity testEntity = new TestEntity(
                UUID.randomUUID().toString()
        );
        AnotherEntity anotherEntity = new AnotherEntity(
                testEntity,
                ThreadLocalRandom.current().nextBoolean() ? TYPE_A : TYPE_B,
                byteArrayOfSize1MB
        );

        testEntity.addRelatedEntity(anotherEntity);

        return testEntity;
    }

    private static byte[] getByteArrayOfSize1MB(Resource resource) {
        try {
            return Files.readAllBytes(resource.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

