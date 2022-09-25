package re.smartcity.modeling;

public enum GameStatuses {
    NONE, // игровых сценариев нет
    GAMERS_IDENTIFY, // сцена 1 - игроки обозначают себя
    GAMERS_CHOICE_OES, // сцена 2 - выбор объектов
    GAMERS_AUCTION_PREPARE, // сцена 3 - аукцион - подготовка
    GAMERS_AUCTION_SALE, // сцена 4 - аукцион торговля
    GAMERS_SCHEME // сцена 5 - сборка схемы
}
