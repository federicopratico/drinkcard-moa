package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event;

import java.time.Instant;

public record UserRegisteredEvent(
        String volunteerId,
        String email,
        String role,
        Instant occurredOn
) implements DomainEvent {}
