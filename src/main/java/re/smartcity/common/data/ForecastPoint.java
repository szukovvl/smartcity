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
public class ForecastPoint implements Comparable<ForecastPoint> {

    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @NonNull
    private LocalTime point;

    @NonNull
    private Double value;

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
}
