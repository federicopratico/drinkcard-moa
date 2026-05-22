package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination;

import java.util.Locale;
import java.util.Set;

public final class PageSortParser {

    private PageSortParser() {}

    public static PageSort parse(
            int page,
            int size,
            String sort,
            String defaultSortBy,
            String defaultSortDirection,
            int defaultPage,
            int defaultSize,
            int maxSize,
            Set<String> allowedSortFields,
            String resourceName
    ) {
        SortParts sortParts = parseSort(sort, defaultSortBy, defaultSortDirection, allowedSortFields, resourceName);

        return new PageSort(
                normalizePage(page, defaultPage),
                normalizeSize(size, defaultSize, maxSize),
                sortParts.sortBy(),
                sortParts.sortDirection()
        );
    }

    private static int normalizePage(int page, int defaultPage) {
        return page < 0 ? defaultPage : page;
    }

    private static int normalizeSize(int size, int defaultSize, int maxSize) {
        if (size <= 0) {
            return defaultSize;
        }

        return Math.min(size, maxSize);
    }

    private static SortParts parseSort(
            String sort,
            String defaultSortBy,
            String defaultSortDirection,
            Set<String> allowedSortFields,
            String resourceName
    ) {
        if (sort == null || sort.isBlank()) {
            return new SortParts(defaultSortBy, defaultSortDirection);
        }

        String[] parts = sort.split(",");
        String sortBy = parts[0].trim();
        String sortDirection = parts.length > 1
                ? parts[1].trim().toLowerCase(Locale.ROOT)
                : defaultSortDirection;

        if (!allowedSortFields.contains(sortBy)) {
            throw new IllegalArgumentException("Unsupported " + resourceName + " sort field: " + sortBy);
        }

        if (!sortDirection.equals("asc") && !sortDirection.equals("desc")) {
            throw new IllegalArgumentException("Unsupported " + resourceName + " sort direction: " + sortDirection);
        }

        return new SortParts(sortBy, sortDirection);
    }

    private record SortParts(String sortBy, String sortDirection) {
    }
}
