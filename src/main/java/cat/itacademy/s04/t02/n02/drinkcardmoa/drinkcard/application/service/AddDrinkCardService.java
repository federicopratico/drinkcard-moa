package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.AddDrinkCardCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.AddDrinkCardResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.AddDrinkCardUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.PurchaseLimitExceededException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.RefillDisabledException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.Payment;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.UserNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.TurnRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.exception.NoTurnTodayException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AddDrinkCardService implements AddDrinkCardUseCase {

    private final DrinkCardAccountRepository drinkCardAccountRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final TurnRepository turnRepository;

    private static final ZoneId FESTIVAL_ZONE = ZoneId.of("Europe/Rome");

    @Override
    @Transactional
    public AddDrinkCardResult execute(AddDrinkCardCommand cmd) {
        Instant now = Instant.now();
        VolunteerID volunteerIdObj = VolunteerID.from(cmd.volunteerId());

        DrinkCardAccount account = drinkCardAccountRepository.findByVolunteerId(volunteerIdObj)
                .orElseThrow(() -> new DrinkCardAccountNotFoundException("No drink card account for the provided Id: " + cmd.volunteerId()));

        if(!account.canRefill()) {
            throw new RefillDisabledException("Account is disabled for refilling.");
        }

        if(!account.canPurchaseCard(now)) {
            throw new PurchaseLimitExceededException("Purchase limit exceeded for the provided Id: " + cmd.volunteerId());
        }

        User user = userRepository.findById(volunteerIdObj)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + cmd.volunteerId()));

        LocalDate today = now.atZone(FESTIVAL_ZONE).toLocalDate();

        if (!turnRepository.existsByEmailAndDate(user.getEmail(), today)) {
            throw new NoTurnTodayException(
                    "Volunteer " + user.getEmail().asString() + " has no turn scheduled for " + today + " and cannot receive a drink card."
            );
        }


        Card card = Card.newCard();
        String idempotencyKey = cmd.volunteerId() + now.toString();

        Payment payment = Payment.pending(
                volunteerIdObj,
                card.getPrice(),
                idempotencyKey,
                now,
                now.plusSeconds(120)
        );

        payment.markAsSuccess();

        paymentRepository.save(payment);
        account.purchaseCard(card, now);
        drinkCardAccountRepository.save(account);

        return new AddDrinkCardResult(
                account.getVolunteerId().asString(),
                account.getCredits(),
                card.getPrice()
        );
    };
}
