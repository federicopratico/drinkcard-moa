package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;

public interface TokenService {

    String generateToken(User user);

    boolean validateToken(String token);

    VolunteerID extractVolunteerID(String token);

    String extractEmail(String token);

    String extractRole(String token);
}
