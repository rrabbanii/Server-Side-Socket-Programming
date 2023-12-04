import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

  public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
  private Socket socket;
  private BufferedReader bufferedReader;
  private BufferedWriter bufferedWriter;
  private String clientUsername;

  public ClientHandler(Socket socket) {
    try {
      this.socket = socket;
      this.bufferedWriter =
        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.bufferedReader =
        new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.clientUsername = bufferedReader.readLine();
      clientHandlers.add(this);
    } catch (IOException e) {
      closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  //@override
  public void run() {
    String messageFromClient;
    while (socket.isConnected()) {
      try {
        messageFromClient = bufferedReader.readLine();
        directMessage(messageFromClient);
      } catch (IOException e) {
        closeEverything(socket, bufferedReader, bufferedWriter);
        break;
      }
    }
  }

  public void directMessage(String messageToSend) {
    for (ClientHandler clientHandler : clientHandlers) {
      System.out.println(clientHandler.clientUsername);
      try {
        if (
          messageToSend.contains(clientHandler.clientUsername) &&
          !clientHandler.clientUsername.equals(this.clientUsername)
        ) {
          clientHandler.bufferedWriter.write(
            this.clientUsername + ": " + messageToSend
          );
          clientHandler.bufferedWriter.newLine();
          clientHandler.bufferedWriter.flush();
        }
      } catch (IOException e) {
        closeEverything(socket, bufferedReader, bufferedWriter);
      }
    }
  }

  public void removeClientHandler() {
    clientHandlers.remove(this);
    directMessage("SERVER: " + clientUsername + " has left the chat");
  }

  public void closeEverything(
    Socket socket,
    BufferedReader bufferedreader,
    BufferedWriter bufferedWriter
  ) {
    removeClientHandler();
    try {
      if (bufferedReader != null) {
        bufferedReader.close();
      }
      if (bufferedWriter != null) {
        bufferedWriter.close();
      }
      if (socket != null) {
        socket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
