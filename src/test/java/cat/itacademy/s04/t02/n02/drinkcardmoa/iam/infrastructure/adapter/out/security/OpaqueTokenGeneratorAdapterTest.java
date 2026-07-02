package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.security;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.OpaqueTokenGenerator.GeneratedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpaqueTokenGeneratorAdapterTest {

    private final OpaqueTokenGeneratorAdapter generator =
            new OpaqueTokenGeneratorAdapter();

    @Test
    void generate_ShouldReturnRawAndHashedToken() {
        GeneratedToken generated = generator.generate();

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
        GeneratedToken first = generator.generate();
        GeneratedToken second = generator.generate();

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
        HashedToken first = generator.hash("opaque-token");
        HashedToken second = generator.hash("opaque-token");

        assertEquals(first, second);
    }

    @Test
    void hash_WhenInputChanges_ShouldReturnDifferentHash() {
        HashedToken first = generator.hash("opaque-token-one");
        HashedToken second = generator.hash("opaque-token-two");

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
