# timing-logger
## Библиотека для измерения и логирования длительности исполнения участков кода.
### Единичные измерения
Результаты измерений можно отправить в:
* StatsD 
* Slf4j

Примеры:
```java
 /*
  Самый простой кейс
  Логируем длительность одного метода
  Этот же логгер нигде больше вызывать не хоитм
  */
public class VacancyPublicator {
  public void publishVacancy(){
        Timings.Builder timingsBuilder = new Timings.Builder();
        var timings = timingsBuilder
          .withStatsDSender(statsDSender)
          .withMetric("VacancyPublicator.coolMetric")
          .start();
          
          doLotOfStuffFirst();
          timings.timeWithDefaultTag("First_Stuff_Done"); // Логируем сколько прошло с момента старта
          
          doLotOfStuffSecond();
          timings.timeWithDefaultTag("Second_Stuff_Done"); // Логируем сколько прошло с момента предыдущего логирования 
          //...
          timings.timeWholeWithDefaultTag("just_before_return"); // Логируем полную длительность с момента старта
  }
} 
```
```java
 /*
  Кейс посложнее
  Хотим логировать одним и тем же логгером в разных местах - внутри вложенных вызововов, хуков и тп
  Для этого инстанс логгера хранится в ThreadLocal
  */
public class VacancyPublicator {
  public void publishVacancy(){
        Timings.Builder timingsBuilder = new Timings.Builder();
        try (
            var timings = timingsBuilder
              .withStatsDSender(statsDSender)
              .withMetric("VacancyPublicator.coolMetric")
              // тот самый ThreadLocal             
              .withThreadLocal()
              .start()) {
          
          doLotOfStuffFirst();
          timings.timeWithDefaultTag("First_Stuff_Done"); // Логируем сколько прошло с момента старта
          
          doLotOfStuffSecond();
          timings.timeWithDefaultTag("Second_Stuff_Done"); // Логируем сколько прошло с момента предыдущего логирования 
          //...
          timings.timeWholeWithDefaultTag("just_before_return"); // Логируем полную длительность с момента старта
        }
  }
}

public class VeryDemandingSaveUpdateListener {
  public void onSaveOrUpdate(){
    Timings.get().time(new Tag("InListener", "start_listener")); 
    //...A lot of heavy stuff happening 
    Timings.get().time(new Tag("InListener", "finish_listener"));
  }
}
```
### Поэтапные измерения - измерение перцентилей этапов процесса
Результаты измерений можно отправить в StatsD 
```java
class Service {
  enum SessionStage {
    STAGE_ONE,
    STAGE_TWO
  }
  /**
    используется threadlocal таймер, т.е. этапы измеряются в одном потоке
  */
  public void stagedLogic() {
    stageTimings.start();
    stageOne();
    stageTimings.markStage(SessionStage.STAGE_ONE);
    stageTwo();
    stageTimings.markStage(SessionStage.STAGE_TWO);
  }
}
```
