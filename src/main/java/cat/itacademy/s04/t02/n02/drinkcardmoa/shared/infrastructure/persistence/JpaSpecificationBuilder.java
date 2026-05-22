package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.persistence;

import org.springframework.data.jpa.domain.Specification;

public class JpaSpecificationBuilder<T> {
    private Specification<T> specification = ((root, query, criteriaBuilder) -> criteriaBuilder.conjunction());

    private JpaSpecificationBuilder() {};

    public static <T> JpaSpecificationBuilder<T> builder() {
        return new JpaSpecificationBuilder<>();
    }

    public JpaSpecificationBuilder<T> equal(String field, Object value) {
        if (field != null) {
            specification = specification.and(((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(field), value)));
        }
        return this;
    }

    public <V extends Comparable<? super V>> JpaSpecificationBuilder<T> greaterThanOrEqualTo(String field, V value) {
        if (field != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get(field), value));
        }
        return this;
    }

    public <V extends Comparable<? super V>> JpaSpecificationBuilder<T> lessThanOrEqualTo(String field, V value) {
        if (field != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get(field), value));
        }
        return this;
    }

    public Specification<T> build() {
        return specification;
    }
}
