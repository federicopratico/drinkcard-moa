package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.config;

import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.usecase.ConfirmPaymentUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.usecase.CreatePaymentCheckoutUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.usecase.CreateVolunteerUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.EventPublisher;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.PaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.VolunteerRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.payment.PaymentGateway;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.service.ConfirmPaymentService;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.service.CreatePaymentCheckoutService;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.service.CreateVolunteerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VolunteerBeanConfiguration {

    @Bean
    CreateVolunteerUseCase createVolunteerUseCase(VolunteerRepository volunteerRepository) {
        return new CreateVolunteerService(volunteerRepository);
    }

    @Bean
    CreatePaymentCheckoutUseCase createPaymentCheckoutUseCase(VolunteerRepository repository, PaymentGateway paymentGateway, PaymentRepository paymentRepository) {
        return new CreatePaymentCheckoutService(
                paymentRepository,
                paymentGateway,
                repository
        );
    }

    @Bean
    ConfirmPaymentUseCase confirmPaymentUseCase(
            PaymentGateway paymentGateway, PaymentRepository paymentRepository, EventPublisher eventPublisher, VolunteerRepository volunteerRepository) {
        return new ConfirmPaymentService(paymentGateway, paymentRepository, eventPublisher, volunteerRepository);
    }
}
