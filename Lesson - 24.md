## Избавляемся от зависимости от зависимостей-3

### 1. Зависимость от фреймворка
Проект зависит от фрэймворка логирования.

Решение: программирование на интрефейсах, тогда при смене фреймворка, для основного кода подмена будет не заметна вообще.

### 2. Расширенность формата
Согласованность форматов обмена данными между сервисами посредством Rest API. При проектировании новых сервисов или расширения функционала существующих, много времени уходит на согласование контрактов.

Решение: использование контракторв на основе Open API и генерации клиентов к ним.

### 3. Зависимость зависимости
В репозитории общих библиотек есть библиотеки с транзитивными зависимостями. При попытке "дотягивать" такие библиотеки до нужных версий, часто возникает проблема обратной совместимости.

Решение: выделили отдельный модуль в репозитории для свежих версий библиотек, чтобы не нарушать обратной совместимости. Теперь проекты каждый работают со своими версиями библиотек.

### 4. Зависимость краша
RuntimeException при записи данных в хранилища, в которых закончилось место или они недоступны и т.п.

Решение: в спецификайии методов теперь обязательно прописана обработка таких исключений "на уровне железа", ведется отдельный чек-поинт с возможными исключениями в таких случаях.

### 5. Зависимость перебрасывания
Скоринг клиента осуществлялся с помощью одного сервиса, который постоянно сбоил.

Решение: внедрили механизм обхода скорингом нескольких сервисов, для того чтобы исключить сбой при отправке и получении данных. Для компани это стало несколько дороже, зато процент отказа в обслуживании клиентов стал значительно меньше, что в свою очередь, легко компенсировало производственные потери.

### 6. Зависимость инверсии
DI предполагает написание кода на интерфейсах, при этом, если у нас есть несколько реализаций, то необходимо корректно прописывать выбор конкретной реализации.

Решение: вызов конкретной реализации, если не предполагается её иной интерпретации.

### 7. Зацикливание зависимостей
При использовании внедрения зависимостей через поля, может возникнуть зацикливание зависимостей. Что уже само по себе намекает на плохое решение.

Решение: на уровне линтера запретили внедрение зависимостей через поля, только через конструктор, что в свою очередь, запрещает зацикливание зависимостей на уровне компилятора.

### 8. Зависимость высшего порядка.
Пример со Spring Data JPA. При реализации своего репозитория легко можно подменить стандартную логику Spring. В итоге будет вызван наш функционал стандартной библиотекой Spring.

Решение: реализация логики строго в соответствии со спецификацией фреймворка.

### 9. Зависимость большинства
Согласование Merge Request, требующего 2 аппрува от лразработчиков из команды, не зависит от какого-то конкретного, но зависит от множества из двух любых разрабочтиков.

## Выводы:

Не думал, честно говоря, что тема зависимостей будет пронизывать прктически каждую строку кода. Теперь, прежде чем реализовать какую-либо логику, я думаю о вещах, как-будто бы посторонних, но в то же время тесно связанных. Безусловно, сознательное управление зависимостями позволяет сделать приложение гораздо более отказоустойчивым. Но тут возникает другая проблема: как донести эту философию до других разработчиков. Но это уже вопрос "разработческой" культуры, полагаю.
Теперь я на слой сервисов приложения смотрю с другой стороны. Раньше я даже не задумывался о тех зависимостях, которые он тянет, теперь же я понимаю что слой сервиса выступает в роли менеджера, и ни как не зависит от реализации своих зависимостей. По сути, идеальный слой сервиса должен был декларативным.
