package ru.mirea.project.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.mirea.project.db.DatabaseInitializer;
import ru.mirea.project.repository.OrderRepository;
import ru.mirea.project.repository.UserRepository;
import ru.mirea.project.repository.VinylRepository;
import ru.mirea.project.service.ExportService;
import ru.mirea.project.service.OrderService;
import ru.mirea.project.service.UserService;
import ru.mirea.project.service.VinylService;

public class VinylStoreFxApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseInitializer.initialize();

        UserRepository userRepository = new UserRepository();
        OrderRepository orderRepository = new OrderRepository();
        UserService userService = new UserService(userRepository);
        OrderService orderService = new OrderService(orderRepository, userRepository);
        ExportService exportService = new ExportService();
        VinylService vinylService = new VinylService(new VinylRepository());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/mirea/project/fx/main-view.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();
        controller.configure(userService, orderService, vinylService, exportService);

        Scene scene = new Scene(root, 1180, 760);
        scene.getStylesheets().add(getClass().getResource("/ru/mirea/project/fx/app.css").toExternalForm());

        stage.setTitle("Vinyl Store Hub");
        stage.setMinWidth(1020);
        stage.setMinHeight(660);
        stage.setScene(scene);
        stage.show();
    }
}
