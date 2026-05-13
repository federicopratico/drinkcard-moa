package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out;

public interface EventPublisher {
    // change to DomainEvent. This is a note for the other publishers too.
    void publish(Object event);
}
