package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}
