package re.smartcity.stand;

public final class SerialPackageTypes {

    public final static byte DATA_SCHEME_CONNECTION = 0x01; // данные о схеме соединения элементов стенда
    public final static byte DATA_SUPPLY_VOLTAGE = 0x02; // данные о напряжении питания элемента стенда
    public final static byte ILLUMINATION_DATA_SOLAR_BATTERY = 0x03; // данные об освещенности от элемента стенда "солнечная батарея"
    public final static byte WIND_FORCE_DATA = 0x04; // данные о силе ветра от элемента стенда "ветрогенератор"
    public final static byte MODEL_HIGHLIGHT_DATA = 0x05; // данные о уровне подсветки модели
    public final static byte DATA_CURRENT_CONSUMED = 0x06; // данные о токе, потребляемом стендом
    public final static byte INTERNAL_BUFFER_OVERFLOW = 0x07; // сообщение о переполнении внутренних буферов элемента стенда

    public final static byte SET_BRIGHTNESS_SUN_SIMULATOR = 0x10; // установка яркости имитатора солнца
    public final static byte SET_WIND_STRENGTH_WIND_SIMULATOR = 0x11; // установка силы ветра имитатора ветра
    public final static byte SET_HIGHLIGHT_LEVEL = 0x12; // установка уровня подсветки модели
    public final static byte REQUEST_SUPPLY_VOLTAGE = 0x13; // запрос данных о напряжении питания
    public final static byte REQUEST_SCHEME_CONNECTION_ELEMENTS = 0x14; // запрос данных о схеме соединения элементов стенда
    public final static byte REQUEST_ILLUMINATION_SOLAR_BATTERY = 0x15; // запрос данных об освещенности от элемента стенда "солнечная батарея"
    public final static byte REQUEST_STRENGTH_WIND_GENERATOR = 0x16; // запрос данных о силе ветра от элемента стенда "ветрогенератор"
    public final static byte REQUEST_LEVEL_MODEL_ILLUMINATION = 0x17; // запрос данных об уровне подсветки модели
    public final static byte SOLAR_CELL_CALIBRATION = 0x18; // калибровка элемента "солнечная батарея"
    public final static byte CALIBRATION_WIND_GENERATOR = 0x19; // калибровка элемента "ветрогенератор"
    public final static byte RESET_ELEMENT = 0x1A; // сброс элемента в исходное состояние (через 2 секунды после получения команды сброса)
}
