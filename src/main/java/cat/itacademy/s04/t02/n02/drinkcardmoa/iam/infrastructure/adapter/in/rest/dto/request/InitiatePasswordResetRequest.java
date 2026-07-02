package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.Email;

public record InitiatePasswordResetRequest(
        @Email
        String email
) {
}
