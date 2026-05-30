package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invitations")
@Getter
@Setter
@NoArgsConstructor
public class InvitationJpaEntity {

    @Id
    private String id;

    @Column(unique = true, nullable = false, name = "email")
    private String email;

    @Column(nullable = false, name = "role")
    private String role;

    @Column(nullable = false, name = "status")
    private String status;

    @Column(unique = true, nullable = false, name = "invitation_token")
    private String invitationToken;

    private InvitationJpaEntity(String id, String email,
                                String role, String status, String invitationToken) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.status = status;
        this.invitationToken = invitationToken;
    }

    public static InvitationJpaEntity create(String id, String email,
                                             String role, String status, String invitationToken) {
        return new InvitationJpaEntity(id, email, role, status, invitationToken);
    }
}
