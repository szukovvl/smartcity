package re.smartcity.config.sockets.process;

public enum PortStates {
    LOW,               // низкая нагрузка
    NORMAL,            // нормальная
    MEDIUM,            // средняя
    HIGH_CRITICAL,     // высокая, критическая
    OVERLOAD_BLACKOUT, // перегрузка, аварийное отключение
}
