package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.InviteUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.InvitationResult;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InviteUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private InviteUserService inviteUserService;

    @Test
    void execute_WhenEmailIsNotRegisteredAndNoPriorInvitation_CreatesPendingInvitationAndPublishesEvent() {
        InviteUserCommand cmd = new InviteUserCommand("new@userid.com", "VOLUNTEER");

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(invitationRepository.findInvitationByEmail(any(Email.class))).thenReturn(Optional.empty());
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(i -> i.getArguments()[0]);

        InvitationResult result = inviteUserService.execute(cmd);

        ArgumentCaptor<Invitation> invitationCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(invitationCaptor.capture());
        Invitation saved = invitationCaptor.getValue();

        ArgumentCaptor<UserInvitedEvent> eventCaptor = ArgumentCaptor.forClass(UserInvitedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        UserInvitedEvent event = eventCaptor.getValue();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("new@userid.com", result.email()),
                () -> assertEquals(Role.VOLUNTEER.name(), result.role()),
                () -> assertEquals(InvitationStatus.PENDING.name(), result.status()),
                () -> assertNotNull(result.id()),
                () -> assertEquals("new@userid.com", saved.getEmail().asString()),
                () -> assertEquals(Role.VOLUNTEER, saved.getRole()),
                () -> assertEquals(InvitationStatus.PENDING, saved.getStatus()),
                () -> assertNotNull(saved.getInvitationToken()),
                () -> assertEquals(saved.getId().asString(), event.invitationId()),
                () -> assertEquals("new@userid.com", event.email()),
                () -> assertEquals(Role.VOLUNTEER.name(), event.role()),
                () -> assertEquals(saved.getInvitationToken().asString(), event.invitationToken()),
                () -> assertNotNull(event.occurredOn())
        );
    }

    @Test
    void execute_WhenPendingInvitationExists_RefreshesTokenAndPublishesEvent() {
        InviteUserCommand cmd = new InviteUserCommand("existing@userid.com", "VOLUNTEER");

        InvitationToken originalToken = InvitationToken.from("original-token");
        Invitation existing = Invitation.rehydrate(
                InvitationID.generate(),
                Email.from("existing@userid.com"),
                Role.VOLUNTEER,
                originalToken,
                InvitationStatus.PENDING
        );

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(invitationRepository.findInvitationByEmail(any(Email.class))).thenReturn(Optional.of(existing));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(i -> i.getArguments()[0]);

        InvitationResult result = inviteUserService.execute(cmd);

        ArgumentCaptor<Invitation> invitationCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(invitationCaptor.capture());
        Invitation saved = invitationCaptor.getValue();

        ArgumentCaptor<UserInvitedEvent> eventCaptor = ArgumentCaptor.forClass(UserInvitedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        assertAll(
                () -> assertEquals(existing.getId().asString(), result.id()),
                () -> assertEquals("existing@userid.com", result.email()),
                () -> assertEquals(InvitationStatus.PENDING.name(), result.status()),
                () -> assertEquals(existing.getId(), saved.getId()),
                () -> assertNotEquals(originalToken.asString(), saved.getInvitationToken().asString()),
                () -> assertEquals(saved.getInvitationToken().asString(), eventCaptor.getValue().invitationToken())
        );
    }

    @Test
    void execute_WhenEmailAlreadyRegistered_ThrowsEmailAlreadyExistsException() {
        InviteUserCommand cmd = new InviteUserCommand("registered@userid.com", "VOLUNTEER");

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> inviteUserService.execute(cmd));

        verify(invitationRepository, never()).findInvitationByEmail(any(Email.class));
        verify(invitationRepository, never()).save(any(Invitation.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_WhenInvitationAlreadyAccepted_ThrowsInvitationAlreadyAcceptedException() {
        InviteUserCommand cmd = new InviteUserCommand("accepted@userid.com", "VOLUNTEER");

        Invitation accepted = Invitation.rehydrate(
                InvitationID.generate(),
                Email.from("accepted@userid.com"),
                Role.VOLUNTEER,
                InvitationToken.from("any-token"),
                InvitationStatus.ACCEPTED
        );

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(invitationRepository.findInvitationByEmail(any(Email.class))).thenReturn(Optional.of(accepted));

        assertThrows(InvitationAlreadyAcceptedException.class, () -> inviteUserService.execute(cmd));

        verify(invitationRepository, never()).save(any(Invitation.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_NormalizesEmailBeforeLookup() {
        InviteUserCommand cmd = new InviteUserCommand("  Mixed.Case@UserID.com  ", "VOLUNTEER");

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(invitationRepository.findInvitationByEmail(any(Email.class))).thenReturn(Optional.empty());
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(i -> i.getArguments()[0]);

        InvitationResult result = inviteUserService.execute(cmd);

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(userRepository, times(1)).existsByEmail(emailCaptor.capture());
        verify(invitationRepository).findInvitationByEmail(any(Email.class));

        assertAll(
                () -> assertEquals("mixed.case@userid.com", emailCaptor.getValue().asString()),
                () -> assertEquals("mixed.case@userid.com", result.email())
        );
    }
}
