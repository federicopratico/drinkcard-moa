package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;

public class User {
    private VolunteerID id;
    private FullName fullName;
    private Email email;
    private HashedPassword hashedPassword;
    private Role role;

    private User(VolunteerID id, FullName fullName, Email email, HashedPassword hashedPassword, Role role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.role = role;
    }

    public static User create(VolunteerID id, FullName fullName, Email email, HashedPassword hashedPassword, Role role) {
        return new User(id, fullName, email, hashedPassword, role);
    }

    public static User rehydrate(VolunteerID id, FullName fullName, Email email, HashedPassword hashedPassword, Role role) {
        return new User(id, fullName, email, hashedPassword, role);
    }

    public VolunteerID getId() {
        return id;
    }
    public FullName getFullName() {
        return fullName;
    }
    public Email getEmail() {
        return email;
    }
    public HashedPassword getHashedPassword() {
        return hashedPassword;
    }
    public Role getRole() {
        return role;
    }
}
