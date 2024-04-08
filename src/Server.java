import java.net.*;
import java.io.*;

public class Server {
    public static void main(String[] args) {
        try {
            // Initializing server socket
            ServerSocket socketServer = new ServerSocket(5106);
            System.out.println("Server Started");
            int clientNumber = 1;
            try {
                while (true) {
                    new threadHandler(socketServer.accept(), clientNumber).start();
                    System.out.println("Client " + clientNumber + " is connected!");
                    clientNumber++;
                }
            } finally {
                socketServer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static class threadHandler extends Thread {
        public Socket clientConnections;
        public int number; //The index number of the client
        // Initializing input and output streams
        public DataOutputStream outputStream = null;
        public DataInputStream inputStream = null;
        public threadHandler(Socket connection, int number) {
            try {
                this.clientConnections = connection;
                this.number = number;
                outputStream = new DataOutputStream(connection.getOutputStream());
                inputStream = new DataInputStream(connection.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                String clientMessages;
                while (!(clientMessages = (String) inputStream.readUTF()).equals("terminate")) {
                    System.out.println("hero " + clientMessages);
                    String[] messages = clientMessages.split(" ");
                    if (messages[0].equals("receiveFile")) {
                        String fileName = messages[1];
                        String finalFileName = "new" + fileName.substring(0, 1).toUpperCase() + fileName.substring(1);
                        receiveFile(finalFileName);
                        System.out.println(finalFileName + " file uploaded from client " + number);
                    } else {
                        String fileName = messages[1];
                        boolean success = sendFile(fileName);
                        if (success) {
                            System.out.println(fileName + " file sent to client " + number);
                        } else {
                            System.out.println("Ran into an error!! retry again");
                        }
                    }
                }
                System.out.println("Server closing down!!!");
                clientConnections.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void receiveFile(String fileName) {
            try {
                int bytes = 0;
                FileOutputStream fileOutput = new FileOutputStream(fileName);
                // read file size that is being received
                long fileSize = inputStream.readLong();
                // Divide file into chunks of 1K
                byte[] buffer = new byte[1024];
                while (fileSize > 0 && (bytes = inputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                    fileOutput.write(buffer, 0, bytes);
                    fileSize -= bytes;
                }
                fileOutput.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        public Boolean sendFile(String fileName) {
            String currentDirectory = System.getProperty("user.dir");
            String filePath = currentDirectory + "/" + fileName;
            try {
                int bytes = 0;
                File fileData = new File(filePath);
                FileInputStream fileInput = new FileInputStream(fileData);
                outputStream.writeLong(fileData.length());
                // Divide file into chunks of 1K
                byte[] buffer = new byte[1024];
                while ((bytes = fileInput.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytes);
                    outputStream.flush();
                }
                fileInput.close();
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }
}
