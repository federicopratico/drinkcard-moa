package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event;

import java.time.Instant;

public record UserInvitedEvent(
        String invitationId,
        String email,
        String role,
        String invitationToken,
        Instant occurredOn
) implements DomainEvent {}
