package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.RefillDisabledException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRefillDisabled_ShouldMapToUnprocessableEntity() {
        ResponseEntity<Map<String, Object>> response = handler.handleRefillDisabled(
                new RefillDisabledException("Refill is disabled for this DrinkCardAccount.")
        );

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertAll(
                () -> assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode()),
                () -> assertEquals(422, body.get("status")),
                () -> assertEquals("Unprocessable Entity", body.get("error")),
                () -> assertEquals("Refill is disabled for this DrinkCardAccount.", body.get("message"))
        );
    }
}
