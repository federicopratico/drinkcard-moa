package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event;

import java.time.Instant;

public record ResetPasswordEvent(
        String passwordResetRequestId,
        String email,
        String passwordResetToken,
        Instant occurredOn
) implements DomainEvent {}
