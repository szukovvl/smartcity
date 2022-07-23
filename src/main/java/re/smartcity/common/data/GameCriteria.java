package re.smartcity.common.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.data.exchange.GameCriteriaData;

@Data
@NoArgsConstructor
@Table("configurations")
public class GameCriteria {

    public static final String key = "game.criteria.cfg";

    @Id
    private final String id = key;

    private GameCriteriaData data = new GameCriteriaData();
}
