package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

public record CardPurchasedEvent(
        String volunteerId,
        int creditsAdded,
        BigDecimal paidAmount,
        Instant timestamp
) {}
