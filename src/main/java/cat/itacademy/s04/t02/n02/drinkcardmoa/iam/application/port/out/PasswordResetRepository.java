package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.PasswordReset;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;

import java.util.Optional;

public interface PasswordResetRepository {
    PasswordReset save(PasswordReset passwordReset);
    Optional<PasswordReset> findByPasswordResetToken(HashedToken token);
}