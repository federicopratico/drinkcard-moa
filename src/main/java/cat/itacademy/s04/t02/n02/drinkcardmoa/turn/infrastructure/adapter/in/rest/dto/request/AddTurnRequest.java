package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.in.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AddTurnRequest(
        @NotNull @NotBlank @Email
        String email,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate date
) {
}
