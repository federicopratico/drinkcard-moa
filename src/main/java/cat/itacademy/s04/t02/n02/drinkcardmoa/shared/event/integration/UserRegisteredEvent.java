package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.integration;

import java.time.Instant;

public record UserRegisteredEvent(
        String VolunteerId,
        String email,
        String role,
        Instant registeredAt
) {}
