package re.smartcity.config.sockets.model;

import re.smartcity.modeling.GameStatuses;
import re.smartcity.modeling.data.AuctionSettings;

/**
 * @param settings настройки аукциона
 * @param lots     доступные лоты
 * @param unsolds  отказники
 * @param current  текущий лот на продаже
 * @param gamer    лоты по игрокам
 * @param status   текущий игровой сценарий
 */
public record ResponseAuctionData(
        AuctionSettings settings,
        AuctionGamerData[] gamer,
        int[] lots,
        int[] unsolds,
        int current,
        GameStatuses status) { }
