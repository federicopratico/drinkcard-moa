package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.pagination;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageSortParserTest {

    private static final String DEFAULT_SORT_BY = "createdAt";
    private static final String DEFAULT_SORT_DIRECTION = "desc";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "status",
            "volunteerId"
    );

    @Test
    void parse_WhenSortIsBlank_ShouldReturnDefaultSortValues() {
        PageSort pageSort = parse(1, 10, " ");

        assertAll(
                () -> assertEquals(1, pageSort.page()),
                () -> assertEquals(10, pageSort.size()),
                () -> assertEquals(DEFAULT_SORT_BY, pageSort.sortBy()),
                () -> assertEquals(DEFAULT_SORT_DIRECTION, pageSort.sortDirection())
        );
    }

    @Test
    void parse_WhenPageIsNegative_ShouldReturnDefaultPage() {
        PageSort pageSort = parse(-1, 10, "status,asc");

        assertAll(
                () -> assertEquals(DEFAULT_PAGE, pageSort.page()),
                () -> assertEquals(10, pageSort.size()),
                () -> assertEquals("status", pageSort.sortBy()),
                () -> assertEquals("asc", pageSort.sortDirection())
        );
    }

    @Test
    void parse_WhenSizeIsInvalid_ShouldReturnDefaultSize() {
        PageSort pageSort = parse(0, 0, "status,asc");

        assertAll(
                () -> assertEquals(0, pageSort.page()),
                () -> assertEquals(DEFAULT_SIZE, pageSort.size()),
                () -> assertEquals("status", pageSort.sortBy()),
                () -> assertEquals("asc", pageSort.sortDirection())
        );
    }

    @Test
    void parse_WhenSizeExceedsMaximum_ShouldCapSize() {
        PageSort pageSort = parse(0, 150, "volunteerId,desc");

        assertAll(
                () -> assertEquals(0, pageSort.page()),
                () -> assertEquals(MAX_SIZE, pageSort.size()),
                () -> assertEquals("volunteerId", pageSort.sortBy()),
                () -> assertEquals("desc", pageSort.sortDirection())
        );
    }

    @Test
    void parse_WhenSortFieldIsUnsupported_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parse(0, 20, "unsupported,asc")
        );

        assertEquals("Unsupported resource sort field: unsupported", exception.getMessage());
    }

    @Test
    void parse_WhenSortDirectionIsUnsupported_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parse(0, 20, "createdAt,sideways")
        );

        assertEquals("Unsupported resource sort direction: sideways", exception.getMessage());
    }

    private PageSort parse(int page, int size, String sort) {
        return PageSortParser.parse(
                page,
                size,
                sort,
                DEFAULT_SORT_BY,
                DEFAULT_SORT_DIRECTION,
                DEFAULT_PAGE,
                DEFAULT_SIZE,
                MAX_SIZE,
                ALLOWED_SORT_FIELDS,
                "resource"
        );
    }
}
