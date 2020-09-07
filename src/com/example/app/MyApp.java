package com.example.app;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

import org.json.JSONObject;


public class MyApp {
  public static int C_USER_INPUT_PORT;
  public static int C_USER_LISTENING_PORT;
  public static int S_USER_INPUT_PORT;
  public static int S_USER_LISTENING_PORT;

  public static void main(String args[]) throws Exception{

    //need to change this to json file!!
    //in turn will change ports and allow for many users at once
    //add loop to send to many users at once
    //maybe add mutex
    //address edge cases of ports that can't connect

    JSONObject jsonObject = new JSONObject();

    if(args[0].equals("one")){
      C_USER_INPUT_PORT = 10000;
      C_USER_LISTENING_PORT = 10001;
      S_USER_INPUT_PORT = 20000;
      S_USER_LISTENING_PORT = 20001;
      
    }else{
      C_USER_INPUT_PORT = 20000;
      C_USER_LISTENING_PORT = 20001;
      S_USER_INPUT_PORT = 10000;
      S_USER_LISTENING_PORT = 10001;
    }


    Scanner scanner= new Scanner(System.in);

    handleCtrlC();

    handleUserInput(scanner);

    handleReceiverMessagesAndRespond();
  
  }

  public static void handleUserInput(Scanner scanner) throws Exception {
  
    Runnable waitForUserInput = () -> { 
      while(true){
        if(scanner.hasNextLine()){  

          String input= scanner.nextLine();

          if(input.equals("quit")){
            scanner.close();
            System.exit(0);
          }  

          System.out.println("You entered: \""+input);

          try{
            DatagramSocket datagramSocket = new DatagramSocket(C_USER_INPUT_PORT); //maybe do last port number send from
            InetAddress inetAddress = InetAddress.getByName("localhost"); //change to json value

            int portNumber = S_USER_LISTENING_PORT; //change to json value
            DatagramPacket datagramSendingPacket = new DatagramPacket(input.getBytes(),
              input.getBytes().length, inetAddress, portNumber);
            datagramSocket.send(datagramSendingPacket);

            DatagramPacket datagramReceivingPacket = new DatagramPacket(input.getBytes(),
              input.getBytes().length);

            try{
              datagramSocket.setSoTimeout(2000);  
              datagramSocket.receive(datagramReceivingPacket);
  
              //put received into array and loop over aphabetically
              String received = new String(datagramReceivingPacket.getData(), 0, 
                datagramReceivingPacket.getLength());
              
              System.out.println("Response received: " + received);
            }
            catch(SocketTimeoutException e){
              continue;
            }
            finally{
              datagramSocket.close();
              datagramSocket.setSoTimeout(0);
            }

          }
          catch(Exception e){
            //not sure
          }
        }
      }
    };

    Thread userInputThread = new Thread(waitForUserInput);
    userInputThread.start();
  }


  static void handleReceiverMessagesAndRespond(){
    Runnable waitAndSendEcho = () -> { 
      try{
        InetAddress inetAddress = InetAddress.getByName("localhost"); //change to json value
        int portNumber = C_USER_LISTENING_PORT; //based on json file
        DatagramSocket datagramSocket = new DatagramSocket(portNumber); //maybe do last port number send from
      
        while(true){
  
          byte[] requestMessageBytes = new byte[1024];
          DatagramPacket datagramRequestPacket = new DatagramPacket(requestMessageBytes,
            requestMessageBytes.length);
          
          datagramSocket.receive(datagramRequestPacket);

          String requestMessage = new String(datagramRequestPacket.getData(), 0, 
          datagramRequestPacket.getLength());

          System.out.println(String.valueOf("Request received: " + requestMessage));

          String respondMessage = requestMessage;
          DatagramPacket datagramRespondPacket = new DatagramPacket(respondMessage.getBytes(),
            respondMessage.getBytes().length, inetAddress,
            datagramRequestPacket.getPort());
          
          datagramSocket.send(datagramRespondPacket);

        }
      }
      catch(Exception e){
        //do nothing
      }
    };
    
    Thread thread = new Thread(waitAndSendEcho);
    thread.start();
  }


  public static void handleCtrlC(){
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
          try {
            Thread.sleep(200);
            System.out.println("Shutting down ...");
          } 
          catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
          }
      }
    });
  }

}