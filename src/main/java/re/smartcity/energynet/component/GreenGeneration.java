package re.smartcity.energynet.component;

import re.smartcity.energynet.*;

import java.time.LocalTime;

public class GreenGeneration {

    // одинаковые для всех
    private String identy; // уникальный идентификатор
    private SupportedTypes componenttype; // тип компонента

    // общие характеристики
    private Double energy; // проектная мощность в МВт (или текущаа генерация ?)
    private boolean isactive; // активность
    private Double cost; // тариф (?)

    // частные характеристики
    private SupportedGenerations generation_type; // тип генерации
    private Object energyline; // энерговвод (?)
    private Integer highload; // значение в процента от генерируемой мощности, высокая нагрузка
    private Integer criticalload; // значение в процентах от генерируемой мощности, критическая нагрузка
    private Integer blackouttime; // время в секундах, прежде чем произойдет отключение генерации
    private GenerationUsageModes mode; // режим использования

    // мгновенные значения (instant values / instantaneous values)
    private LocalTime timestamp; // метка времени примененных значений
    private Double currentcost; // действующая стоимость
    private Double currentpower; // мгновенное значения мощности потребления в МВт
    private Double currentgeneration; // мгновенное значение генерации
    private Double unusedenergy; // незадействованные мощности
    private SupportedLoadIndicators loadindicator; // индикатор нагрузки

    // значения с накоплением для текущей временной метки
    // (* для вычисления используются предыдущие мгновенные значения)
    private Double electricitymeter; // потребленная электроэнергия с накоплением в кВт*ч.
    private Double topay; // стоимость потребленной электроэнергии в руб.
    private Double unusedmeter; // счетчик невостребованной энергии
    private Double lostprofit; // упущенная выгода

}
