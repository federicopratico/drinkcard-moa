package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.BootstrapAdminCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.BootstrapAdminUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordEncoder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import lombok.RequiredArgsConstructor;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BootstrapAdminService implements BootstrapAdminUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void execute(BootstrapAdminCommand cmd) {
        Email email = Email.from(cmd.email());

        if (userRepository.existsByEmail(email)) {
            return;
        }

        User admin = User.create(
                VolunteerID.generate(),
                FullName.from(cmd.firstName(), cmd.lastName()),
                email,
                HashedPassword.from(passwordEncoder.encode(cmd.password())),
                Role.ADMIN
        );

        userRepository.save(admin);
    }
}
