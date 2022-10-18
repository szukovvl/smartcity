package re.smartcity.common.resources;

public final class Messages {
    public final static String ER_0 = "Идентификация компонента энергосети не задана.";
    public final static String ER_1 = "Значение мощности не может быть отрицательным.";
    public final static String ER_2 = "Необходимо задать прогноз перед его использованием.";
    public final static String ER_3 = "Тело запроса не содержит данных.";
    public final static String ER_4 = "Значение выброса CO2 не может быть отрицательным.";
    public final static String ER_5 = "Параметр задается положительным числом.";
    public final static String ER_6 = "Время в секундах задается положительным числом.";
    public final static String ER_7 = "Данные игрока не обнаружены";
    public final static String ER_8 = "Не поддерживаемый тип объекта.";
    public final static String ER_9 = "Сетевой адрес устройства не может быть ноль.";
    public final static String ER_10 = "Порт подключения блока управления не задан.";
    public final static String ER_11 = "Нет доступных портов.";
    public final static String ER_12 = "Неверная длина принятого пакета.";
    public final static String ER_13 = "Отсутствуют необходимые данные сообщения";
    public final static String ER_14 = "Доступно только для администратора";
    public final static String ER_15 = "Текущая сцена конечная";
    public final static String ER_16 = "Объект энергосистемы не задан";
    public final static String ER_17 = "Необходимо отменить торги для текущего лота";
    public final static String ER_18 = "Нет доступных лотов аукциона";

    public final static String FER_0 = "обновляемый объект {} в хранилище не зафиксирован.";
    public final static String FER_1 = "ошибка при создании объекта конфигурации {}.";
    public final static String FER_2 = "Напряжение питание вне диапазона для элемента %02X: %fВ.";
    public final static String FER_3 = "Превышении допустимого суммарного потребляемого элементами тока для элемента %02X.";
    public final static String FER_4 = "Переполнение буфера для элемента %02X.";
    public final static String FER_5 = "Неизвестный тип пакета %02X элемента %02X.";
    public final static String FER_6 = "Объект %02X не найден";
    public final static String FER_7 = "Неверный формат адреса объекта: %s";

    public final static String SER_0 = "Главная подстанция должна подключаться к блоку управления.";
    public final static String SER_1 = "К входной линии главной подстанции может быть подключено не более одного устройства генерации.";
    public final static String SER_2 = "Главная подстанция должна быть подключена к блоку управления соответствующим выходом.";
    public final static String SER_3 = "К выходной линии главной подстанции могут быть подключены только миниподстанции и потребители 1, 2-й категорий.";
    public final static String SER_4 = "Одно и тоже устройство может быть подключено только один раз к одной линии.";
    public final static String SER_5 = "Миниподстанция должна подключаться только к выходной линии главной подстанции.";
    public final static String SER_6 = "К выходной линии министанции могут быть подключены только потребители 3-й категории.";
    public final static String SER_7 = "Миниподстанция должна быть подключена к выходу главной подстанции входной линией.";

    public final static String FSER_0 = "Неизвестный объект 0x%02X.";
    public final static String FSER_1 = "Объект не подключен 0x%02X.";
    public final static String FSER_2 = "Невосстановленное подключение для порта {} элемента {}";
    public final static String FSER_3 = "Нарушение идентичности порта {} элемента {}";
}
