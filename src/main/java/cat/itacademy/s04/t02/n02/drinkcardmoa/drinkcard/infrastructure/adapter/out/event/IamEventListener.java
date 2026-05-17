package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.event;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.UserRegisteredEvent;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreateVolunteerCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateVolunteerResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.CreateVolunteerUseCase;
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

@Component
@RequiredArgsConstructor
public class IamEventListener {

    private static final Logger log = LoggerFactory.getLogger(IamEventListener.class);

    private final CreateVolunteerUseCase createVolunteerUseCase;

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void onUserRegistered(UserRegisteredEvent event) {

        log.info("Received UserRegisteredEvent from IAM context. VolunteerId: {}", event.volunteerId());

        try {
            CreateVolunteerCommand command = new CreateVolunteerCommand(event.volunteerId());

            CreateVolunteerResult result = createVolunteerUseCase.execute(command);

            log.info("Successfully processed UserRegisteredEvent. Created volunteer with id {} and {} credits",
                    result.volunteerID() ,result.credits());
        } catch (Exception e) {
            log.error("Failed to process UserRegisteredEvent for volunteerId: {}",
                    event.volunteerId(), e);

            // here manage 3 times failure and fix eventual consistency
            throw e;
        }
    }
}
