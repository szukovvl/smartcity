package re.smartcity.config.sockets.process;

public enum GeneratorStatuses {
    NONE, // неопределенно
    ACTIVE, // объект активен
    RESERVED, // объект в резерве
    OVERLOAD, // объект перегружен
    BLACKOUT // отключен в аварийном состоянии
}
