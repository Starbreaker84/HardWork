## Пишем безошибочный код

Основная идея заключается в том, что мы исключаем некорретное поведение прогрыммы путем исключения такой ситуации на уровне компиляции.

### Пример 1
Кредитная заявка проходит нексколько стадий (для упрощения примера пусть их будет 3 - Native, Refilled, Final. На текущий момент в проекте стадийность определяется значениями определённых полей, которые на всем процессе (да, все программные модули работают в Camunda) проходят через тонны if else и switch конструкций. Баги вылазят регулярно. Мной предложено решение вынести в отдельные объекты заявки на определннной стадии и отрефакторить класс, который совершал "переход" заявки из одного состояния в другое. Сами классы должны быть final, чтобы нельзя было переопределить их конструктор предком, а также 

### Было:
``` Java
// Метод "преобразования" сущности
public Opportunity finalisingProcess(Opportunity opp) {
    // Обработка полей сущности
    if (someFiled != criticalValue) {
        throw RuntimeExceprion("Невозможное значение поля someField");

}

class Opportunity{
    // Множество полей и геттеров/сеттеров
    public Opportunity();

}
```

### Стало:
``` Java
// Методы "преобразования" сущности
public FinalOpportunity finalisingProcess(RefilledOpportunity opp) {
    FinalOpportunity finalOpportunity = new FinalOpportunity();
    // Обработка полей сущности
    return FinalOpportunity;

}

final class NativeOpportunity{
    // Множество полей и геттеров/сеттеров
    protected NativeOpportunity();

}

final class RefilledOpportunity{
    // Множество полей и геттеров/сеттеров
    protected RefilledOpportunity();

}

final class FinalOpportunity{
    // Множество полей и геттеров/сеттеров
    protected FinalOpportunity();

}
```

### Пример 2
Далее, описанный подход был применим к некоторым другим проектам. Напрмер к получению токена после авторизации клиента. В принципе, данных подход можно ьыло применить и к паролю.

### Было:
``` Java 
char[] token = authenticationService.authentication(login, password);
```
### Стало:
``` Java
public final class Token {

  protected Token(){
  }

}

Token token = authenticationService.authentication(login, password);
```

### Пример 3
Ну и достаточно тривиальный пример, который однако действительно дал свою пользу. Небольшой локальный проект ветеринарной клиники. В коде была сущность карточки "клиента", которая была всегда либо в работе, либо открывалась для нового "клиента", либо была закрыта(в спящем режиме) до следующего обращения.

### Было:
``` Java
class Application{
    private String status;

    Application(){
    }

}
```

### Стало:
``` Java
final class ApplicationInWork{

    protected ApplicationInWork(){
    }



}

final class ApplicationClose{

    protected ApplicationClose(){
    }

}

//в управдяющем классе были добавлены соответствующие методы
public ApplicationInWork workApplication(ApplicationClose app){
}

public ApplicationInWork workApplication(){
}

public ApplicationClose closeApplication(ApplicationInWork app){
}
```

## Выводы:
К сожалению, в современной корпоративной среде данный подход откдика не находит от слова совсем. Но вот мне он наоброт сильно импонирует, даже несмотря на свою вроде бы избыточность. Да, в команде уровень остальных разработчиков должен быть как минимум способен понять данную архитектуру и легко её расширять, что далеко от действительности. Но сам подход считаю классным, ещё постараюсь добавить ультра-строгую типизацию как в Ada, и буду пытаться писать "неубиваемые" приложения. К подходу напрашивается также хороший инструмент для ведения uml диаграм, но ни в коем случае не с поддержкой кодогенерации, а именно как слепок архитектуры для системного анализа.
