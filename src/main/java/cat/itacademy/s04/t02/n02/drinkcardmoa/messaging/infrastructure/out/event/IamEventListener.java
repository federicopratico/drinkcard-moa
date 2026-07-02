package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.infrastructure.out.event;

import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.dto.command.SendInvitationEmailCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.dto.command.SendResetPasswordEmailCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.usecase.SendInvitationEmailUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.usecase.SendPasswordResetEmailUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.ResetPasswordEvent;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.UserInvitedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component("messagingIamEventListener")
@RequiredArgsConstructor
public class IamEventListener {

    private static final Logger log = LoggerFactory.getLogger(IamEventListener.class);

    private final SendInvitationEmailUseCase sendInvitationEmailUseCase;
    private final SendPasswordResetEmailUseCase sendPasswordResetEmailUseCase;

    @EventListener
    @Async
    @Retryable(
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void onUserInvited(UserInvitedEvent event) {

        log.info("Received UserInvitedEvent from IAM context. InvitationID: {}", event.invitationId());

        try {
            SendInvitationEmailCommand command = new SendInvitationEmailCommand(event.email(), event.role(), event.invitationToken());

            var success = sendInvitationEmailUseCase.execute(command);

            log.info("Processed UserInvitedEvent. Sent email for {} role {}, success: {}",
                    command.email(), command.role(), success);
        } catch (Exception e) {
            log.error("Failed to process UserInvitedEvent for email: {}",
                    event.email(), e);

            throw e;
        }
    }

    @EventListener
    @Async
    @Retryable(
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void onResetPassword(ResetPasswordEvent event) {

        log.info("Received ResetPasswordEvent from IAM context. ResetPasswordId: {}", event.passwordResetRequestId());

        try {
            SendResetPasswordEmailCommand cmd = new SendResetPasswordEmailCommand(event.email(), event.passwordResetToken());

            boolean success = sendPasswordResetEmailUseCase.execute(cmd);

            log.info("Processed ResetPasswordEvent. Sent email for {} success: {}",
                    cmd.email(), success);
        } catch (Exception e) {
            log.error("Failed to process ResetPasswordEvent for email: {}",
                    event.email(), e);

            throw e;
        }
    }
}
