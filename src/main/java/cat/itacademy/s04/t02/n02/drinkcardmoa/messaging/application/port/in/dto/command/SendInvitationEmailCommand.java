package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.dto.command;

public record SendInvitationEmailCommand(
        String email,
        String role,
        String invitationToken
) {
}
