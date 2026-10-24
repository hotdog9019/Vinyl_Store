package ru.mirea.project.fx;

//import javafx.scene.control.ListView;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import ru.mirea.project.model.Order;
import ru.mirea.project.model.OrderStatus;
import ru.mirea.project.model.User;
import ru.mirea.project.model.Vinyl;
import ru.mirea.project.service.ExportService;
import ru.mirea.project.service.OrderService;
import ru.mirea.project.service.UserService;
import ru.mirea.project.service.VinylService;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MainController {
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final ObservableList<Order> visibleOrders = FXCollections.observableArrayList();
    private final ObservableList<Vinyl> vinyls = FXCollections.observableArrayList();

    private UserService userService;
    private OrderService orderService;
    private VinylService vinylService;
    private ExportService exportService;

//  @FXML private ListView<String> artistListView;
    @FXML private Label userCountLabel;
    @FXML private Label orderCountLabel;
    @FXML private Label revenueLabel;
    @FXML private Label averageLabel;
    @FXML private Label minOrderLabel;
    @FXML private Label maxOrderLabel;
    @FXML private Label statusBreakdownLabel;

    @FXML private TextField orderSearchField;
    @FXML private ComboBox<StatusFilter> statusFilterBox;
    @FXML private ComboBox<UserFilter> userFilterBox;
    @FXML private ComboBox<SortMode> sortModeBox;

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Long> orderIdColumn;
    @FXML private TableColumn<Order, String> orderUserColumn;
    @FXML private TableColumn<Order, String> orderArtistColumn;
    @FXML private TableColumn<Order, String> orderAlbumColumn;
    @FXML private TableColumn<Order, Integer> orderQuantityColumn;
    @FXML private TableColumn<Order, BigDecimal> orderTotalColumn;
    @FXML private TableColumn<Order, String> orderStatusColumn;
    @FXML private TableColumn<Order, String> orderCreatedColumn;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Long> userIdColumn;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userPhoneColumn;

    @FXML private TableView<Vinyl> vinylTable;
    @FXML private TableColumn<Vinyl, Long> vinylIdColumn;
    @FXML private TableColumn<Vinyl, String> vinylTitleColumn;
    @FXML private TableColumn<Vinyl, String> vinylArtistColumn;
    @FXML private TableColumn<Vinyl, String> vinylGenreColumn;
    @FXML private TableColumn<Vinyl, BigDecimal> vinylPriceColumn;
    @FXML private TableColumn<Vinyl, Integer> vinylStockColumn;

    @FXML
    private void initialize() {
        configureOrderColumns();
        configureUserColumns();
        configureVinylColumns();

        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        vinylTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        ordersTable.setItems(visibleOrders);
        usersTable.setItems(users);
        vinylTable.setItems(vinyls);

        statusFilterBox.setItems(FXCollections.observableArrayList(StatusFilter.all()));
        for (OrderStatus status : OrderStatus.values()) {
            statusFilterBox.getItems().add(StatusFilter.of(status));
        }
        statusFilterBox.getSelectionModel().selectFirst();
        statusFilterBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(StatusFilter value) {
                return value == null ? "" : value.title();
            }

            @Override
            public StatusFilter fromString(String value) {
                return null;
            }
        });
        //        artistListView.setItems(
//                FXCollections.observableArrayList(
//                        "The Beatles",
//                        "Pink Floyd",
//                        "Queen"
//                )
//        );
        sortModeBox.setItems(FXCollections.observableArrayList(
                SortMode.DATE_DESC,
                SortMode.PRICE_ASC,
                SortMode.PRICE_DESC,
                SortMode.QUANTITY_ASC,
                SortMode.QUANTITY_DESC
        ));
        sortModeBox.getSelectionModel().selectFirst();
        sortModeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SortMode value) {
                return value == null ? "" : value.title();
            }

            @Override
            public SortMode fromString(String value) {
                return null;
            }
        });
    }

    public void configure(UserService userService, OrderService orderService, VinylService vinylService, ExportService exportService) {
        this.userService = userService;
        this.orderService = orderService;
        this.vinylService = vinylService;
        this.exportService = exportService;
        refreshAll();
    }

    @FXML
    private void refreshAll() {
        loadUsers();
        loadVinyls();
        updateUserFilter();
        applyOrderFilters();
        loadStats();
    }

    @FXML
    private void applyOrderFilters() {
        try {
            List<Order> filtered = orderService.findAll();
            String query = orderSearchField.getText();
            if (query != null && !query.isBlank()) {
                String normalized = query.trim().toLowerCase();
                filtered = filtered.stream()
                        .filter(order -> contains(order.getAlbumTitle(), normalized) || contains(order.getArtist(), normalized))
                        .toList();
            }

            StatusFilter statusFilter = statusFilterBox.getValue();
            if (statusFilter != null && statusFilter.status() != null) {
                filtered = filtered.stream()
                        .filter(order -> order.getStatus() == statusFilter.status())
                        .toList();
            }

            UserFilter userFilter = userFilterBox.getValue();
            if (userFilter != null && userFilter.userId() != null) {
                filtered = filtered.stream()
                        .filter(order -> order.getUserId() == userFilter.userId())
                        .toList();
            }

            SortMode sortMode = sortModeBox.getValue();
            if (sortMode != null) {
                filtered = filtered.stream().sorted(sortMode.comparator()).toList();
            }

            visibleOrders.setAll(filtered);
        } catch (Exception e) {
            showError("Не удалось загрузить заказы", e);
        }
    }

    @FXML
    private void resetOrderFilters() {
        orderSearchField.clear();
        statusFilterBox.getSelectionModel().selectFirst();
        userFilterBox.getSelectionModel().selectFirst();
        sortModeBox.getSelectionModel().select(SortMode.DATE_DESC);
        applyOrderFilters();
    }

    @FXML
    private void addOrder() {
        showOrderDialog(null);
    }

    @FXML
    private void editOrder() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Выберите заказ в таблице.");
            return;
        }
        showOrderDialog(selected);
    }

    @FXML
    private void deleteOrder() {
        Order order = ordersTable.getSelectionModel().getSelectedItem();
        if (order == null) {
            showInfo("Выберите заказ в таблице.");
            return;
        }
        if (!confirm("Удалить выбранный заказ?")) {
            return;
        }
        try {
            orderService.delete(order.getId());
            refreshAll();
        } catch (Exception e) {
            showError("Не удалось удалить заказ", e);
        }
    }

    @FXML
    private void addUser() {
        showUserDialog(null);
    }

    @FXML
    private void editUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Выберите пользователя в таблице.");
            return;
        }
        showUserDialog(selected);
    }

    @FXML
    private void deleteUser() {
        User user = usersTable.getSelectionModel().getSelectedItem();
        if (user == null) {
            showInfo("Выберите пользователя в таблице.");
            return;
        }
        if (!confirm("Удалить выбранного пользователя?")) {
            return;
        }
        try {
            userService.delete(user.getId());
            refreshAll();
        } catch (Exception e) {
            showError("Не удалось удалить пользователя", e);
        }
    }

    @FXML
    private void showSelectedUserOrders() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Выберите пользователя в таблице.");
            return;
        }
        userFilterBox.getSelectionModel().select(userFilterFor(selected.getId()));
        applyOrderFilters();
    }

    @FXML
    private void addVinyl() {
        showVinylDialog(null);
    }

    @FXML
    private void editVinyl() {
        Vinyl selected = vinylTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Выберите пластинку в таблице.");
            return;
        }
        showVinylDialog(selected);
    }

    @FXML
    private void deleteVinyl() {
        Vinyl selected = vinylTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Выберите пластинку в таблице.");
            return;
        }
        if (!confirm("Удалить выбранную пластинку из каталога?")) {
            return;
        }
        try {
            vinylService.delete(selected.getId());
            refreshAll();
        } catch (Exception e) {
            showError("Не удалось удалить пластинку", e);
        }
    }

    @FXML
    private void exportData() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить Excel-файл");
        chooser.setInitialFileName("vinyl-store-export.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel workbook", "*.xlsx"));
        java.io.File file = chooser.showSaveDialog(ordersTable.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            Path exported = exportService.exportAllTablesToXlsx(userService.findAll(), List.copyOf(visibleOrders), file.toPath());
            showInfo("Данные экспортированы: " + exported);
        } catch (Exception e) {
            showError("Не удалось экспортировать данные", e);
        }
    }

    private void configureOrderColumns() {
        setCell(orderIdColumn, Order::getId);
        setCell(orderUserColumn, order -> userTitle(order.getUserId()));
        setCell(orderArtistColumn, Order::getArtist);
        setCell(orderAlbumColumn, Order::getAlbumTitle);
        setCell(orderQuantityColumn, Order::getQuantity);
        setCell(orderTotalColumn, Order::getTotalPrice);
        setCell(orderStatusColumn, order -> order.getStatus().getTitle());
        setCell(orderCreatedColumn, order -> order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
    }

    private void configureUserColumns() {
        setCell(userIdColumn, User::getId);
        setCell(userNameColumn, User::getFullName);
        setCell(userEmailColumn, User::getEmail);
        setCell(userPhoneColumn, User::getPhone);
    }

    private void configureVinylColumns() {
        setCell(vinylIdColumn, Vinyl::getId);
        setCell(vinylTitleColumn, Vinyl::getTitle);
        setCell(vinylArtistColumn, Vinyl::getArtist);
        setCell(vinylGenreColumn, Vinyl::getGenre);
        setCell(vinylPriceColumn, Vinyl::getPrice);
        setCell(vinylStockColumn, Vinyl::getStock);
    }

    private <S, T> void setCell(TableColumn<S, T> column, Function<S, T> mapper) {
        column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(mapper.apply(data.getValue())));
    }

    private void loadUsers() {
        try {
            users.setAll(userService.findAll());
        } catch (Exception e) {
            showError("Не удалось загрузить пользователей", e);
        }
    }

    private void loadVinyls() {
        try {
            vinyls.setAll(vinylService.findAll());
        } catch (Exception e) {
            showError("Не удалось загрузить пластинки", e);
        }
    }

    private void updateUserFilter() {
        UserFilter selected = userFilterBox.getValue();
        ObservableList<UserFilter> filters = FXCollections.observableArrayList(UserFilter.all());
        filters.addAll(users.stream().map(user -> new UserFilter(user.getId(), user.getFullName())).toList());
        userFilterBox.setItems(filters);
        userFilterBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(UserFilter value) {
                return value == null ? "" : value.title();
            }

            @Override
            public UserFilter fromString(String value) {
                return null;
            }
        });
        if (selected != null && filters.contains(selected)) {
            userFilterBox.getSelectionModel().select(selected);
        } else {
            userFilterBox.getSelectionModel().selectFirst();
        }
    }

    private void loadStats() {
        try {
            List<Order> allOrders = orderService.findAll();
            userCountLabel.setText(String.valueOf(users.size()));
            orderCountLabel.setText(String.valueOf(allOrders.size()));
            revenueLabel.setText(orderService.calculateRevenue().toPlainString());
            averageLabel.setText(orderService.calculateAverageOrderPrice().toPlainString());
            minOrderLabel.setText(allOrders.stream()
                    .map(Order::getTotalPrice)
                    .min(BigDecimal::compareTo)
                    .map(BigDecimal::toPlainString)
                    .orElse("0.00"));
            maxOrderLabel.setText(allOrders.stream()
                    .map(Order::getTotalPrice)
                    .max(BigDecimal::compareTo)
                    .map(BigDecimal::toPlainString)
                    .orElse("0.00"));

            Map<OrderStatus, Long> byStatus = allOrders.stream()
                    .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
            String statusText = "";
            for (OrderStatus status : OrderStatus.values()) {
                if (!statusText.isBlank()) {
                    statusText += "   ";
                }
                statusText += status.getTitle() + ": " + byStatus.getOrDefault(status, 0L);
            }
            statusBreakdownLabel.setText(statusText);
        } catch (Exception e) {
            showError("Не удалось обновить статистику", e);
        }
    }

    private void showUserDialog(User user) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Новый пользователь" : "Редактирование пользователя");
        ButtonType saveButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, saveButton);

        TextField name = new TextField(user == null ? "" : user.getFullName());
        TextField email = new TextField(user == null ? "" : user.getEmail());
        TextField phone = new TextField(user == null ? "" : user.getPhone());
        limitLength(name, 50);
        limitLength(email, 120);
        phone.setPromptText("+79991234567");
        phone.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("[+\\d ()-]{0,20}") ? change : null));
        dialog.getDialogPane().setContent(formGrid(
                row("ФИО", name),
                row("Email", email),
                row("Телефон", phone)
        ));
                keepDialogOpenOnError(dialog, saveButton, "Не удалось сохранить пользователя", () -> {
            if (user == null) {
                userService.create(name.getText(), email.getText(), phone.getText());
            } else {
                userService.update(user.getId(), name.getText(), email.getText(), phone.getText());
            }
        });
        dialog.showAndWait();
    }

//private void showUserDialog(User user) {
//    Dialog<ButtonType> dialog = new Dialog<>();
//    dialog.setTitle(user == null ? "Новый пользователь" : "Редактирование пользователя");
//    ButtonType saveButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
//    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, saveButton);
//
//    TextField name = new TextField(user == null ? "" : user.getFullName());
//    TextField email = new TextField(user == null ? "" : user.getEmail());
//    TextField phone = new TextField(user == null ? "" : user.getPhone());
//    phone.setPromptText("+79991234567");
//    phone.setTextFormatter(new TextFormatter<String>(change -> change.getControlNewText().matches("[+\\d ()-]{0,20}") ? change : null));
//    dialog.getDialogPane().setContent(formGrid(row("ФИО", name), row("Email", email), row("Телефон", phone)));
//    dialog.setResultConverter(button -> button);
//    ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
//
//    if (result != saveButton) {
//        return;
//    }
//    try {
//        if (user == null) {
//            userService.create(name.getText(), email.getText(), phone.getText());
//        } else {
//            userService.update(user.getId(), name.getText(), email.getText(), phone.getText());
//        }
//        refreshAll();
//    } catch (Exception e) {
//        showError("Не удалось сохранить пользователя", e);
//    }
//}



    private void showOrderDialog(Order order) {
        if (users.isEmpty()) {
            showInfo("Сначала создайте пользователя.");
            return;
        }
        if (vinyls.isEmpty()) {
            showInfo("Сначала добавьте пластинку в каталог.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(order == null ? "Новый заказ" : "Редактирование заказа");
        ButtonType saveButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, saveButton);

        ComboBox<User> user = new ComboBox<>(users);
        user.setConverter(userConverter());
        user.getSelectionModel().select(order == null ? users.getFirst() : users.stream()
                .filter(item -> item.getId() == order.getUserId())
                .findFirst()
                .orElse(users.getFirst()));

        ComboBox<Vinyl> vinyl = new ComboBox<>(vinyls);
        vinyl.setConverter(vinylConverter());
        vinyl.getSelectionModel().select(order == null ? vinyls.getFirst() : vinyls.stream()
                .filter(item -> item.getTitle().equalsIgnoreCase(order.getAlbumTitle())
                        && item.getArtist().equalsIgnoreCase(order.getArtist()))
                .findFirst()
                .orElse(vinyls.getFirst()));
        Spinner<Integer> quantity = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, order == null ? 1 : order.getQuantity()));
        quantity.setEditable(true);
        TextField total = new TextField(order == null ? "" : order.getTotalPrice().toPlainString());
        total.setEditable(false);
        Runnable updateTotal = () -> total.setText(vinyl.getValue().getPrice()
                .multiply(BigDecimal.valueOf(quantity.getValue()))
                .toPlainString());
        if (order == null) {
            updateTotal.run();
        }
        vinyl.valueProperty().addListener((observable, oldValue, newValue) -> updateTotal.run());
        quantity.valueProperty().addListener((observable, oldValue, newValue) -> updateTotal.run());
        ComboBox<OrderStatus> status = new ComboBox<>(FXCollections.observableArrayList(OrderStatus.values()));
        status.setConverter(statusConverter());
        status.getSelectionModel().select(order == null ? OrderStatus.NEW : order.getStatus());

        dialog.getDialogPane().setContent(formGrid(
                row("Пользователь", user),
                row("Пластинка", vinyl),
                row("Количество", quantity),
                row("Сумма", total),
                row("Статус", status)
        ));
        keepDialogOpenOnError(dialog, saveButton, "Не удалось сохранить заказ", () -> {
            int enteredQuantity = readSpinnerValue(quantity, 1, 50, "Количество");
            Vinyl selectedVinyl = vinyl.getValue();
            if (selectedVinyl.getStock() < enteredQuantity) {
                throw new IllegalArgumentException("На складе только " + selectedVinyl.getStock() + " шт.");
            }
            BigDecimal totalPrice = selectedVinyl.getPrice().multiply(BigDecimal.valueOf(enteredQuantity));
            if (order == null) {
                orderService.create(user.getValue().getId(), selectedVinyl.getTitle(), selectedVinyl.getArtist(),
                        enteredQuantity, totalPrice, status.getValue());
            } else {
                orderService.update(order.getId(), user.getValue().getId(), selectedVinyl.getTitle(),
                        selectedVinyl.getArtist(), enteredQuantity, totalPrice, status.getValue());
            }
        });
        dialog.showAndWait();
    }

    private void showVinylDialog(Vinyl vinyl) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(vinyl == null ? "Новая пластинка" : "Редактирование пластинки");
        ButtonType saveButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.CANCEL,
                saveButton
        );

        TextField title = new TextField(vinyl == null ? "" : vinyl.getTitle());
        TextField artist = new TextField(vinyl == null ? "" : vinyl.getArtist());
        ObservableList<String> genres = FXCollections.observableArrayList(
                vinyls.stream()
                        .map(Vinyl::getGenre)
                        .filter(value -> value != null && !value.isBlank())
                        .distinct()
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList()
        );
        ComboBox<String> genre = new ComboBox<>(genres);
        genre.setEditable(true);
        genre.setPromptText("Выберите или введите жанр");
        if (vinyl != null) {
            genre.setValue(vinyl.getGenre());
        }
        TextField price = new TextField(vinyl == null ? "" : vinyl.getPrice().toPlainString());
        limitLength(title, 50);
        limitLength(artist, 50);
        limitLength(genre.getEditor(), 30);
        price.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,7}([.,]\\d{0,2})?") ? change : null));
        Spinner<Integer> stock = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                0, 100_000, vinyl == null ? 0 : vinyl.getStock()
        ));
        stock.setEditable(true);

        dialog.getDialogPane().setContent(formGrid(
                row("Название", title),
                row("Исполнитель", artist),
                row("Жанр", genre),
                row("Цена", price),
                row("Остаток", stock)
        ));
        keepDialogOpenOnError(dialog, saveButton, "Не удалось сохранить пластинку", () -> {
            try {
                BigDecimal parsedPrice = new BigDecimal(price.getText().replace(',', '.'));
                int enteredStock = readSpinnerValue(stock, 0, 100_000, "Остаток");
                if (vinyl == null) {
                    vinylService.create(title.getText(), artist.getText(), genre.getEditor().getText(), parsedPrice, enteredStock);
                } else {
                    vinylService.update(vinyl.getId(), title.getText(), artist.getText(), genre.getEditor().getText(), parsedPrice, enteredStock);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Введите цену числом, например 1500.00.");
            }
        });
        dialog.showAndWait();
    }

    private String userTitle(long userId) {
        return users.stream()
                .filter(user -> user.getId() == userId)
                .findFirst()
                .map(user -> user.getFullName() + " (ID " + user.getId() + ")")
                .orElse("ID " + userId);
    }

    private UserFilter userFilterFor(long userId) {
        return userFilterBox.getItems().stream()
                .filter(filter -> filter.userId() != null && filter.userId() == userId)
                .findFirst()
                .orElse(UserFilter.all());
    }

    private boolean contains(String source, String query) {
        return source != null && source.toLowerCase().contains(query);
    }

    private StringConverter<User> userConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(User value) {
                return value == null ? "" : value.getId() + " - " + value.getFullName();
            }

            @Override
            public User fromString(String value) {
                return null;
            }
        };
    }

    private StringConverter<OrderStatus> statusConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(OrderStatus value) {
                return value == null ? "" : value.getTitle();
            }

            @Override
            public OrderStatus fromString(String value) {
                return null;
            }
        };
    }

    private StringConverter<Vinyl> vinylConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Vinyl value) {
                return value == null ? "" : value.getArtist() + " — " + value.getTitle()
                        + " (" + value.getPrice().toPlainString() + " руб., остаток " + value.getStock() + ")";
            }

            @Override
            public Vinyl fromString(String value) {
                return null;
            }
        };
    }

    private GridPane formGrid(FormRow... rows) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));
        for (int i = 0; i < rows.length; i++) {
            FormRow row = rows[i];
            grid.add(row.label(), 0, i);
            grid.add(row.field(), 1, i);
        }
        return grid;
    }

    private FormRow row(String label, Node field) {
        Label title = new Label(label);
        title.setMinWidth(110);
        if (field instanceof Region region) {
            region.setPrefWidth(340);
        }
        return new FormRow(title, field);
    }

    private void keepDialogOpenOnError(
            Dialog<?> dialog,
            ButtonType saveButton,
            String errorTitle,
            ThrowingAction saveAction
    ) {
        Node button = dialog.getDialogPane().lookupButton(saveButton);
        button.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                saveAction.run();
                refreshAll();
            } catch (Exception e) {
                event.consume();
                showError(errorTitle, e);
            }
        });
    }

    private int readSpinnerValue(Spinner<Integer> spinner, int min, int max, String fieldName) {
        try {
            int value = Integer.parseInt(spinner.getEditor().getText().trim());
            if (value < min || value > max) {
                throw new IllegalArgumentException(fieldName + " должно быть от " + min + " до " + max + ".");
            }
            spinner.getValueFactory().setValue(value);
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " должно быть целым числом.");
        }
    }

    private void limitLength(TextField field, int maxLength) {
        field.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= maxLength ? change : null));
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.CANCEL, ButtonType.OK);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String title, Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR, error.getMessage(), ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    private record FormRow(Label label, Node field) {
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }

    private record StatusFilter(OrderStatus status, String title) {
        private static StatusFilter all() {
            return new StatusFilter(null, "Все статусы");
        }

        private static StatusFilter of(OrderStatus status) {
            return new StatusFilter(status, status.getTitle());
        }
    }

    private record UserFilter(Long userId, String title) {
        private static UserFilter all() {
            return new UserFilter(null, "Все пользователи");
        }
    }

    private enum SortMode {
        DATE_DESC("Дата: сначала новые", Comparator.comparing(Order::getCreatedAt).reversed()),
        PRICE_ASC("Сумма: по возрастанию", Comparator.comparing(Order::getTotalPrice)),
        PRICE_DESC("Сумма: по убыванию", Comparator.comparing(Order::getTotalPrice).reversed()),
        QUANTITY_ASC("Количество: по возрастанию", Comparator.comparing(Order::getQuantity)),
        QUANTITY_DESC("Количество: по убыванию", Comparator.comparing(Order::getQuantity).reversed());

        private final String title;
        private final Comparator<Order> comparator;

        SortMode(String title, Comparator<Order> comparator) {
            this.title = title;
            this.comparator = comparator;
        }

        private String title() {
            return title;
        }

        private Comparator<Order> comparator() {
            return comparator;
        }
    }
}
