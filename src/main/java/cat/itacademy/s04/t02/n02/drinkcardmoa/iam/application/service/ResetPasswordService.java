package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.ResetPasswordCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.ResetPasswordUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.OpaqueTokenGenerator;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordEncoder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordResetRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidTokenException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.PasswordReset;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordService.class);

    private final UserRepository userRepository;
    private final OpaqueTokenGenerator opaqueTokenGenerator;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    @Override
    public void execute(ResetPasswordCommand cmd) {
        Instant now = Instant.now();
        HashedToken hashedToken = opaqueTokenGenerator.hash(cmd.rawToken());

        PasswordReset passwordReset = passwordResetRepository.findByPasswordResetToken(hashedToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid password reset token"));

        if (!passwordReset.isUsable(now)) {
            throw new InvalidTokenException("Invalid password reset token");
        }

        User user = userRepository.findUserByEmail(passwordReset.getEmail())
                .orElseThrow(() -> new InvalidTokenException("Invalid password reset token"));

        user.changePassword(HashedPassword.from(passwordEncoder.encode(cmd.newPassword())));
        passwordReset.markAsUsed(now);

        userRepository.save(user);
        passwordResetRepository.save(passwordReset);
        passwordResetRepository.revokePendingByEmailExceptCurrent(
                passwordReset.getEmail(),
                passwordReset.getPasswordResetId()
        );

        log.info("Password reset completed: {}", passwordReset.getPasswordResetId().asString());
    }
}
