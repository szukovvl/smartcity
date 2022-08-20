package re.smartcity.config.sockets;

public enum GameEventTypes {
    STATUS, // запрос текущего состояния или информирование
    GAMECONTROL, // запрос на захват контроллера игры
    ERROR // инициируется только сервисом для конкретного клиента
}
