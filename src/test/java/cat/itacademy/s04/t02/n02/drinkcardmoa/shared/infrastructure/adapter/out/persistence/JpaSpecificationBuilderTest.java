package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.out.persistence;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaSpecificationBuilderTest {

    @Test
    void build_WhenValuesAreNull_ShouldIgnoreFilters() {
        CriteriaContext context = criteriaContext();
        Specification<TestEntity> specification = JpaSpecificationBuilder.<TestEntity>builder()
                .equal("volunteerId", null)
                .greaterThanOrEqualTo("createdAt", null)
                .lessThanOrEqualTo("createdAt", null)
                .build();

        Predicate predicate = specification.toPredicate(
                context.root(),
                context.query(),
                context.criteriaBuilder()
        );

        assertNotNull(predicate);
        verify(context.criteriaBuilder()).conjunction();
        verify(context.root(), never()).get(any(String.class));
        verify(context.criteriaBuilder(), never()).equal(any(), any());
        verify(context.criteriaBuilder(), never()).greaterThanOrEqualTo(any(Path.class), any(Comparable.class));
        verify(context.criteriaBuilder(), never()).lessThanOrEqualTo(any(Path.class), any(Comparable.class));
    }

    @Test
    void build_WhenValuesAreProvided_ShouldCreateEqualAndDateRangePredicates() {
        CriteriaContext context = criteriaContext();
        Path<Object> volunteerPath = path();
        Path<Instant> createdAtPath = path();
        Object volunteerId = new Object();
        Instant from = Instant.parse("2026-05-01T00:00:00Z");
        Instant to = Instant.parse("2026-05-19T23:59:59Z");

        doReturn(volunteerPath).when(context.root()).get("volunteerId");
        doReturn(createdAtPath).when(context.root()).get("createdAt");
        when(context.criteriaBuilder().equal(volunteerPath, volunteerId)).thenReturn(mock(Predicate.class));
        when(context.criteriaBuilder().greaterThanOrEqualTo(createdAtPath, from)).thenReturn(mock(Predicate.class));
        when(context.criteriaBuilder().lessThanOrEqualTo(createdAtPath, to)).thenReturn(mock(Predicate.class));

        Specification<TestEntity> specification = JpaSpecificationBuilder.<TestEntity>builder()
                .equal("volunteerId", volunteerId)
                .greaterThanOrEqualTo("createdAt", from)
                .lessThanOrEqualTo("createdAt", to)
                .build();

        Predicate predicate = specification.toPredicate(
                context.root(),
                context.query(),
                context.criteriaBuilder()
        );

        assertNotNull(predicate);
        verify(context.root()).get("volunteerId");
        verify(context.root(), times(2)).get("createdAt");
        verify(context.criteriaBuilder()).equal(volunteerPath, volunteerId);
        verify(context.criteriaBuilder()).greaterThanOrEqualTo(createdAtPath, from);
        verify(context.criteriaBuilder()).lessThanOrEqualTo(createdAtPath, to);
    }

    private CriteriaContext criteriaContext() {
        Root<TestEntity> root = mock();
        CriteriaQuery<?> query = mock();
        CriteriaBuilder criteriaBuilder = mock();

        when(criteriaBuilder.conjunction()).thenReturn(mock(Predicate.class));
        lenient().when(criteriaBuilder.and(any(Predicate.class), any(Predicate.class))).thenReturn(mock(Predicate.class));

        return new CriteriaContext(root, query, criteriaBuilder);
    }

    @SuppressWarnings("unchecked")
    private <T> Path<T> path() {
        return mock(Path.class);
    }

    private record CriteriaContext(
            Root<TestEntity> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder
    ) {
    }

    private static class TestEntity {
    }
}
