package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.AdminStatsResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAdminStatsServiceTest {

    @Mock
    private DrinkCardAccountRepository drinkCardAccountRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private GetAdminStatsService service;

    @Test
    void execute_ReturnsTotalsFromRepositories() {
        when(drinkCardAccountRepository.sumAvailableCredits()).thenReturn(42L);
        when(paymentRepository.sumSuccessfulPaymentsAmount()).thenReturn(new BigDecimal("123.45"));
        when(drinkCardAccountRepository.countActiveCards()).thenReturn(7L);

        AdminStatsResult result = service.execute();

        assertAll(
                () -> assertEquals(42L, result.totalAvailableCredits()),
                () -> assertEquals(0, new BigDecimal("123.45").compareTo(result.totalSuccessfulPaymentsAmount())),
                () -> assertEquals(7L, result.totalActiveCards())
        );
    }

    @Test
    void execute_WhenNoData_ReturnsZeros() {
        when(drinkCardAccountRepository.sumAvailableCredits()).thenReturn(0L);
        when(paymentRepository.sumSuccessfulPaymentsAmount()).thenReturn(BigDecimal.ZERO);
        when(drinkCardAccountRepository.countActiveCards()).thenReturn(0L);

        AdminStatsResult result = service.execute();

        assertAll(
                () -> assertEquals(0L, result.totalAvailableCredits()),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(result.totalSuccessfulPaymentsAmount())),
                () -> assertEquals(0L, result.totalActiveCards())
        );
    }
}
