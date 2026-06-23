package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.dto.command;

import jakarta.validation.constraints.Email;

public record SendResetPasswordEmailCommand(
        @Email
        String email,
        String passwordResetToken
) {
}
