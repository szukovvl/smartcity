package re.smartcity.config.sockets;

public enum GameEventTypes {
    STATUS, // запрос текущего состояния или информирование
    GAMECONTROL, // запрос на захват контроллера игры
    GAMER_ENTER, // запрос на вход игрока
    STARTGAMESCENES, // запрос инициирования игрового сценария
    CANCELGAMESCENES, // запрос прерывния игрового сценария
    GAME_SCENE_IDENTIFY, // смена сцены: игрок, определи себя
    SCENESDATA, // запрос данных сцен игрока
    ERROR // инициируется только сервисом для конкретного клиента
}
