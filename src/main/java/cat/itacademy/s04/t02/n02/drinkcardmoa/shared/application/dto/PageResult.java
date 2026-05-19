package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
