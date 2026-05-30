package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.infrastructure.out.event;

import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.dto.command.SendInvitationEmailCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.usecase.SendInvitationEmailUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.UserInvitedEvent;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.UserRegisteredEvent;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreateDrinkCardAccountCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateDrinkCardAccountResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.CreateDrinkCardAccountUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messagingIamEventListener")
@RequiredArgsConstructor
public class IamEventListener {

    private static final Logger log = LoggerFactory.getLogger(IamEventListener.class);

    private final SendInvitationEmailUseCase sendInvitationEmailUseCase;

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
}
