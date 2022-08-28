package re.smartcity.config.sockets;

public enum GameEventTypes {
    STATUS, // запрос текущего состояния или информирование
    GAMECONTROL, // запрос на захват контроллера игры
    STARTGAMESCENES, // запрос инициирования игрового сценария
    GAMERSDATA, // запрос данных игрока
    GAME_SCENE_IDENTIFY, // смена сцены: игрок, определи себя
    ERROR // инициируется только сервисом для конкретного клиента
}
