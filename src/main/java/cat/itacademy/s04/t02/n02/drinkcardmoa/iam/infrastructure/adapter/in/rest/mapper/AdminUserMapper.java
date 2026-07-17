package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.ListUsersQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.UserSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminUserMapper {

    public PageResponse<UserSummaryResponse> toResponse(PageResult<UserSummaryResult> result) {
        List<UserSummaryResponse> content = result.content()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }

    public UserSummaryResponse toResponse(UserSummaryResult result) {
        return new UserSummaryResponse(
                result.userId(),
                result.fullName(),
                result.email(),
                result.role(),
                result.status(),
                result.drinkCard()
        );
    }

    public ListUsersQuery toQuery(String role, String status, String email, int page, int size, String sort) {
        return new ListUsersQuery(role, status, email, page, size, sort);
    }
}
