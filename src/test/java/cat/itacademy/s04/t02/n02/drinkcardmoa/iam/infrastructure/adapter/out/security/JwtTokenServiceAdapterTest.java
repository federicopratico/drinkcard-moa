package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.security;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidTokenException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceAdapterTest {

    private static final String SECRET =
            "z6B98vN2qR5sT8uW1xZ4A7dGjKmNpQtReGuHwJxKzM5P7S9YbcEeFhJkMnPrStUw";

    @Test
    void generateToken_WhenUserIsValid_ReturnValidToken() {
        JwtTokenServiceAdapter tokenService = new JwtTokenServiceAdapter(SECRET, 1440);
        User user = createUser();

        String token = tokenService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(tokenService.validateToken(token));
    }

    @Test
    void extractClaims_WhenTokenIsValid_ReturnUserData() {
        JwtTokenServiceAdapter tokenService = new JwtTokenServiceAdapter(SECRET, 1440);
        User user = createUser();

        String token = tokenService.generateToken(user);

        assertEquals(user.getId(), tokenService.extractVolunteerID(token));
        assertEquals(user.getEmail().asString(), tokenService.extractEmail(token));
        assertEquals(user.getRole().name(), tokenService.extractRole(token));
    }

    @Test
    void validateToken_WhenTokenIsMalformed_ReturnFalse() {
        JwtTokenServiceAdapter tokenService = new JwtTokenServiceAdapter(SECRET, 1440);

        boolean result = tokenService.validateToken("invalid-token");

        assertFalse(result);
    }

    @Test
    void validateToken_WhenTokenIsExpired_ReturnFalse() {
        JwtTokenServiceAdapter tokenService = new JwtTokenServiceAdapter(SECRET, -1);
        User user = createUser();

        String token = tokenService.generateToken(user);

        assertFalse(tokenService.validateToken(token));
    }

    @Test
    void extractClaims_WhenTokenIsInvalid_ThrowInvalidTokenException() {
        JwtTokenServiceAdapter tokenService = new JwtTokenServiceAdapter(SECRET, 1440);

        assertThrows(InvalidTokenException.class, () -> {
            tokenService.extractEmail("invalid-token");
        });
    }

    @Test
    void constructor_WhenSecretIsTooShort_ThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new JwtTokenServiceAdapter("short-secret", 1440);
        });
    }

    private User createUser() {
        return User.rehydrate(
                VolunteerID.generate(),
                FullName.from("First", "Last"),
                Email.from("user@email.com"),
                HashedPassword.from("hashed_password"),
                Role.VOLUNTEER
        );
    }
}
