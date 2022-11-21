package application.client;

// import application.controller.Controller;
import application.server.TTTConstants;
import java.io.*;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class PlayerClient implements TTTConstants {

  private Stage primaryStage;
  // private Scene scene;

  // private boolean isMatched = false;

  public void initPlayer(Stage stage) throws IOException {
    primaryStage = stage;
    FXMLLoader fxmlLoader = new FXMLLoader();
    fxmlLoader.setLocation(
      getClass().getClassLoader().getResource("mainUI.fxml")
    );
    Pane roots = fxmlLoader.load();
    // scene = new Scene(root);
    primaryStage.setTitle("Tic Tac Toe");
    // primaryStage.setScene(scene);
    primaryStage.setScene(new Scene(roots));
    primaryStage.setResizable(false);

    // 为什么stage上没有东西并且会卡住?
    primaryStage.show();
  }
}
