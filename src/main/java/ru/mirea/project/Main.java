package ru.mirea.project;

import ru.mirea.project.db.DatabaseInitializer;
import ru.mirea.project.model.Order;
import ru.mirea.project.model.OrderStatus;
import ru.mirea.project.model.User;
import ru.mirea.project.repository.OrderRepository;
import ru.mirea.project.repository.UserRepository;
import ru.mirea.project.service.ExportService;
import ru.mirea.project.service.OrderService;
import ru.mirea.project.service.UserService;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        try {
            DatabaseInitializer.initialize();

            UserRepository userRepository = new UserRepository();
            OrderRepository orderRepository = new OrderRepository();

            UserService userService = new UserService(userRepository);
            OrderService orderService = new OrderService(orderRepository, userRepository);
            ExportService exportService = new ExportService();

            boolean work = true;

            while (work) {

                System.out.println(
                        "========================================\n" +
                                "       СИСТЕМА ПРОДАЖИ ВИНИЛА\n" +
                                "========================================\n" +
                                "1. Пользователи\n" +
                                "2. Заказы\n" +
                                "3. Поиск\n" +
                                "4. Фильтрация\n" +
                                "5. Статистика\n" +
                                "6. Экспорт данных\n" +
                                "7. Таблица базы данных\n" +
                                "0. Выход\n" +
                                "Выберите действие:"
                );

                String choice = scanner.nextLine();

                try {
                    switch (choice) {

                        case "1":
                            usersMenu(scanner, userService);
                            break;

                        case "2":
                            ordersMenu(scanner, orderService);
                            break;

                        case "3":
                            searchOrders(scanner, orderService);
                            break;

                        case "4":
                            filterOrders(scanner, orderService);
                            break;

                        case "5":
                            showStatistics(orderService, userService);
                            break;

                        case "6":
                            exportOrders(userService, orderService, exportService);
                            break;

                        case "7":
                            showDatabase();
                            break;

                        case "0":
                            work = false;
                            System.out.println("Выход из системы. До свидания!");
                            break;

                        default:
                            System.out.println("Ошибка: неверный пункт меню!");
                            break;
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
            }

        } catch (Exception e) {

            System.out.println("Произошла ошибка:");
            e.printStackTrace();

        } finally {

            scanner.close();
        }
    }


    // Меню пользователей
    public static void usersMenu(Scanner scanner, UserService userService) throws SQLException {
        boolean back = false;

        while (!back) {
            System.out.println(
                    "\n===== ПОЛЬЗОВАТЕЛИ =====\n" +
                            "1. Создать пользователя\n" +
                            "2. Показать всех пользователей\n" +
                            "3. Найти пользователя по ID\n" +
                            "4. Обновить пользователя\n" +
                            "5. Удалить пользователя\n" +
                            "0. Назад\n" +
                            "Выберите действие:"
            );

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        createUser(scanner, userService);
                        break;
                    case "2":
                        showUsers(userService);
                        break;
                    case "3":
                        System.out.println(userService.findById(readLong(scanner, "Введите ID пользователя: ")));
                        break;
                    case "4":
                        updateUser(scanner, userService);
                        break;
                    case "5":
                        deleteUser(scanner, userService);
                        break;
                    case "0":
                        back = true;
                        break;
                    default:
                        System.out.println("Неверный пункт меню.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    public static void createUser(Scanner scanner, UserService userService) throws SQLException {
        String fullName = readText(scanner, "ФИО: ");
        String email = readText(scanner, "Email: ");
        String phone = readText(scanner, "Телефон: ");
        long id = userService.create(fullName, email, phone);
        System.out.println("Пользователь создан. ID: " + id);
    }

    public static void updateUser(Scanner scanner, UserService userService) throws SQLException {
        long id = readLong(scanner, "Введите ID пользователя: ");
        String fullName = readText(scanner, "ФИО: ");
        String email = readText(scanner, "Email: ");
        String phone = readText(scanner, "Телефон: ");
        userService.update(id, fullName, email, phone);
        System.out.println("Пользователь обновлен.");
    }

    public static void deleteUser(Scanner scanner, UserService userService) throws SQLException {
        long id = readLong(scanner, "Введите ID пользователя: ");
        userService.delete(id);
        System.out.println("Пользователь удален.");
    }

    public static void showUsers(UserService userService)
            throws SQLException {

        List<User> users = userService.findAll();

        System.out.println("\n===== ПОЛЬЗОВАТЕЛИ =====");

        if (users.size() == 0) {
            System.out.println("Пользователей нет.");
            return;
        }

        for (User user : users) {
            System.out.println(user);
        }
    }


    // Меню заказов
    public static void ordersMenu(
            Scanner scanner,
            OrderService orderService) throws SQLException {

        boolean back = false;

        while (!back) {
            System.out.println(
                    "\n===== ЗАКАЗЫ =====\n" +
                            "1. Создать заказ\n" +
                            "2. Показать все заказы\n" +
                            "3. Найти заказ по ID\n" +
                            "4. Обновить заказ\n" +
                            "5. Удалить заказ\n" +
                            "6. Сортировка заказов\n" +
                            "0. Назад\n" +
                            "Выберите действие:"
            );

            String choice = scanner.nextLine();

            try {
                switch (choice) {

                    case "1":
                        createOrder(scanner, orderService);
                        break;

                    case "2":
                        showOrders(orderService.findAll());
                        break;

                    case "3":
                        long id = readLong(scanner, "Введите ID заказа: ");
                        System.out.println(orderService.findById(id));
                        break;

                    case "4":
                        updateOrder(scanner, orderService);
                        break;

                    case "5":
                        deleteOrder(scanner, orderService);
                        break;

                    case "6":
                        sortOrders(scanner, orderService);
                        break;

                    case "0":
                        back = true;
                        break;

                    default:
                        System.out.println("Неверный пункт меню.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }


    // Создание заказа
    public static void createOrder(
            Scanner scanner,
            OrderService orderService) throws SQLException {

        long userId = readLong(scanner, "ID пользователя: ");
        String album = readText(scanner, "Название пластинки: ");
        String artist = readText(scanner, "Исполнитель: ");
        int quantity = readInt(scanner, "Количество: ");
        BigDecimal price = readBigDecimal(scanner, "Сумма заказа: ");
        OrderStatus status = readStatus(scanner);

        long id = orderService.create(
                userId,
                album,
                artist,
                quantity,
                price,
                status
        );

        System.out.println("Заказ создан. ID: " + id);
    }


    // Обновление заказа
    public static void updateOrder(
            Scanner scanner,
            OrderService orderService) throws SQLException {

        long id = readLong(scanner, "Введите ID заказа: ");
        long userId = readLong(scanner, "ID пользователя: ");
        String album = readText(scanner, "Название пластинки: ");
        String artist = readText(scanner, "Исполнитель: ");
        int quantity = readInt(scanner, "Количество: ");
        BigDecimal price = readBigDecimal(scanner, "Сумма заказа: ");
        OrderStatus status = readStatus(scanner);

        orderService.update(
                id,
                userId,
                album,
                artist,
                quantity,
                price,
                status
        );

        System.out.println("Заказ обновлен.");
    }


    // Удаление заказа
    public static void deleteOrder(
            Scanner scanner,
            OrderService orderService) throws SQLException {

        long id = readLong(scanner, "Введите ID заказа: ");
        orderService.delete(id);
        System.out.println("Заказ удален.");
    }


    // Вывод заказов
    public static void showOrders(List<Order> orders) {

        System.out.println("\n===== ЗАКАЗЫ =====");

        if (orders.size() == 0) {
            System.out.println("Заказов нет.");
            return;
        }

        for (Order order : orders) {
            System.out.println(order);
        }
    }


    // Поиск
    public static void searchOrders(
            Scanner scanner,
            OrderService orderService) throws SQLException {

        System.out.println(
                "\n===== ПОИСК =====\n" +
                        "1. По названию пластинки\n" +
                        "2. По исполнителю\n" +
                        "3. По названию или исполнителю\n" +
                        "4. По ID заказа\n" +
                        "0. Назад\n" +
                        "Выберите действие:"
        );

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                showOrders(orderService.searchByAlbum(readText(scanner, "Название пластинки: ")));
                break;
            case "2":
                showOrders(orderService.searchByArtist(readText(scanner, "Исполнитель: ")));
                break;
            case "3":
                showOrders(orderService.search(readText(scanner, "Текст поиска: ")));
                break;
            case "4":
                System.out.println(orderService.findById(readLong(scanner, "ID заказа: ")));
                break;
            case "0":
                break;
            default:
                System.out.println("Неверный пункт меню.");
                break;
        }
    }


    // Фильтрация
    public static void filterOrders(
            Scanner scanner,
            OrderService orderService) throws SQLException {

        System.out.println(
                "\n===== ФИЛЬТРАЦИЯ =====\n" +
                        "1. По статусу\n" +
                        "2. По пользователю\n" +
                        "0. Назад\n" +
                        "Выберите действие:"
        );

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                showOrders(orderService.filterByStatus(readStatus(scanner)));
                break;
            case "2":
                showOrders(orderService.filterByUser(readLong(scanner, "ID пользователя: ")));
                break;
            case "0":
                break;
            default:
                System.out.println("Неверный пункт меню.");
                break;
        }
    }


    // Сортировка
    public static void sortOrders(
            Scanner scanner,
            OrderService orderService) throws SQLException {

        System.out.println(
                "\n===== СОРТИРОВКА =====\n" +
                        "1. По сумме\n" +
                        "2. По дате\n" +
                        "0. Назад\n" +
                        "Выберите действие:"
        );

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                showOrders(orderService.sortByTotalPrice());
                break;
            case "2":
                showOrders(orderService.sortByDate());
                break;
            case "0":
                break;
            default:
                System.out.println("Неверный пункт меню.");
                break;
        }
    }


    // Статистика
    public static void showStatistics(
            OrderService orderService,
            UserService userService) throws SQLException {

        System.out.println("\n===== СТАТИСТИКА =====");

        long userCount = userService.countUsers();
        long orderCount = orderService.countOrders();
        Map<OrderStatus, Long> counts = orderService.countByStatus();

        System.out.println("Всего пользователей: " + userCount);
        System.out.println("Всего заказов: " + orderCount);
        System.out.println("Новых заказов: " + counts.getOrDefault(OrderStatus.NEW, 0L));
        System.out.println("Оплаченных заказов: " + counts.getOrDefault(OrderStatus.PAID, 0L));
        System.out.println("Отправленных заказов: " + counts.getOrDefault(OrderStatus.SHIPPED, 0L));
        System.out.println("Завершенных заказов: " + counts.getOrDefault(OrderStatus.COMPLETED, 0L));
        System.out.println("Отмененных заказов: " + counts.getOrDefault(OrderStatus.CANCELED, 0L));
        System.out.println("Общая сумма продаж: " + orderService.calculateRevenue());
        System.out.println("Средняя стоимость заказа: " + orderService.calculateAverageOrderPrice());
    }


    // Экспорт
    public static void exportOrders(
            UserService userService,
            OrderService orderService,
            ExportService exportService) throws Exception {

        Path file = exportService.exportAllTablesToXlsx(
                userService.findAll(),
                orderService.findAll(),
                Path.of("exports", "orders.xlsx")
        );

        System.out.println(
                "Данные экспортированы в: "
                        + file.toAbsolutePath()
        );
    }


    // Информация о БД
    public static void showDatabase() {

        System.out.println(
                "\n===== БАЗА ДАННЫХ =====\n" +
                        "users  - пользователи\n" +
                        "orders - заказы\n" +
                        "SQL-файл: src/main/resources/database.sql"
        );
    }


    // Выбор статуса
    public static OrderStatus readStatus(Scanner scanner) {

        System.out.println(
                "\nВыберите статус:\n" +
                        "1. Новый\n" +
                        "2. Оплачен\n" +
                        "3. Отправлен\n" +
                        "4. Завершен\n" +
                        "5. Отменен"
        );

        String choice = scanner.nextLine();

        return OrderStatus.fromMenuValue(choice);
    }

    public static String readText(Scanner scanner, String text) {
        System.out.print(text);
        return scanner.nextLine();
    }

    public static long readLong(Scanner scanner, String text) {
        while (true) {
            try {
                System.out.print(text);
                return Long.parseLong(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: нужно ввести целое число.");
            }
        }
    }

    public static int readInt(Scanner scanner, String text) {
        while (true) {
            try {
                System.out.print(text);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: нужно ввести целое число.");
            }
        }
    }

    public static BigDecimal readBigDecimal(Scanner scanner, String text) {
        while (true) {
            try {
                System.out.print(text);
                return new BigDecimal(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: нужно ввести число.");
            }
        }
    }
}
