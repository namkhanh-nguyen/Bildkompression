package arithmetic_coding;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Objects;

public class ArithmeticEncodingApp extends Application
{

    @Override
    public void start(Stage primaryStage)
        throws Exception
    {
        BorderPane root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ArithmeticEncodingAppView.fxml")));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Arithmetic Encoding Demo");
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}

