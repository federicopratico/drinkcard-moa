package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SumUpWebhookRequest(
        @JsonProperty("event_type") String eventType,
        String id
) {
}
