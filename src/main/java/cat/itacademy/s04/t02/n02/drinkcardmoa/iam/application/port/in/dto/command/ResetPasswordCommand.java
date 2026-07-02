package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command;

public record ResetPasswordCommand(
        String rawToken,
        String newPassword
) {
}
