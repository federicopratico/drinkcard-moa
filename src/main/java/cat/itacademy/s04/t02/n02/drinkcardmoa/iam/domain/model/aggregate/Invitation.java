package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.InvitationStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.InvitationToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.InvitationID;
import lombok.Getter;

@Getter
public class Invitation {
    private InvitationID id;
    private Email email;
    private Role role;
    private InvitationToken invitationToken;
    private InvitationStatus status;

    private Invitation(InvitationID id, Email email, Role role, InvitationToken invitationToken, InvitationStatus status) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.invitationToken = invitationToken;
        this.status = status;
    }


    public boolean isAccepted() {
        return status == InvitationStatus.ACCEPTED;
    }

    public static Invitation create(InvitationID id, Email email, Role role, InvitationToken invitationToken) {
        return new Invitation(id, email, role, invitationToken, InvitationStatus.PENDING);
    }

    public static Invitation rehydrate(InvitationID id, Email email, Role role, InvitationToken invitationToken, InvitationStatus status) {
        return new Invitation(id, email, role, invitationToken, status);
    }

    public void refresh(InvitationToken invitationToken) {
        this.invitationToken = invitationToken;
    }
}
