package re.smartcity.common.utils;

import re.smartcity.common.data.ForecastPoint;

import java.util.Arrays;

import static re.smartcity.common.resources.AppConstant.*;

public final class Helpers {

    public static byte[] byteArrayCopy(Byte[] src) {
        return byteArrayCopy(src, 0, src.length);
    }

    public static byte[] byteArrayCopy(Byte[] src, int from_index, int length) {
        byte[] res = new byte[length];
        for (int i = 0; i < length; i++) {
            res[i] = src[i + from_index];
        }
        return res;
    }

    public static ForecastPoint[] checkForecastBounds(ForecastPoint[] items) {
        if (items == null || items.length == 0) {
            return items;
        }
        var s = Arrays.stream(items)
                .sorted()
                .distinct();
        items = s.toArray(ForecastPoint[]::new);

        Arrays.stream(items).forEachOrdered(e ->
        {
            double val = e.getValue();
            if (val < FORECAST_POINT_MIN_VALUE) e.setValue(FORECAST_POINT_MIN_VALUE);
            if (val > FORECAST_POINT_MAX_VALUE) e.setValue(FORECAST_POINT_MAX_VALUE);
        });

        return items;
    }

    public static float normalizeAsPercentage(float val, float maxVal) {
        if (val < 0.0f) return 0.0f;
        if (val > maxVal) return 100.0f;
        return (val / maxVal) * 100.0f;
    }

    public static float noramlizeValue(float val, float maxVal) {
        if (val < 0.0f) return 0.0f;
        return Math.min(val, maxVal);
    }
}
