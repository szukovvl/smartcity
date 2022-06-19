package re.smartcity.energynet.component;

import re.smartcity.common.data.Forecast;
import re.smartcity.energynet.*;

import java.time.LocalTime;

public class EnergyDistributor {

    // одинаковые для всех
    private String identy; // уникальный идентификатор
    private SupportedTypes componenttype; // тип компонента

    // общие характеристики
    private boolean isactive; // активность

    // частные характеристики
    private Object energyinput; // подсеть ввода энергии
    private Object[] outputs; // подсети потребителей
    private Object generation; // собственная генерация
    private Double cost; // тариф (?)

    // мгновенные значения (instant values / instantaneous values)
    private LocalTime timestamp; // метка времени примененных значений
    private Double currentcost; // действующая стоимость
    private Double currentpower; // мгновенное значения мощности потребления в МВт
    private Double current_greenenergy; // значение мощности, приходящейся на зеленную энергетику (?)

    // значения с накоплением для текущей временной метки
    // (* для вычисления используются предыдущие мгновенные значения)
    private Double electricitymeter; // потребленная электроэнергия с накоплением в кВт*ч.
    private Double greenenergy_meter; // потребленная электроэнергия с накоплением, приходящая на зеленную энергетику
    private Double topay; // стоимость потребленной электроэнергии в руб.

}
