package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrinkConsumption {
    private final String drinkType;
    private final Long drinkTicketsCount;

    public DrinkConsumption(String drinkType, Long drinkTicketsCount) {
        this.drinkType = drinkType;
        this.drinkTicketsCount = drinkTicketsCount;
    }
}
