package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void newCard_ShouldCreateCardWithDefaultCreditsAndPrice() {
        Card card = Card.newCard();

        assertAll(
                () -> assertEquals(5, card.getCredits()),
                () -> assertEquals(BigDecimal.TEN, card.getPrice())
        );
    }

    @Test
    void equals_WhenSameObject_ShouldReturnTrue() {
        Card card = Card.newCard();

        assertEquals(card, card);
    }

    @Test
    void equals_WhenCardsHaveSameValues_ShouldReturnTrue() {
        Card firstCard = Card.newCard();
        Card secondCard = Card.newCard();

        assertEquals(firstCard, secondCard);
    }

    @Test
    void equals_WhenObjectIsNull_ShouldReturnFalse() {
        Card card = Card.newCard();

        assertNotEquals(null, card);
    }

    @Test
    void equals_WhenObjectIsDifferentClass_ShouldReturnFalse() {
        Card card = Card.newCard();

        assertNotEquals("not a card", card);
    }

    @Test
    void hashCode_WhenCardsHaveSameValues_ShouldReturnSameHashCode() {
        Card firstCard = Card.newCard();
        Card secondCard = Card.newCard();

        assertEquals(firstCard.hashCode(), secondCard.hashCode());
    }
}