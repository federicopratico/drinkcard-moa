package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;

public interface RefreshTokenGenerator {

    GeneratedRefreshToken generate();
    HashedToken hash(String rawToken);

    record GeneratedRefreshToken(String rawToken, HashedToken hashedToken) {}
}
