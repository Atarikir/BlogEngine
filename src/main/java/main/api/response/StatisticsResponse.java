package main.api.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticsResponse {
    private Long postsCount;
    private Long likesCount;
    private Long dislikesCount;
    private Long viewsCount;
    private Long firstPublication;
}
