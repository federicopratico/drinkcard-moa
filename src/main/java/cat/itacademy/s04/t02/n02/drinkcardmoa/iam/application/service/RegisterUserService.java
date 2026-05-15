package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.RegisterUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.RegisterUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.RegisterUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.EventPublisher;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordEncoder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.EmailAlreadyExistsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.UserRegisteredEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;

    public RegisterUserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RegisterUserResult execute(RegisterUserCommand cmd) {

        validateEmailUniqueness(cmd.email());

        User user = mapToDomain(cmd);
        User savedUser = userRepository.save(user);

        eventPublisher.publish(new UserRegisteredEvent(
                savedUser.getId().asString(),
                savedUser.getEmail().asString(),
                savedUser.getRole().name(),
                Instant.now()
        ));

        return mapToResult(savedUser);
    }

    private void validateEmailUniqueness(String email) {
        if(userRepository.existsByEmail(Email.from(email)))
            throw new EmailAlreadyExistsException("The provided email already exists");
    }

    private User mapToDomain(RegisterUserCommand cmd) {
        return User.create(
                VolunteerID.generate(),
                FullName.from(cmd.firstName(), cmd.lastName()),
                Email.from(cmd.email()),
                HashedPassword.from(passwordEncoder.encode(cmd.password())),
                Role.valueOf(cmd.role().toUpperCase())
        );
    }

    private RegisterUserResult mapToResult(User user) {
        return RegisterUserResult.from(user);
    }
}
