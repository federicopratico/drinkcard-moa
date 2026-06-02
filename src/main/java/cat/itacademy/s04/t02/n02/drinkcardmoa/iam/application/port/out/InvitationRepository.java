package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.Invitation;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;

import java.util.Optional;

public interface InvitationRepository {
    Invitation save(Invitation invitation);
    Optional<Invitation> findInvitationByEmail(Email email);
}
