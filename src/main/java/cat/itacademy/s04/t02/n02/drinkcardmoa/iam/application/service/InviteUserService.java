package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.InviteUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.InvitationResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.LoginUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.InviteUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.EventPublisher;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.InvitationRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.EmailAlreadyExistsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvitationAlreadyAcceptedException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.Invitation;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.InvitationStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.InvitationToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.InvitationID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.UserInvitedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InviteUserService implements InviteUserUseCase {

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final EventPublisher eventPublisher;

    @Override
    public InvitationResult execute(InviteUserCommand cmd) {
        var email = Email.from(cmd.email());
        var exists = userRepository.existsByEmail(email);
        if (exists) {
            throw new EmailAlreadyExistsException("The provided email already exists");
        }
        var invitation = invitationRepository
                .findInvitationByEmail(email)
                .map(found -> {
                    if (found.isAccepted()) {
                        throw new InvitationAlreadyAcceptedException("The invitation is already accepted");
                    }

                    found.refresh(InvitationToken.generate());
                    return found;
                }).orElseGet(() -> Invitation.create(
                        InvitationID.generate(),
                        email,
                        Role.VOLUNTEER,
                        InvitationToken.generate()
                ));

        invitationRepository.save(invitation);

        eventPublisher.publish(new UserInvitedEvent(
                invitation.getId().asString(),
                invitation.getEmail().asString(),
                invitation.getRole().name(),
                invitation.getInvitationToken().asString(),
                Instant.now()
        ));

        return InvitationResult.from(invitation);
    }
}
