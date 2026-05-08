package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Card {

    private static final int DEFAULT_CREDITS = 5;
    private static final BigDecimal PRICE_IN_EUROS = new BigDecimal(10);

    private final int credits;
    private final BigDecimal price;

    private Card(int credits, BigDecimal price) {
        this.credits = credits;
        this.price = price;
    }

    public static Card newCard() {
        return new Card(DEFAULT_CREDITS, PRICE_IN_EUROS);
    }

    public int getCredits() {
        return credits;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return getCredits() == card.getCredits() && Objects.equals(getPrice(), card.getPrice());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCredits(), getPrice());
    }
}
