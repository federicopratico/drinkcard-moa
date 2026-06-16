package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.security;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import org.junit.jupiter.api.Test;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenGenerator.GeneratedRefreshToken;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenGeneratorAdapterTest {

    private final RefreshTokenGeneratorAdapter generator =
            new RefreshTokenGeneratorAdapter();

    @Test
    void generate_ShouldReturnRawAndHashedToken() {
        GeneratedRefreshToken generated = generator.generate();

        assertAll(
                () -> assertNotNull(generated.rawToken()),
                () -> assertFalse(generated.rawToken().isBlank()),
                () -> assertEquals(43, generated.rawToken().length()),
                () -> assertEquals(64, generated.hashedToken().asString().length()),
                () -> assertEquals(
                        generator.hash(generated.rawToken()),
                        generated.hashedToken()
                )
        );
    }

    @Test
    void generate_WhenCalledTwice_ShouldReturnDifferentTokens() {
        GeneratedRefreshToken first = generator.generate();
        GeneratedRefreshToken second = generator.generate();

        assertAll(
                () -> assertNotEquals(first.rawToken(), second.rawToken()),
                () -> assertNotEquals(
                        first.hashedToken(),
                        second.hashedToken()
                )
        );
    }

    @Test
    void hash_WhenInputIsTheSame_ShouldBeDeterministic() {
        HashedToken first = generator.hash("refresh-token");
        HashedToken second = generator.hash("refresh-token");

        assertEquals(first, second);
    }

    @Test
    void hash_WhenInputChanges_ShouldReturnDifferentHash() {
        HashedToken first = generator.hash("refresh-token-one");
        HashedToken second = generator.hash("refresh-token-two");

        assertNotEquals(first, second);
    }

    @Test
    void hash_ShouldProduceLowercaseSha256Hex() {
        HashedToken result = generator.hash("test");

        assertEquals(
                "9f86d081884c7d659a2feaa0c55ad015"
                        + "a3bf4f1b2b0b822cd15d6c15b0f00a08",
                result.asString()
        );
    }

    @Test
    void hash_WhenInputIsNull_ShouldThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.hash(null)
        );
    }

    @Test
    void hash_WhenInputIsBlank_ShouldThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.hash(" ")
        );
    }


}