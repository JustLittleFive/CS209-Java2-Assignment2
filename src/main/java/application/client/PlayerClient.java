package application.client;

import application.controller.Controller;
import application.server.TTTConstants;
import java.io.*;
import javafx.event.EventHandler;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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

    primaryStage.setOnCloseRequest(
        new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
          try {
            Controller.runningMatch.interrupt();
            Controller.socket.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
          System.out.print("监听到窗口关闭");
        }
      }
    );

    primaryStage.show();
  }
}
