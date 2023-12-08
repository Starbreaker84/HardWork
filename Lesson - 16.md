## Пример 1
Интересный экземпляр стретил не так давно у знакомого, проходящего стажировку. При решении классической CRUD задачи, он не нашел ничего лучше чем всю логику сервиса положить в класс контроллера, смешав все слои и сделав возможным только интеграционные тесты. Пример разберём на основе одного класса контроллера.

### Было:
**Класс контроллера**:
``` Java
@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonController {
    private final PersonDAO personDAO;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PersonShortResponse> persons() {
        return personDAO.findAll()
                .stream()
                .map(PersonMapper::toShortPerson)
                .collect(Collectors.toList());
    }

    ...
}
```
**Класс репозитория**:
``` Java
@Repository
public class PersonDaoImpl implements PersonDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Nonnull
    @Override
    public List<Person> findAll() {
        return entityManager.createQuery("FROM Person", Person.class).getResultList();
    }

    ...
}
```

### Стало:
**Класс контроллера**:
``` Java
@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonController {
    private final PersonService personService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PersonShortResponse> persons() {
        return personService.findAll();
    }

    ...
}
```

**Класс сервиса**:
``` Java
@Service
@RequiredArgsConstructor
public class PersonServiceImplimplements PersonService {

    private final PersonDao personDao;
    private final DepartmentDao departmentDao;

    @Nonnull
    @Override
    @Transactional(readOnly = true)
    public List<PersonShortResponse> findAll() {
        return personDao.findAll()
                .stream()
                .map(PersonMapper::toShortPerson)
                .collect(Collectors.toList());
    }

    ...
}
```

**Класс репозитория**:
``` Java
@Repository
public class PersonDaoImpl
        implements PersonDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Nonnull
    @Override
    public List<Person> findAll() {
        return entityManager.createQuery("FROM Person", Person.class).getResultList();
    }

    ...
}
```
Мы аккуратно разграничили логику посредством интерфейсов. Благодаря этом теперь каждый слой абстракции отвечает сугубо за свой участок, плюс стало возможным покрыть код unit-тестами, сняв нагрузку с мануа-QA в команде. На этом примере выигрыш от рефакторинга не очень заметен, но на самом деле, по мере роста бизнес логики такой подход разграничений даст свои хорошие плоды.

## Пример 2
Задача аписани игры крестики-нолики. Небезизестный AI в свое время написал отвратительную версию, свалив весь код в одну кучу.
``` Java
public class ChatGPTGeneratedSolution {
    static char[][] board;
    static char turn;

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        board = new char[3][3];
        turn = 'X';
        initializeBoard();

        System.out.println("Игра 'Крестики-нолики'.");
        System.out.println("Чтобы сделать ход, введите номер строки и номер столбца.");
        printBoard();

        while (true) {
            System.out.println("Ход игрока " + turn + ", введите номер строки и номер столбца:");
            int row = scan.nextInt() - 1;
            int col = scan.nextInt() - 1;

            if (board[row][col] == 'X' || board[row][col] == 'O') {
                System.out.println("Это поле уже занято, попробуйте другое поле");
                continue;
            }

            board[row][col] = turn;

            if (gameOver(row, col)) {
                System.out.println("Игра окончена! Игрок " + turn + " выиграл!");
                break;
            }

            printBoard();
            if (turn == 'X') {
                turn = 'O';
            } else {
                turn = 'X';
            }
        }
    }

    static void printBoard() {
        for (int i = 0; i < 3; i++) {
            System.out.println();
            for (int j = 0; j < 3; j++) {
                if (j == 0) {
                    System.out.print("| ");
                }
                System.out.print(board[i][j] + " | ");
            }
        }
        System.out.println();
    }

    static void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '_';
            }
        }
    }

    static boolean gameOver(int rMove, int cMove) {
        if (board[0][cMove] == board[1][cMove] && board[0][cMove] == board[2][cMove]) {
            return true;
        }
        if (board[rMove][0] == board[rMove][1] && board[rMove][0] == board[rMove][2]) {
            return true;
        }
        if (board[0][0] == board[1][1] && board[0][0] == board[2][2] && board[1][1] != '_') {
            return true;
        }
        if (board[0][2] == board[1][1] && board[0][2] == board[2][0] && board[1][1] != '_') {
            return true;
        }
        return false;
    }
}
```
Была проделана работа по рефакторингу данного кода, а именно, разделили всю логику посредством разбиения на классы, где у каждого класса есть свой ограниченный пул ответственности. Это нам позволяет в дальнейшем легко как модифицировать так и тестировать наши компоненты.
``` Java
public class AppRunner {
    public static void main(String[] args) {
        GameLogic gameLogic = new VersusPCGameLogicImpl();
        gameLogic.play();
    }
}
```
``` Java
public class Field {

    private final PlayerSymbol[] storage;

    public Field() {
        storage = new PlayerSymbol[9];
        Arrays.fill(storage, PlayerSymbol.BLANK);
    }

    public Field(PlayerSymbol[] storage) {
        this.storage = storage;
    }

    public PlayerSymbol[] getStorage() {
        return storage;
    }

    public void printBoard() {
        for (int i = 0; i < storage.length; i++) {
            if(i % 3 == 0 && i != 0) {
                System.out.println();
            }

            System.out.print((storage[i] == PlayerSymbol.BLANK ? i + 1 : storage[i].getValue()) + " ");

        }
        System.out.println();
    }
}
```
``` Java
public interface GameLogic {

    void play();
}
```
``` Java
public enum PlayerSymbol {
    X("X"),
    O("O"),
    BLANK(" ");

    private final String value;

    PlayerSymbol(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
```
``` Java
public class VersusPCGameLogicImpl implements GameLogic {

    private final Field field = new Field();
    private final Scanner scanner = new Scanner(System.in);
    private final Random random = new Random();

    private final PlayerSymbol HUMAN_SYMBOL = PlayerSymbol.X;
    private final PlayerSymbol PC_SYMBOL = PlayerSymbol.O;

    private boolean checkWinner(PlayerSymbol symbol) {
        PlayerSymbol[] symbols = field.getStorage();
        int[][] winnerCombinations = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };

        for (int[] combination : winnerCombinations) {
            if (symbols[combination[0]] == symbol
                    && symbols[combination[1]] == symbol
                    && symbols[combination[2]] == symbol) {
                return true;
            }
        }
        return false;
    }

    private boolean validateMove(int position) {
        return position >= 1 && position <= 9 && field.getStorage()[position - 1] == PlayerSymbol.BLANK;
    }

    private void getMove(PlayerSymbol currentPlayer) {
        if (currentPlayer == HUMAN_SYMBOL)
            humanMove();
        else
            pcMove();
    }

    private void humanMove() {
        int position;

        while (true) {
            System.out.println("Human move " + HUMAN_SYMBOL.getValue() + " enter position (1-9)");

            position = scanner.nextInt();
            if(validateMove(position)) {
                field.getStorage()[position - 1] = HUMAN_SYMBOL;
                break;
            }
            System.out.println("Incorrect input. Try again");
        }
    }

    private void pcMove() {
        int position;
        do {
            position = random.nextInt(9) + 1;
        } while (!validateMove(position));
        field.getStorage()[position - 1] = PC_SYMBOL;
    }

    @Override
    public void play() {
        PlayerSymbol currentPlayer = HUMAN_SYMBOL;
        while (true) {
            field.printBoard();
            getMove(currentPlayer);
            if (checkWinner(currentPlayer)) {
                System.out.println("Player " + currentPlayer.getValue() + " won!!!");
                break;
            }
            currentPlayer = (currentPlayer == HUMAN_SYMBOL) ? PC_SYMBOL : HUMAN_SYMBOL;
        }
    }
}
```

## Пример 3:
Тут хочу описать один из последних кейсов с работы, но придется описывать словами, так как NDA ещё никто не отменял, репозиторий публичный, но кейс очень показательный.

Имеется класс, который формирует сообщение для фронтэнда в зависимости от некторых условий под кредитной заявкой. Я молчу о том что "Зачем мы отдаёи отформатированную строку, когда можем отдать объект и пускай они там сами на фронте решают что клиенту показывать?". Ну вот так "исторически сложилось". Ладно. Класс на момент постановки новой задачи представлял из себя классический "спагетти код" из набора небольших методов и КУЧИ вложенных условий друг в друга. Его банально было невозможно нормально прочитать, я уже молчу о модификации.

Мной было предложено и реализовано следующее архитектурное решение. Мы под каждый вид сообщения создаём отдельный интерфейс и его имплементацию. В кредитной заявке добавляем доп. поле, характеризующее тип заявки (это было необходимо, потому что в некоторых случаях триггером выступало сочетание значений нескольких полей заявки). Ткже был создан отдельный класс, в котором осуществлялся выбор класса формирования сообщения из энамки, которую я создал чтобы убрать возможность использования if и switch.

В итоге, и написание тестов оказалось теперь чистым и прозрачным. И тестировлось уже не то как класс будет анализировать заявку (КАК?) а что он просто выдает сообщение с валидными данными из под заявки (ЧТО?). Получается тут границы ответственности тоже были очерчены с помощью интерфейсов.

## Итоги:
В общем, мне представляется что сейчас в основном разделяют ответственность именно интерфейсами, по крайней мере Java к этому сама подталкивает. Я ещё ни разу не видел чтобы на проекте где-либо использовалось наследование. Сам подобный рефакторинг хорошо развязывает руки разработчику. Конечно, иногда в проекте встречаются решения, которые уже никто не хочет, да и скорее всего не может отрефакторить, поскольку на однажды принятом архитектурном решении уже выстроена целая экосистема (как пример, раздутые схемы в камунде и как следствие, нечитаемые и крайне хрупкие процессные тесты, которые, как выяснилось, головная боль всего юнита кредитов для юр. лиц.).
