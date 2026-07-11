package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.AddDrinkCardCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.AddDrinkCardResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.PurchaseLimitExceededException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.RefillDisabledException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkCardAccountStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.UserNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.UserStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.TurnRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.exception.NoTurnTodayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddDrinkCardServiceTest {

    @Mock
    private DrinkCardAccountRepository drinkCardAccountRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TurnRepository turnRepository;

    @InjectMocks
    private AddDrinkCardService service;

    @Test
    void execute_WhenAccountIsActiveAndCanPurchase_AddsCardCreatesSuccessfulPaymentAndReturnsResult() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount account = activeAccount(volunteerId, 2, Instant.now().minus(2, ChronoUnit.DAYS));

        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(account));
        stubVolunteerHasTurnToday(volunteerId);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(drinkCardAccountRepository.save(any(DrinkCardAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddDrinkCardResult result = service.execute(new AddDrinkCardCommand(volunteerId.asString()));

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        ArgumentCaptor<DrinkCardAccount> accountCaptor = ArgumentCaptor.forClass(DrinkCardAccount.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        verify(drinkCardAccountRepository).save(accountCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();
        DrinkCardAccount savedAccount = accountCaptor.getValue();

        assertAll(
                () -> assertEquals(volunteerId.asString(), result.volunteerId()),
                () -> assertEquals(7, result.credits()),
                () -> assertEquals(0, Card.newCard().getPrice().compareTo(result.amount())),
                () -> assertEquals(volunteerId, savedPayment.getVolunteerId()),
                () -> assertEquals(PaymentStatus.SUCCESS, savedPayment.getStatus()),
                () -> assertEquals(0, Card.newCard().getPrice().compareTo(savedPayment.getAmount())),
                () -> assertNotNull(savedPayment.getPaidAt()),
                () -> assertNotNull(savedPayment.getExpiresAt()),
                () -> assertFalse("manual_payment".equals(savedPayment.getIdempotencyKey())),
                () -> assertTrue(savedPayment.getIdempotencyKey().startsWith(volunteerId.asString())),
                () -> assertEquals(7, savedAccount.getCredits()),
                () -> assertNotNull(savedAccount.getLastPurchaseTimestamp())
        );
    }

    @Test
    void execute_WhenAccountDoesNotExist_ThrowsNotFoundAndDoesNotSave() {
        VolunteerID volunteerId = VolunteerID.generate();

        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.empty());

        assertThrows(
                DrinkCardAccountNotFoundException.class,
                () -> service.execute(new AddDrinkCardCommand(volunteerId.asString()))
        );

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(drinkCardAccountRepository, never()).save(any(DrinkCardAccount.class));
    }

    @Test
    void execute_WhenRefillIsDisabled_ThrowsRefillDisabledAndDoesNotSave() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount account = DrinkCardAccount.rehydrate(
                1L,
                volunteerId,
                2,
                null,
                Instant.now().minus(2, ChronoUnit.DAYS),
                DrinkCardAccountStatus.REFILL_DISABLED
        );

        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(account));

        assertThrows(
                RefillDisabledException.class,
                () -> service.execute(new AddDrinkCardCommand(volunteerId.asString()))
        );

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(drinkCardAccountRepository, never()).save(any(DrinkCardAccount.class));
    }

    @Test
    void execute_WhenAccountAlreadyPurchasedToday_ThrowsPurchaseLimitExceededAndDoesNotSave() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount account = activeAccount(volunteerId, 2, Instant.now());

        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(account));

        assertThrows(
                PurchaseLimitExceededException.class,
                () -> service.execute(new AddDrinkCardCommand(volunteerId.asString()))
        );

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(drinkCardAccountRepository, never()).save(any(DrinkCardAccount.class));
    }

    @Test
    void execute_WhenVolunteerIdIsInvalid_ThrowsIllegalArgumentExceptionAndDoesNotUseRepositories() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(new AddDrinkCardCommand("invalid-uuid"))
        );

        verifyNoInteractions(drinkCardAccountRepository, paymentRepository);
    }

    @Test
    void execute_WhenVolunteerHasNoTurnToday_ThrowsNoTurnTodayExceptionAndDoesNotSave() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount account = activeAccount(volunteerId, 2, Instant.now().minus(2, ChronoUnit.DAYS));

        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(account));
        when(userRepository.findById(volunteerId)).thenReturn(Optional.of(user(volunteerId)));
        when(turnRepository.existsByEmailAndDate(any(Email.class), any())).thenReturn(false);

        assertThrows(
                NoTurnTodayException.class,
                () -> service.execute(new AddDrinkCardCommand(volunteerId.asString()))
        );

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(drinkCardAccountRepository, never()).save(any(DrinkCardAccount.class));
    }

    @Test
    void execute_WhenUserDoesNotExist_ThrowsUserNotFoundExceptionAndDoesNotSave() {
        VolunteerID volunteerId = VolunteerID.generate();
        DrinkCardAccount account = activeAccount(volunteerId, 2, Instant.now().minus(2, ChronoUnit.DAYS));

        when(drinkCardAccountRepository.findByVolunteerId(volunteerId)).thenReturn(Optional.of(account));
        when(userRepository.findById(volunteerId)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.execute(new AddDrinkCardCommand(volunteerId.asString()))
        );

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(drinkCardAccountRepository, never()).save(any(DrinkCardAccount.class));
    }

    private DrinkCardAccount activeAccount(VolunteerID volunteerId, int credits, Instant lastPurchaseTimestamp) {
        return DrinkCardAccount.rehydrate(
                1L,
                volunteerId,
                credits,
                lastPurchaseTimestamp,
                Instant.now().minus(5, ChronoUnit.DAYS),
                DrinkCardAccountStatus.ACTIVE
        );
    }

    private void stubVolunteerHasTurnToday(VolunteerID volunteerId) {
        User user = user(volunteerId);
        when(userRepository.findById(volunteerId)).thenReturn(Optional.of(user));
        when(turnRepository.existsByEmailAndDate(any(Email.class), any())).thenReturn(true);
    }

    private User user(VolunteerID volunteerId) {
        return User.rehydrate(
                volunteerId,
                FullName.from("Jane", "Doe"),
                Email.from("jane@example.com"),
                HashedPassword.from("hashed_password"),
                Role.VOLUNTEER,
                UserStatus.ACTIVE
        );
    }
}
