package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.security.JwtAuthenticationFilter;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.ConsumeDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreateDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetDrinkTicketStatusQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.ConsumeDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateDrinkTicketResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkTicketStatusResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ConsumeDrinkTicketUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.CreateDrinkTicketUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.GetDrinkTicketStatusUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper.DrinkTicketControllerMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DrinkTicketController.class)
@Import(DrinkTicketControllerMapper.class)
@AutoConfigureMockMvc(addFilters = false)
class DrinkTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateDrinkTicketUseCase createDrinkTicketUseCase;

    @MockitoBean
    private ConsumeDrinkTicketUseCase consumeDrinkTicketUseCase;

    @MockitoBean
    private GetDrinkTicketStatusUseCase getDrinkTicketStatusUseCase;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createDrinkTicket_ReturnsCreatedDrinkTicketResponse() throws Exception {
        String volunteerId = "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1";
        Instant expiresAt = Instant.parse("2026-05-16T21:30:00Z");

        CreateDrinkTicketResult result = new CreateDrinkTicketResult(
                "7aab22f8-60d3-4700-8ba6-b35e67dfacb6",
                "BEER",
                "PENDING",
                expiresAt
        );

        when(createDrinkTicketUseCase.execute(new CreateDrinkTicketCommand(volunteerId, "BEER")))
                .thenReturn(result);

        String requestBody = objectMapper.writeValueAsString(new CreateDrinkTicketJson(
                volunteerId,
                "BEER"
        ));

        mockMvc.perform(post("/api/v1/drink-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketId").value("7aab22f8-60d3-4700-8ba6-b35e67dfacb6"))
                .andExpect(jsonPath("$.drinkType").value("BEER"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.expiresAt").value("2026-05-16T21:30:00Z"));

        ArgumentCaptor<CreateDrinkTicketCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateDrinkTicketCommand.class);

        verify(createDrinkTicketUseCase).execute(commandCaptor.capture());

        CreateDrinkTicketCommand command = commandCaptor.getValue();

        assertEquals(volunteerId, command.volunteerId());
        assertEquals("BEER", command.drinkType());
    }

    @Test
    void consumeDrinkTicket_ReturnsOkConsumeDrinkTicketResponse() throws Exception {
        String ticketId = "7aab22f8-60d3-4700-8ba6-b35e67dfacb6";
        String consumedByStaffId = "8799df50-d517-4693-9e46-51b537c305a2";

        ConsumeDrinkTicketResult result = new ConsumeDrinkTicketResult(
                ticketId,
                "CONSUMED",
                "BEER",
                4
        );

        when(consumeDrinkTicketUseCase.execute(new ConsumeDrinkTicketCommand(ticketId, consumedByStaffId)))
                .thenReturn(result);

        String requestBody = objectMapper.writeValueAsString(new ConsumeDrinkTicketJson(consumedByStaffId));

        mockMvc.perform(post("/api/v1/drink-tickets/{ticketId}/consume", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(ticketId))
                .andExpect(jsonPath("$.status").value("CONSUMED"))
                .andExpect(jsonPath("$.drinkType").value("BEER"))
                .andExpect(jsonPath("$.remainingCredits").value(4));

        ArgumentCaptor<ConsumeDrinkTicketCommand> commandCaptor =
                ArgumentCaptor.forClass(ConsumeDrinkTicketCommand.class);

        verify(consumeDrinkTicketUseCase).execute(commandCaptor.capture());

        ConsumeDrinkTicketCommand command = commandCaptor.getValue();

        assertEquals(ticketId, command.ticketId());
        assertEquals(consumedByStaffId, command.consumedByStaffId());
    }

    @Test
    void getTicketStatus_ReturnsOkDrinkTicketStatusResponse() throws Exception {
        String ticketId = "7aab22f8-60d3-4700-8ba6-b35e67dfacb6";
        Instant expiresAt = Instant.parse("2026-05-16T21:30:00Z");

        DrinkTicketStatusResult result = new DrinkTicketStatusResult(
                ticketId,
                "PENDING",
                "BEER",
                expiresAt,
                null
        );

        when(getDrinkTicketStatusUseCase.execute(new GetDrinkTicketStatusQuery(ticketId)))
                .thenReturn(result);

        mockMvc.perform(get("/api/v1/drink-tickets/{ticketId}/status", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(ticketId))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.drinkType").value("BEER"))
                .andExpect(jsonPath("$.expiresAt").value("2026-05-16T21:30:00Z"))
                .andExpect(jsonPath("$.consumedAt").doesNotExist());

        ArgumentCaptor<GetDrinkTicketStatusQuery> queryCaptor =
                ArgumentCaptor.forClass(GetDrinkTicketStatusQuery.class);

        verify(getDrinkTicketStatusUseCase).execute(queryCaptor.capture());

        GetDrinkTicketStatusQuery query = queryCaptor.getValue();

        assertEquals(ticketId, query.ticketId());
    }

    private record CreateDrinkTicketJson(
            String volunteerId,
            String drinkType
    ) {
    }

    private record ConsumeDrinkTicketJson(
            String consumedByStaffId
    ) {
    }
}
