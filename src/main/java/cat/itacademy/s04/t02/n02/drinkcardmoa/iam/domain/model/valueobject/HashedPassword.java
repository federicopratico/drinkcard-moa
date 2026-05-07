package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject;

public record HashedPassword(String value) {

    public HashedPassword {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
    }

    public static HashedPassword from(String value) {
        return new HashedPassword(value);
    }
}
