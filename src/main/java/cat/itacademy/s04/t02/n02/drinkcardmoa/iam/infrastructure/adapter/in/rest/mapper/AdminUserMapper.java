package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.UserSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class AdminUserMapper {

    public UserSummaryResponse toResponse(UserSummaryResult result) {
        return new UserSummaryResponse(
                result.userId(),
                result.fullName(),
                result.email(),
                result.role(),
                result.status()
        );
    }
}
