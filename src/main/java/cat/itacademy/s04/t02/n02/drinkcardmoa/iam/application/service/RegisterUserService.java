package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.RegisterUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.RegisterUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.RegisterUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.EventPublisher;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.InvitationRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordEncoder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.EmailAlreadyExistsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvitationAlreadyAcceptedException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvitationNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.InvitationToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.UserRegisteredEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public RegisterUserResult execute(RegisterUserCommand cmd) {
        var invitation = invitationRepository.findInvitationByToken(InvitationToken.from(cmd.invitationToken()))
                .orElseThrow(() -> new InvitationNotFoundException("No invitation found for the provided token"));

        if (invitation.isAccepted()) {
            throw new InvitationAlreadyAcceptedException("The invitation associated with this token has already been accepted");
        }

        validateEmailUniqueness(invitation.getEmail());

        User user = mapToDomain(cmd, invitation.getEmail().asString());
        User savedUser = userRepository.save(user);
        invitation.accept();
        invitationRepository.save(invitation);

        eventPublisher.publish(new UserRegisteredEvent(
                savedUser.getId().asString(),
                savedUser.getEmail().asString(),
                savedUser.getRole().name(),
                Instant.now()
        ));

        return mapToResult(savedUser);
    }

    private void validateEmailUniqueness(Email email) {
        if(userRepository.existsByEmail(email))
            throw new EmailAlreadyExistsException("The provided email already exists");
    }

    private User mapToDomain(RegisterUserCommand cmd, String email) {
        return User.create(
                VolunteerID.generate(),
                FullName.from(cmd.firstName(), cmd.lastName()),
                Email.from(email),
                HashedPassword.from(passwordEncoder.encode(cmd.password())),
                Role.valueOf(cmd.role().toUpperCase())
        );
    }

    private RegisterUserResult mapToResult(User user) {
        return RegisterUserResult.from(user);
    }
}
