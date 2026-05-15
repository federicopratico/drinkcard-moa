package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent event);
}
