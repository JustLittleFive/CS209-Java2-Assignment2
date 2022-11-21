package application;

import java.io.IOException;
// import java.io.*;
// import java.net.*;
import application.client.PlayerClient;
import javafx.application.Application;
// import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
// import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage){
    try {

      Stage stage = new Stage();
      stage.initModality(Modality.APPLICATION_MODAL);

      Button btn = new Button("开始匹配");
      btn.setOnMouseClicked(event -> {
        stage.close();
        PlayerClient player = new PlayerClient();
        try {
          player.initPlayer(primaryStage);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      });
      Button btn1 = new Button("结束游戏");
      btn1.setOnMouseClicked(event -> {
        stage.close();
      });

      VBox vBox = new VBox();
      vBox.getChildren().addAll(btn, btn1);
      vBox.setAlignment(Pos.CENTER);
      Scene scene = new Scene(vBox, 600, 200);
      stage.setScene(scene);
      stage.setTitle("五子棋在线匹配");
      stage.showAndWait();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

}
