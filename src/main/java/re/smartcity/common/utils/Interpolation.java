package re.smartcity.common.utils;

import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import re.smartcity.common.data.ForecastPoint;
import re.smartcity.common.data.exchange.ForecastInterpolation;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static re.smartcity.common.resources.AppConstant.*;
import static re.smartcity.common.resources.AppConstant.GAMEDAY_MAX_MINUTES;

public final class Interpolation {

    public static ForecastInterpolation interpolate(ForecastPoint[] points) {
        return interpolate(points, 1.0);
    }

    public static ForecastInterpolation interpolate(ForecastPoint[] points, double scale) {
        ArrayList<ForecastPoint> wrkdata = new ArrayList<>(Arrays.stream(points).toList());
        if (points[0].getPoint().toSecondOfDay() != 0)
        {
            wrkdata.add(0, new ForecastPoint());
        }
        if (points[points.length - 1].getPoint().toSecondOfDay() < (GAMEDAY_MAX_MINUTES * 60)) {
            ForecastPoint lastpt = new ForecastPoint();
            lastpt.setPoint(LocalTime.ofSecondOfDay(GAMEDAY_MAX_MINUTES * 60L));
            lastpt.setValue(points[points.length - 1].getValue());
            wrkdata.add(lastpt);
        }

        ForecastInterpolation interpolation = new ForecastInterpolation();
        if (wrkdata.size() < 5) {
            interpolation.setLinear(true);
            wrkdata.forEach(pt -> pt.setValue(pt.getValue() * scale));
            interpolation.setItems(wrkdata.toArray(ForecastPoint[]::new));
        } else {
            double[] xx = wrkdata.stream()
                    .mapToDouble(b -> ForecastPoint.TimeToDouble(b.getPoint())).toArray();
            double[] yy = wrkdata.stream()
                    .mapToDouble(ForecastPoint::getValue).toArray();
            PolynomialSplineFunction splineFunction = (new AkimaSplineInterpolator()).interpolate(xx, yy);
            ForecastPoint[] pts = Stream.generate(ForecastPoint::new).limit(720).toArray(ForecastPoint[]::new);
            long[] tm = { 0 };
            Arrays.stream(pts).forEachOrdered(pt -> {
                pt.setPoint(LocalTime.ofSecondOfDay(tm[0]));
                tm[0] += 120;
                double val = splineFunction.value(ForecastPoint.TimeToDouble(pt.getPoint()));
                if (val < FORECAST_POINT_MIN_VALUE) {
                    val = FORECAST_POINT_MIN_VALUE;
                } else if (val > FORECAST_POINT_MAX_VALUE) {
                    val = FORECAST_POINT_MAX_VALUE;
                }
                pt.setValue(val * scale);
            });
            interpolation.setItems(pts);
        }

        return interpolation;
    }

    public static double[] interpolate(ForecastPoint[] points, double scale, long count_pt, long step) {
        ArrayList<ForecastPoint> wrkdata = new ArrayList<>(Arrays.stream(points).toList());
        if (points[0].getPoint().toSecondOfDay() != 0)
        {
            wrkdata.add(0, new ForecastPoint());
        }
        if (points[points.length - 1].getPoint().toSecondOfDay() < LocalTime.MAX.toSecondOfDay()) {
            ForecastPoint lastpt = new ForecastPoint();
            lastpt.setPoint(LocalTime.ofSecondOfDay(LocalTime.MAX.toSecondOfDay()));
            lastpt.setValue(points[points.length - 1].getValue());
            wrkdata.add(lastpt);
        }

        double[] xx = wrkdata.stream()
                .mapToDouble(b -> b.getPoint().toSecondOfDay()).toArray();
        double[] yy = wrkdata.stream()
                .mapToDouble(ForecastPoint::getValue).toArray();
        PolynomialSplineFunction splineFunction;
        if (wrkdata.size() < 5) {
            splineFunction = (new LinearInterpolator()).interpolate(xx, yy);
        } else {
            splineFunction = (new AkimaSplineInterpolator()).interpolate(xx, yy);
        }
        final double[] current_x = {0.0};
        return DoubleStream.generate(() -> {
            double v = splineFunction.value(current_x[0]);
            if (v < FORECAST_POINT_MIN_VALUE) {
                v = FORECAST_POINT_MIN_VALUE;
            } else if (v > FORECAST_POINT_MAX_VALUE) {
                v = FORECAST_POINT_MAX_VALUE;
            }
            v *= scale;
            current_x[0] += step;
            return v;
        }).limit(count_pt).toArray();
    }
}
