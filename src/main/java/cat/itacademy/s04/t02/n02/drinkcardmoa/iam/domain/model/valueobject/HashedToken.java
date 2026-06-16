package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject;

public record HashedToken(String value) {
    public HashedToken {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Token cannot be empty");
        }
    }

    public static HashedToken from(String value) {
        return new HashedToken(value);
    }

    public String asString() {
        return value;
    }
}
