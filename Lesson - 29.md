## Про раздутость кода

Основная идея здесь не так очевилна, как кажется. Раздутость кода - это про его логику, а не про его объём. Как известно, в идеале, сигнатура метода уже определяет то, что будет выполнять этот метод. Но, если в зависимости от контекста, метод начинает вести себя по разному - вот тут мы и получаем раздутость кода, место, где по идее, должно быть несколько методов со своими характерными сигнатурами, но на практике один метод со множеством вызывающих контекстов.

Разумеется это никак не добавляет читаемости и поддерживаемости такому коду. Честно говоря, не ожидал увидеть такой код в продакшене, но "кровавый энтерпрайз" меня удивил. Все примеры относятся к слою бизнес-логики (сервисов).

### Пример 1
При назначении/обновлении данных контейнера ковенантов метод, в зависимости от значения флага, либо назначал абсолютно новые данный в зависимости от входной сущности, либо обновлял данные контейнера значениями из входной сущности. Поскольку метод относится к клиенту для сохранения данных в центральное хранилище, то он испоользуется в разных микросервисах, и это в свлю очередь породило не мло багов и их исправлениия костылями.

Задача была вынесена в техдолг. Я предложил избавиться от флага и сделать два явных метода setCovenant(...) и updateCovenant(...). Юлагодаря прозрачной и понятной сигнатуре каждого метода, даже избавление от костылей со старой логикой произошло с минимальными правками.

### Пример 2
Один из методов работы с репозиторием микросервиса явно нарушал принцип SRP. По флагу (точнее значению из сущности), загруженные из репозитория данные, сначала отправлялись в другой сервис для обработки, и только потом отдавались на следующий слой. Обнаружили "раздутый код", когда соседняя команда пришла с вопросом на различное поведение нашего микросервиса, в зависимости от места вызова.

Для решения проблемы явно отделили обработку данных в отдельный сервсис внутри микросервиса и очистили метод сервиса, который работает с репозиторием от всего лишнего.

### Пример 3
Метод поиска в репозитории по сущности имел флаг, по которому можно было дополнительно синхронизировать данные с центральным хранилещем на проприетарной базе данных. Про флаг необходимо было помнить, так как это прямо влияло на логику программы в отдельных местах. Проблема вскрылась, когда на тестовом стенде, при тестировании новой бизнес-логики сервиса появилось расхождение данных между продакшеном и тестом. 

После выяления проблемы, был выставлен критический техдолг. Метод работы с репозиторием был очищен от флага. Синхронизацию между двумя базами выделили в отдельный слой сервиса, который работал с хранилищами. Бизнес логику существующего кода затрагивать даже не пришлось. Но тут повезло, что называется.

## Выводы:
Как правило, раздутость кода свойственна импульсивных решениям при разработке ПО. Я поинтересовался, откуда появились такие странные решения. Выяснилось, что они были ни чем иным как доработками уже на финаотных стадиях, когда внезапно бизнесу понадобилось новое немного отличное поведение от того, что уже есть. Я на практике убедился, что уж лучше потратить больше времени на перепроектирование того что есть, чем потом ловить баги в продакшене и терять убытки из-за раздутости кода. При разработке своих решений, я вообще избегаю такого поведения "по флажку", оно мне всегда казалось странным и каким-то искусственным.