package re.smartcity.common.resources;

public final class AppConstant {
    public final static double FORECAST_POINT_MAX_VALUE = 1.0;
    public final static double FORECAST_POINT_MIN_VALUE = 0.0;

    public final static int GAMEDAY_MAX_SECONDS = 3600 * 24 - 1;
    public final static int GAMEDAY_MAX_MINUTES = 60 * 24 - 1;

    // REST API
    public final static String API_BASE_URL = "/api/1_0";
    public final static String API_WIND_SERVICE = API_BASE_URL + "/wind";
    public final static String API_SUN_SERVICE = API_BASE_URL + "/sun";
    public final static String API_STAND_SERVICE = API_BASE_URL + "/stand";
    public final static String API_FORECAST_SERVICE = API_BASE_URL + "/forecast";
    public final static String API_COMMON_SERVICE = API_BASE_URL + "/common";
    public final static String API_ENERGY_SERVICE = API_BASE_URL + "/energy";

    // Websocket общая информация
    public final static String SOCKET_TOPICS_URL = "/wsapi/1_0/topics";
    public final static String SOCKET_COMMON_SERVICE = SOCKET_TOPICS_URL + "/common";

    //
    public final static int MAX_ILLUMINATION_VALUE = 1000;
}
