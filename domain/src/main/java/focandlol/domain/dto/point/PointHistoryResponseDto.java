package focandlol.domain.dto.point;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PointHistoryResponseDto {
    private int availablePoints;
    private List<PointHistoryItem> history;

    @Getter
    @AllArgsConstructor
    public static class PointHistoryItem {
        private String date;
        private String type;
        private String amount;
    }
}
