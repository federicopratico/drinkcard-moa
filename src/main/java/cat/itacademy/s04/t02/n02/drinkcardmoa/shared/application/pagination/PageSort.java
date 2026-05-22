package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination;

public record PageSort(
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
}
