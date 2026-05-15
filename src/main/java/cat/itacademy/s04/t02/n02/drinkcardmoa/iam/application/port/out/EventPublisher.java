package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent event);
}
