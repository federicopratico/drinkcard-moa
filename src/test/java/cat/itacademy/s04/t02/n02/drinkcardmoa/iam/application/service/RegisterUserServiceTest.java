package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.RegisterUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.RegisterUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.EventPublisher;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.InvitationRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordEncoder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.EmailAlreadyExistsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvitationAlreadyAcceptedException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvitationNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.Invitation;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.InvitationToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.InvitationID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private RegisterUserService registerUserService;


    @Test
    void execute_WhenInvitationIsAlreadyAccepted_ThrowsInvitationAlreadyAcceptedException() {
        RegisterUserCommand cmd = new RegisterUserCommand(
                "first",
                "last",
                "password",
                "VOLUNTEER",
                "token");

        var invitation = Invitation.create(
                InvitationID.generate(),
                Email.from("first_last@userId.com"),
                Role.valueOf(cmd.role().toUpperCase()),
                InvitationToken.from(cmd.invitationToken())
        );
        invitation.accept();

        when(invitationRepository.findInvitationByToken(any(InvitationToken.class)))
                .thenReturn(Optional.of(invitation));

        assertThrows(InvitationAlreadyAcceptedException.class, () -> registerUserService.execute(cmd));
    }

    @Test
    void execute_WhenInvitationIsNotPresent_ThrowsInvitationNotFoundException() {
        RegisterUserCommand cmd = new RegisterUserCommand(
                "first",
                "last",
                "password",
                "VOLUNTEER",
                "token");

        when(invitationRepository.findInvitationByToken(any(InvitationToken.class)))
                .thenReturn(Optional.empty());

        assertThrows(InvitationNotFoundException.class, () -> registerUserService.execute(cmd));
    }

    @Test
    void execute_WhenEmailDoesNotExistAndValidInput_CreateNewUser() {
        RegisterUserCommand cmd = new RegisterUserCommand(
                "first",
                "last",
                "password",
                "VOLUNTEER",
                "token");

        when(invitationRepository.findInvitationByToken(any(InvitationToken.class)))
                .thenReturn(
                        Optional.of(Invitation.create(
                                InvitationID.generate(),
                                Email.from("first_last@userId.com"),
                                Role.valueOf(cmd.role().toUpperCase()),
                                InvitationToken.from(cmd.invitationToken())
                        ))
                );
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(i-> i.getArguments()[0]);

        RegisterUserResult result = registerUserService.execute(cmd);

        assertNotNull(result);
        assertEquals(cmd.firstName(), result.firstName());
        assertEquals(cmd.lastName(), result.lastName());
        assertEquals("first_last@userid.com", result.email());

        verify(userRepository, times(1)).existsByEmail(any(Email.class));
        verify(passwordEncoder, times(1)).encode(cmd.password());
        verify(userRepository, times(1)).save(any(User.class));
        verify(invitationRepository, times(1)).save(
                argThat(invitation ->
                        invitation.getInvitationToken().asString().equals(cmd.invitationToken())
                                && invitation.isAccepted())
        );
    }

    @Test
    void execute_WhenEmailAlreadyExists_ThrowEmailAlreadyExistsException() {
        RegisterUserCommand cmd = new RegisterUserCommand(
                "first",
                "last",
                "password",
                "VOLUNTEER",
                "token");

        when(invitationRepository.findInvitationByToken(any(InvitationToken.class)))
                .thenReturn(
                        Optional.of(Invitation.create(
                            InvitationID.generate(),
                            Email.from("duplicate@userId.com"),
                            Role.valueOf(cmd.role().toUpperCase()),
                            InvitationToken.from(cmd.invitationToken())
                        ))
                );
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> {
            registerUserService.execute(cmd);
        });

        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
    }
}
