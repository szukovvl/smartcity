package re.smartcity.energynet.component;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.data.Forecast;
import re.smartcity.energynet.*;

import java.time.LocalTime;

@Table("component")
public class Generation implements IComponentIdentification {

    // одинаковые для всех
    @Id
    private String identy; // уникальный идентификатор
    private SupportedTypes componenttype = SupportedTypes.GENERATOR; // тип компонента

    // общие характеристики
    private Forecast forecast; // прогноз
    private boolean useforecast = false; // задействовать прогноз
    private double energy; // максимальная мощность в МВт
    private boolean isactive = false; // активность

    // частные характеристики
    private double highload = 0.8; // значение в процента от генерируемой мощности, высокая нагрузка
    private double criticalload = 0.9; // значение в процентах от генерируемой мощности, критическая нагрузка
    private int blackouttime = 300; // время в секундах, прежде чем произойдет отключение генерации
    private double tariff; // тариф
    private double carbon = 930; // г/кВт*ч
    private GenerationUsageModes mode = GenerationUsageModes.RESERVE; // режим использования

    //region динамические характеристики
    //region мгновенные значения (instant values / instantaneous values)
    private LocalTime timestamp; // метка времени примененных значений
    private double currentcost; // действующая тариф
    private double currentpower; // мгновенное значения мощности потребления в МВт
    private double currentgeneration; // мгновенное значение генерации
    private double unusedenergy; // незадействованные мощности
    private double currentcarbon; // мгновенное значения загрязнения
    private SupportedLoadIndicators loadindicator; // индикатор нагрузки
    //endregion

    //region значения с накоплением для текущей временной метки
    // (* для вычисления используются предыдущие мгновенные значения)
    private double electricitymeter; // потребленная электроэнергия с накоплением в кВт*ч.
    private double topay; // стоимость потребленной электроэнергии в руб.
    private double unusedmeter; // счетчик невостребованной энергии
    private double lostprofit; // упущенная выгода
    private double totalcarbon; // итоговое значения загрязнения
    //endregion
    //endregion

    public Generation() { }

    //region IComponentIdentification
    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public SupportedTypes getComponentType() { return this.componenttype; }
    //endregion

}
