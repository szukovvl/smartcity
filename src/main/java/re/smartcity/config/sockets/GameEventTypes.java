package re.smartcity.config.sockets;

public enum GameEventTypes {
    STATUS, // запрос текущего состояния или информирование
    GAMECONTROL, // запрос на захват контроллера игры
    GAMER_ENTER, // запрос на вход игрока
    STARTGAMESCENES, // запрос инициирования игрового сценария
    CANCELGAMESCENES, // запрос прерывания игрового сценария
    GAME_SCENE_IDENTIFY, // смена сцены: игрок, определи себя
    SCENE_COMPLETTE_IDENTIFY, // игрок завершил сцену приглашения
    GAME_SCENE_NEXT, // перейти к следующей сцене
    GAME_SCENE_PREV, // перейти к предыдущей сцене
    GAME_SCENE_CHOICE_OES, // выбор объектов энергосистемы
    GAMER_CAPTURE_OES, // запрос игрока на захват объекта
    GAMER_REFUSE_OES, // отказаться от выбранного объекта,
    GAME_SCENE_AUCTION_PREPARE, // аукцион - подготовка
    GAME_SCENE_AUCTION_SETTINGS, // настройка параметров (только от клиента)
    GAME_SCENE_AUCTION, // данные аукциона
    GAME_SCENE_AUCTION_SALE, // аукцион - торговля
    GAME_SCENE_AUCTION_PUT_LOT, // выставить лот на торги
    GAME_SCENE_AUCTION_CANCEL_LOT, // отменить текущий лот
    GAME_SCENE_AUCTION_BAY_LOT, // игрок забирает лот
    GAME_SCENE_AUCTION_TIME_LOT, // текущее время лота (остаток)
    GAME_SCENE_SCHEME, // схема
    GAME_SCHEMA_DATA, // схема
    GAME_PROCESS_START, // запуск игры
    GAME_PROCESS_ITERATION, // временной срез
    SCENESDATA, // запрос данных сцен игрока
    ERROR // инициируется только сервисом для конкретного клиента
}
