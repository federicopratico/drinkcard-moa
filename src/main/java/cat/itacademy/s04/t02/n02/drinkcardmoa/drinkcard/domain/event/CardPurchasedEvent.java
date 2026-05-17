package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.event;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;

public record CardPurchasedEvent(
        String volunteerId,
        int creditsAdded,
        BigDecimal paidAmount,
        Instant occurredOn
) implements DomainEvent {}
