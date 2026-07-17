package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.AdminStatsResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.GetAdminStatsUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAdminStatsService implements GetAdminStatsUseCase {

    private final DrinkCardAccountRepository drinkCardAccountRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResult execute() {
        return new AdminStatsResult(
                drinkCardAccountRepository.sumAvailableCredits(),
                paymentRepository.sumSuccessfulPaymentsAmount(),
                drinkCardAccountRepository.countActiveCards()
        );
    }
}
