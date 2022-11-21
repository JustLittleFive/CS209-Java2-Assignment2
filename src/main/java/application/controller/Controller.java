package application.controller;

import com.gluonhq.charm.glisten.control.AppBar;
import java.io.*;
import java.net.*;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
// import javafx.fxml.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Controller implements Initializable {

  private static final int PLAY_1 = 1;
  private static final int PLAY_2 = 2;
  public static final int PLAYER1_WON = 10;
  public static final int PLAYER2_WON = 20;
  public static final int DRAW = 3;
  public static final int CONTINUE = 4;
  private static final int EMPTY = 0;
  private static final int BOUND = 90;
  private static final int OFFSET = 15;
  private static final int MATCH_START = 22;

  private boolean myTurn = false;
  private int myToken = 0;
  private int playerNum = 0;
  private DataInputStream fromServer;
  private DataOutputStream toServer;
  private boolean continueToPlay = true;
  private String host = "localhost";

  // private Stage statusBar;

  @FXML
  private AppBar StatusBar;

  @FXML
  private Pane base_square;

  @FXML
  public Rectangle game_panel;

  // private static boolean TURN = false; // true: player2, false: player1
  public boolean TURN = true; // true(1): player1, false(0): player2

  private static int[][] chessBoard = new int[3][3];
  private static boolean[][] flag = new boolean[3][3];
  private static int[] toSend = new int[2];

  // public static int[] action = new int[2];
  // public static boolean waiting = true;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // stage1 is the "wait for match making" window
    Stage stage1 = new Stage();
    stage1.initModality(Modality.APPLICATION_MODAL);
    Button btn = new Button("结束匹配");
    btn.setOnMouseClicked(event1 -> {
      stage1.close();
    });
    VBox vBox = new VBox();
    vBox.getChildren().addAll(btn);
    vBox.setAlignment(Pos.CENTER);
    // hint: this vbox cannot show up
    // which means stage1 is a empty window with title "匹配中"
    Scene scene = new Scene(vBox, 600, 200);
    stage1.setScene(scene);
    stage1.setTitle("匹配中...");
    stage1.show();

    connectToServer();

    while (true) {
      // received 22 as match start sign
      try {
        if (fromServer.readInt() == MATCH_START) {
          break;
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    stage1.close();

    toSend[0] = -1;
    toSend[1] = -1;

    for (int i = 0; i < 2; i++) for (int j = 0; j < 2; j++) {
      chessBoard[i][j] = EMPTY;
      flag[i][j] = false;
    }

    game_panel.setOnMouseClicked(e -> {
      if (myTurn) {
        int x = (int) (e.getX() / 90);
        int y = (int) (e.getY() / 90);
        if (refreshBoard(x, y)) {} else {
          System.err.println("Data not sync!");
          System.exit(-1);
        }
        toSend[0] = x;
        toSend[1] = y;
      }
    });

    // NOT WORK: javafx doesn't allow any other thread draw GUI.
    Thread runningMatch = new Thread(new Controller.MatchThread());
    runningMatch.start();
  }

  // connect socket, open input and output stream
  public void connectToServer() {
    try {
      Socket socket = new Socket(host, 4756);
      fromServer = new DataInputStream(socket.getInputStream());
      toServer = new DataOutputStream(socket.getOutputStream());
    } catch (Exception ex) {
      System.err.println(ex);
    }
  }

  // public void refreshStatus(String status){
  //   // StatusBar.setTitleText(status);
  //   Platform.runLater(() -> {
  //     StatusBar.setTitleText(status);
  //   });
  // }

  public void refreshBoard(String status) {
    Platform.runLater(() -> {
      StatusBar.setTitleText(status);
    });
  }

  public boolean refreshBoard(int x, int y) {
    
    if (chessBoard[x][y] == EMPTY) {
      chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
      System.out.println(chessBoard[x][y]);
      Platform.runLater(() -> {
        drawChess();
      });
      return true;
    }
    return false;
  }

  private void drawChess() {
    for (int i = 0; i < chessBoard.length; i++) {
      for (int j = 0; j < chessBoard[0].length; j++) {
        if (flag[i][j]) {
          // This square has been drawing, ignore.
          continue;
        }
        switch (chessBoard[i][j]) {
          case PLAY_1:
            System.out.print("Drawing: Circle ");
            System.out.println(i + j);
            drawCircle(i, j);
            break;
          case PLAY_2:
            System.out.print("Drawing: Cross ");
            System.out.println(i + j);
            drawLine(i, j);
            break;
          case EMPTY:
            System.out.print("Drawing: Nothing ");
            System.out.println(i + j);
            // do nothing
            break;
          default:
            System.err.println("Invalid value!");
            System.exit(-1);
        }
      }
    }
  }

  private void drawCircle(int i, int j) {
    Circle circle = new Circle();
    base_square.getChildren().add(circle);
    circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
    circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
    circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
    circle.setStroke(Color.RED);
    circle.setFill(Color.TRANSPARENT);
    flag[i][j] = true;
  }

  private void drawLine(int i, int j) {
    Line line_a = new Line();
    Line line_b = new Line();
    base_square.getChildren().add(line_a);
    base_square.getChildren().add(line_b);
    line_a.setStartX(i * BOUND + OFFSET * 1.5);
    line_a.setStartY(j * BOUND + OFFSET * 1.5);
    line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
    line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
    line_a.setStroke(Color.BLUE);

    line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
    line_b.setStartY(j * BOUND + OFFSET * 1.5);
    line_b.setEndX(i * BOUND + OFFSET * 1.5);
    line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
    line_b.setStroke(Color.BLUE);
    flag[i][j] = true;
  }

  class MatchThread implements Runnable {

    // private static final boolean[][] flag = new boolean[3][3];

    public MatchThread() {}

    // core
    @Override
    public void run() {
      System.out.println("Game set!");
      // primaryStage.show();
      try {
        // receive token
        playerNum = fromServer.readInt();
        if (playerNum == PLAY_1) {
          myToken = PLAY_1;
          TURN = true;
          myTurn = true;
        } else if (playerNum == PLAY_2) {
          myToken = PLAY_2;
          TURN = true;
          myTurn = false;
        } else {
          System.out.println("Miss match!");
          return;
        }
        System.out.println("Got my token.");

        // start match
        while (continueToPlay) {
          System.out.print("my token is: ");
          System.out.println(myToken);

          if (myToken == PLAY_1) {
            // waitForPlayerAction();
            System.out.println("I'm player1. ");
            // StatusBar.setTitleText("I'm player1. ");
            refreshBoard("I'm player1. ");
            sendMove();
            receiveInfoFromServer();
          } else if (myToken == PLAY_2) {
            System.out.println("I'm player2. ");
            // StatusBar.setTitleText("I'm player2. ");
            refreshBoard("I'm player2. ");
            receiveInfoFromServer();
            sendMove();
          }
        }
      } catch (Exception ex) {}
    }

    private void sendMove() throws IOException, InterruptedException {
      if (myTurn) {
        System.out.println("Ready to send movement.");
        // stage.show() stuck here.
        // which means, log print txt above, but no GUI display
        try {
          while (toSend[0] == -1 || toSend[1] == -1) {
            Thread.sleep(100);
          }
          toServer.writeInt(toSend[0]);
          toServer.writeInt(toSend[1]);
          System.out.println("Movement sent.");
          refreshBoard("Waiting opponent");
          toSend[0] = -1;
          toSend[1] = -1;
        } catch (IOException e1) {
          e1.printStackTrace();
        }
        myTurn = false;
        TURN = !TURN;
      } else {
        System.err.println("Not your turn!");
      }
    }

    private void receiveInfoFromServer() throws IOException {
      int status = fromServer.readInt();

      System.out.print("Received status: ");
      System.out.println(status);

      if (status == PLAYER1_WON) {
        continueToPlay = false;
        if (myToken == PLAY_1) {
          System.out.println("You won!");
          // StatusBar.setTitleText("You won!");
          refreshBoard("You won!");
        } else if (myToken == PLAY_2) {
          System.out.println("Opponent won!");
          // StatusBar.setTitleText("Opponent won!");
          refreshBoard("Opponent won!");
          receiveMove();
        }
      } else if (status == PLAYER2_WON) {
        continueToPlay = false;
        if (myToken == PLAY_2) {
          System.out.println("You won!");
          // StatusBar.setTitleText("You won!");
          refreshBoard("You won!");
        } else if (myToken == PLAY_1) {
          System.out.println("Opponent won!");
          // StatusBar.setTitleText("Opponent won!");
          refreshBoard("Opponent won!");
          receiveMove();
        }
      } else if (status == DRAW) {
        continueToPlay = false;
        System.out.println("Draw match!");
        // StatusBar.setTitleText("Draw match!");
        refreshBoard("Draw match!");
        if (myToken == PLAY_2) receiveMove();
        // primaryStage.show();
      } else {
        continueToPlay = true;
        System.out.print("Is my turn? ");
        System.out.println(myTurn);
        if (!myTurn) {
          System.out.println("It's my Turn");
          // StatusBar.setTitleText("It's my Turn");
          refreshBoard("It's my Turn");
          receiveMove();
          myTurn = true;
          TURN = !TURN;
          // primaryStage.showAndWait();
        }
      }
    }

    private void receiveMove() throws IOException {
      int row = fromServer.readInt();
      System.out.print(row);
      int column = fromServer.readInt();
      System.out.print(column);
      while (!refreshBoard(row, column)) {
        System.err.println("Data not sync!");
      }
      System.out.println("Reveived movement.");
      System.out.print(row);
      System.out.println(column);
      // cell[row][column].setToken(otherToken);
      // }
    }
  }
}
