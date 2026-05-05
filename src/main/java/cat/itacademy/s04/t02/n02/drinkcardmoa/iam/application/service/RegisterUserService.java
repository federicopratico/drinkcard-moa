package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.RegisterUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.RegisterUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.RegisterUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordEncoder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.EmailAlreadyExistsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterUserResult execute(RegisterUserCommand cmd) {

        validateEmailUniqueness(cmd.email());

        User user = mapToDomain(cmd);
        User savedUser = userRepository.save(user);

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
