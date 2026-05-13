package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.config;

import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.usecase.CreateVolunteerUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.VolunteerRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.service.CreateVolunteerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VolunteerBeanConfiguration {

    @Bean
    CreateVolunteerUseCase createVolunteerUseCase(VolunteerRepository volunteerRepository) {
        return new CreateVolunteerService(volunteerRepository);
    }
}
