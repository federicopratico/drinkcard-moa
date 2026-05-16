package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command;

public record ConsumeDrinkTicketCommand(
        String ticketId,
        String consumedByStaffId) {
}
