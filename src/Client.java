import java.net.*;
import java.io.*;

public class Client {
    // Initializing input and output streams
    public static DataOutputStream outputStream = null;
    public static DataInputStream inputStream = null;
    public static void main(String[] args)  {
        try {
            // Initialize portNumber
            int portNumber;
            if(args.length > 0){
                portNumber = Integer.parseInt(args[0]);
            }
            else{
                portNumber = 5106;
            }
            // Initializing client socket
            Socket socketClient = new Socket("localhost",portNumber);
            System.out.println("Client Started");

            inputStream = new DataInputStream(socketClient.getInputStream());
            outputStream = new DataOutputStream(socketClient.getOutputStream());

            BufferedReader commandLineReader = new BufferedReader(new InputStreamReader(System.in));
            String commandLineInput;

            System.out.println("Please enter get/upload commands!!");
            System.out.println("Press Enter/ Type 'terminate' to close the client!!");
            // Keep listing to requests from terminal until null.
            while ((commandLineInput = commandLineReader.readLine()) != null) {
                String[] commandLineArray = commandLineInput.trim().split("\\s+");
                if(commandLineArray[0].equals("get")){
                    outputStream.writeUTF("sendFile " + commandLineArray[1]);
                    String fileName = "new" + commandLineArray[1].substring(0, 1).toUpperCase() + commandLineArray[1].substring(1);
                    downloadFile(fileName);
                    System.out.println(fileName + " file downloaded");
                }
                else if(commandLineArray[0].equals("upload")){
                    outputStream.writeUTF("receiveFile " + commandLineArray[1]);
                    uploadFile(commandLineArray[1]);
                    String fileName = "new" + commandLineArray[1].substring(0, 1).toUpperCase() + commandLineArray[1].substring(1);
                    System.out.println(commandLineArray[1] + " file uploaded");
                }
                else if(commandLineArray[0].equals("terminate")){
                    outputStream.writeUTF(commandLineArray[0]);
                    break;
                }
                else {
                    System.out.println("Invalid Command!! connection Terminating");
                    outputStream.writeUTF("terminate");
                    break;
                }
                outputStream.flush();
            }
            outputStream.close();
            inputStream.close();
            socketClient.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void downloadFile(String fileName){
        try{
            int bytes = 0;
            FileOutputStream fileOutput = new FileOutputStream(fileName);
            long fileSize = inputStream.readLong();
            // Divide file into chunks of 1K
            byte[] buffer = new byte[1024];
            while (fileSize > 0 && (bytes = inputStream.read(buffer, 0, (int)Math.min(buffer.length, fileSize))) != -1) {
                fileOutput.write(buffer, 0, bytes);
                fileSize -= bytes;
            }
            fileOutput.close();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public static void uploadFile(String fileName){
        String currentDirectory = System.getProperty("user.dir");
        String filePath = currentDirectory + "/" + fileName;
        try{
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
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}