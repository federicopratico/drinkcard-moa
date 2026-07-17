package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.AdminStatsResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.GetAdminStatsUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.VolunteerDirectory;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.TopVolunteer;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.TopVolunteerResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.VolunteerInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.PaymentSummaryResult.UNKNOWN;

@Service
@RequiredArgsConstructor
public class GetAdminStatsService implements GetAdminStatsUseCase {

    private final DrinkCardAccountRepository drinkCardAccountRepository;
    private final PaymentRepository paymentRepository;
    private final VolunteerDirectory volunteerDirectory;
    private final DrinkTicketRepository drinkTicketRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResult execute() {
        var topVolunteers = drinkTicketRepository.getTopConsumer(5);
        var volunteers = volunteerDirectory.findAllByIds(topVolunteers.stream().map(TopVolunteer::getVolunteerId).toList());

        return new AdminStatsResult(
                drinkCardAccountRepository.sumAvailableCredits(),
                paymentRepository.sumSuccessfulPaymentsAmount(),
                paymentRepository.countPayments(PaymentStatus.SUCCESS),
                drinkCardAccountRepository.countActiveCards(),
                drinkTicketRepository.getConsumptionStats(),
                topVolunteers.stream().map(topVolunteer -> {
                    var volunteer = volunteers.get(topVolunteer.getVolunteerId());
                    return new TopVolunteerResponse(
                            new VolunteerInfo(
                                    volunteer != null ? volunteer.id().asString() : UNKNOWN,
                                    volunteer != null ? volunteer.firstName() : UNKNOWN,
                                    volunteer != null ? volunteer.lastName() : UNKNOWN,
                                    volunteer != null ? volunteer.email() : UNKNOWN
                            ),
                            topVolunteer.getDrinkTicketsCount()
                    );
                }).toList()

        );
    }
}
