package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidNameException;

public record FullName(String firstName, String lastName) {

    public FullName {

        if(firstName == null || firstName.isBlank()) throw new InvalidNameException("First name is required");
        if(lastName == null || lastName.isBlank()) throw new InvalidNameException("Last name is required");

        firstName = firstName.trim();
        lastName = lastName.trim();
    }

    public String asString() {
        return firstName + " " + lastName;
    }
}
