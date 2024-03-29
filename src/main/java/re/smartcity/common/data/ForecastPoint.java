package re.smartcity.common.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForecastPoint implements Comparable<ForecastPoint> {

    public static final int SECONDS_IN_HOUR = 3600;

    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @NonNull
    private LocalTime point = LocalTime.of(0, 0, 0);

    @NonNull
    private Double value = 0.0;

    @Override
    public boolean equals(Object obj) {
        // !!! ВНИАМЕНИЕ: проверяю только временные точки
        if (obj == this) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof ForecastPoint)) { return false; }
        return this.point.equals(((ForecastPoint) obj).point);
    }

    @Override
    public int hashCode() {
        // !!! ВНИМАНИЕ: строится только по временным точкам
        return this.point.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s: %s", this.point, this.value);
    }

    @Override
    public int compareTo(ForecastPoint obj)
    // !!! ВНИМАНИЕ: строится только по временным точкам
    {
        return this.point.compareTo(obj.point);
    }

    public static double TimeToDouble(LocalTime time) {
        return (double) time.toSecondOfDay() / (double) SECONDS_IN_HOUR;
    }

    public static LocalTime DoubleToTime(double value) {
        return LocalTime.ofSecondOfDay(Math.round(value * (double) SECONDS_IN_HOUR));
    }
}
