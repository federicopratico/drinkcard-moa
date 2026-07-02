package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.InitiatePasswordResetCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.InitiatePasswordResetUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.EventPublisher;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.OpaqueTokenGenerator;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordResetRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.PasswordReset;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.config.PasswordResetProperties;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.PasswordResetID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.ResetPasswordEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InitiatePasswordResetService implements InitiatePasswordResetUseCase {

    private static final Logger log = LoggerFactory.getLogger(InitiatePasswordResetService.class);

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final OpaqueTokenGenerator opaqueTokenGenerator;
    private final EventPublisher eventPublisher;
    private final PasswordResetProperties passwordResetProperties;

    @Transactional
    @Override
    public void execute(InitiatePasswordResetCommand cmd) {
        Instant now = Instant.now();
        Duration expirationDuration = Duration.ofMinutes(passwordResetProperties.expirationMinutes());

        Email email = Email.from(cmd.email());

        if(!userRepository.existsByEmail(email)) {
            return;
        }

        var passwordResetToken = opaqueTokenGenerator.generate();

        PasswordReset passwordReset = PasswordReset.create(
                PasswordResetID.generate(),
                email,
                passwordResetToken.hashedToken(),
                now,
                now.plusSeconds(expirationDuration.toSeconds())
        );

        passwordResetRepository.save(passwordReset);

        eventPublisher.publish(new ResetPasswordEvent(
                passwordReset.getPasswordResetId().asString(),
                email.asString(),
                passwordResetToken.rawToken(),
                now
        ));

        log.info("Password reset initiated: {}", passwordReset.getPasswordResetId().asString());
    }
}
