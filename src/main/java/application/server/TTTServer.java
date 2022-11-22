package application.server;

import java.awt.*;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class TTTServer extends JFrame implements TTTConstants {

  public static void main(String[] args) {
    new TTTServer();
  }

  public TTTServer() {
    //////////////////////////// server gui by swing, or I can simply print log to terminal
    JTextArea jtaLog = new JTextArea();
    JScrollPane scrollPane = new JScrollPane(jtaLog);
    add(scrollPane, BorderLayout.CENTER);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(500, 500);
    setResizable(false);
    setTitle("TTTServer");
    setVisible(true);
    ArrayList<Thread> threadsPool = new ArrayList<>();
    addWindowListener(
        new WindowListener() {
        @Override
        public void windowOpened(java.awt.event.WindowEvent e) {

        }

        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
          for (Thread thread : threadsPool) {
            try {
              thread.interrupt();
            } catch (Exception es) {
              es.printStackTrace();
            }
          }
        }

        @Override
        public void windowClosed(java.awt.event.WindowEvent e) {

        }

        @Override
        public void windowIconified(java.awt.event.WindowEvent e) {

        }

        @Override
        public void windowDeiconified(java.awt.event.WindowEvent e) {

        }

        @Override
        public void windowActivated(java.awt.event.WindowEvent e) {
        }

        @Override
        public void windowDeactivated(java.awt.event.WindowEvent e) {
        }
      }
    );
    ////////////////////////////

    try {
      ServerSocket serverSocket = new ServerSocket(4756); // game socket port

      jtaLog.append(new Date() + ":Server started at socket 4756\n");
      int sessionNo = 1;
      while (true) {
        jtaLog.append(
            new Date() + ":Wait for players to join session " + sessionNo + '\n'
        );
        Socket player1 = serverSocket.accept();
        jtaLog.append(
            new Date() + "Player 1 joined session " + sessionNo + '\n'
        );

        jtaLog.append(
            "Player 1's IP address" +
            player1.getInetAddress().getHostAddress() +
            '\n'
        );

        new DataOutputStream(player1.getOutputStream()).writeInt(PLAY_1);

        if (player1.isClosed()) {
          jtaLog.append("Player 1 lost connection" + '\n');
          continue;
        }
        Socket player2 = serverSocket.accept();
        jtaLog.append(
            new Date() + ":Player 2 joined session " + sessionNo + '\n'
        );
        jtaLog.append(
            "Player 2's IP address " +
            player2.getInetAddress().getHostAddress() +
            '\n'
        );
        new DataOutputStream(player2.getOutputStream()).writeInt(PLAY_2);
        jtaLog.append(
            new Date() + ":Start a thread for session " + sessionNo + '\n'
        );
        Thread sessionThread = new Thread(new MatchSession(player1, player2));
        sessionThread.start();
        threadsPool.add(sessionThread);
        // if (sessionThread.isAlive()) {
        //   System.out.println("session is running.");
        // }
        sessionNo++;
      }
    } catch (IOException ex) {
      System.err.println(ex);
    }
  }
}

class MatchSession implements Runnable, TTTConstants { // match running session

  private Socket player1;
  private Socket player2;
  private DataInputStream fromPlayer1;
  private DataOutputStream toPlayer1;
  private DataInputStream fromPlayer2;
  private DataOutputStream toPlayer2;

  private static final int[][] chessBoard = new int[3][3];

  // private static final boolean[][] flag = new boolean[3][3];

  public MatchSession(Socket player1, Socket player2) {
    this.player1 = player1;
    this.player2 = player2;
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        chessBoard[i][j] =
          -1;
      }
    }
  }

  public void run() {
    try {
      fromPlayer1 = new DataInputStream(player1.getInputStream());
      toPlayer1 = new DataOutputStream(player1.getOutputStream());
      fromPlayer2 = new DataInputStream(player2.getInputStream());
      toPlayer2 = new DataOutputStream(player2.getOutputStream());
      // 22 means match ready
      toPlayer1.writeInt(MATCH_START);
      toPlayer2.writeInt(MATCH_START);
      // Thread.sleep(100);

      // todo: socket connection check, should not be here
      if (!player1.isClosed() && !player2.isClosed()) {
        System.out.println("match started");
        // send token (is player as player1 or player2)
        toPlayer1.writeInt(PLAY_1);
        toPlayer2.writeInt(PLAY_2);
      } else {
        // send token (is player as player1 or player2) ANYWAY
        toPlayer1.writeInt(PLAY_1);
        toPlayer2.writeInt(PLAY_2);
        toPlayer1.writeInt(DRAW);
        toPlayer2.writeInt(DRAW);
        System.out.println("Match closed");
        return;
      }

      while (true) {
        // if (player1.isClosed() || player2.isClosed()) {
        //   toPlayer1.writeInt(DRAW);
        //   toPlayer2.writeInt(DRAW);
        //   break;
        // }

        if (player1.isClosed()) {
          // send token (is player as player1 or player2) ANYWAY
          toPlayer2.writeInt(CONNECT_LOSE);
          System.out.println("Player1 disconnect, Match closed");
          return;
        } else if (player2.isClosed()) {
          toPlayer1.writeInt(CONNECT_LOSE);
          System.out.println("Player2 disconnect, Match closed");
          return;
        }

        // step 1: receive from player1
        int row = fromPlayer1.readInt();
        int column = fromPlayer1.readInt();
        System.out.print("Player1 take ");
        int[] out = { row, column };
        System.out.println(Arrays.toString(out));
        chessBoard[row][column] = PLAY_1;

        // step2: check if player1 is win or draw match
        if (isWon(PLAY_1)) {
          toPlayer1.writeInt(PLAYER1_WON);
          toPlayer2.writeInt(PLAYER1_WON);
          // always send player1 movement to player2
          toPlayer2.writeInt(row);
          toPlayer2.writeInt(column);
          // sendMove(toPlayer2, row, column);
          System.out.print("Player1 win! ");
          return;
        } else if (isFull()) {
          toPlayer1.writeInt(DRAW);
          toPlayer2.writeInt(DRAW);
          // always send player1 movement to player2
          toPlayer2.writeInt(row);
          toPlayer2.writeInt(column);
          // sendMove(toPlayer2, row, column);
          System.out.print("Draw! ");
          return;
        } else {
          toPlayer2.writeInt(CONTINUE);
          System.out.println(CONTINUE);
          // if match continue send player1 movement to player2
          toPlayer2.writeInt(row);
          System.out.print(row);

          toPlayer2.writeInt(column);
          System.out.println(column);
          // sendMove(toPlayer2, row, column);
        }

        if (player1.isClosed()) {
          // send token (is player as player1 or player2) ANYWAY
          toPlayer2.writeInt(CONNECT_LOSE);
          System.out.println("Player1 disconnect, Match closed");
          return;
        } else if (player2.isClosed()) {
          toPlayer1.writeInt(CONNECT_LOSE);
          System.out.println("Player2 disconnect, Match closed");
          return;
        }

        // step 3: receive from player2
        row = fromPlayer2.readInt();
        column = fromPlayer2.readInt();
        System.out.print("Player2 take ");
        int[] out1 = { row, column };
        System.out.println(Arrays.toString(out1));
        if (chessBoard[row][column] == -1) {
          chessBoard[row][column] = PLAY_2;
        } else {}
        // step2: check if player2 is win or draw match
        if (isWon(PLAY_2)) {
          toPlayer1.writeInt(PLAYER2_WON);
          toPlayer2.writeInt(PLAYER2_WON);
          // always send player2 movement to player1
          // sendMove(toPlayer1, row, column);
          toPlayer1.writeInt(row);
          toPlayer1.writeInt(column);
          System.out.print("Player2 win! ");
          return;
        } else if (isFull()) {
          toPlayer1.writeInt(DRAW);
          toPlayer2.writeInt(DRAW);
          // always send player2 movement to player1
          // sendMove(toPlayer1, row, column);
          toPlayer1.writeInt(row);
          toPlayer1.writeInt(column);
          System.out.print("Draw! ");
          return;
        } else {
          toPlayer1.writeInt(CONTINUE);
          System.out.println(CONTINUE);

          // if match continue send player2 movement to player1
          // sendMove(toPlayer1, row, column);
          toPlayer1.writeInt(row);
          System.out.print(row);

          toPlayer1.writeInt(column);
          System.out.println(column);
        }
        // step 5: do these again
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      if (!player1.isClosed()) {
        try {
          toPlayer1.writeInt(SERVER_CLOSE);
          player1.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (!player2.isClosed()) {
        try {
          toPlayer2.writeInt(SERVER_CLOSE);
          player2.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      // try {
      //   toPlayer1.writeInt(SERVER_CLOSE);
      //   toPlayer2.writeInt(SERVER_CLOSE);
      //   player1.close();
      //   player2.close();
      // } catch (IOException e) {
      //   e.printStackTrace();
      // }
    }
  }

  private boolean isFull() { // panel full check
    for (int i = 0; i < 2; i++) {
      {
        for (int j = 0; j < 2; j++) {
          {
            if (chessBoard[i][j] == -1) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  private boolean isWon(int token) { // win check
    for (int i = 0; i < 2; i++) {
      {
        if ((chessBoard[i][0] == token) && (chessBoard[i][1] == token)
            && (chessBoard[i][2] == token)) {
          return true;
        }
      }
    }
    for (int i = 0; i < 2; i++) {
      {
        if ((chessBoard[0][i] == token) && (chessBoard[1][i] == token)
            && (chessBoard[2][i] == token)) {
          return true;
        }
      }
    }
    if (
        (chessBoard[0][0] == token) 
        &&
        (chessBoard[1][1] == token) 
        &&
        (chessBoard[2][2] == token)
    ) {
      return true;
    }
    if (
        (chessBoard[0][2] == token) 
        &&
        (chessBoard[1][1] == token) 
        &&
        (chessBoard[2][0] == token)
    ) {
      return true;
    }
    return false;
  }
}
