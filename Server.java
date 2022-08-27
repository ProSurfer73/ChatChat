import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.Color;
import java.io.PrintWriter;

public class Server implements Runnable
{
  private int port;
  private ServerSocket server;
  private List<User> clients;

  public static void main(String[] args) throws IOException {
    new Server(1324).run();
  }

  public Server(int port)
  {
    this.port = port;
    this.clients = new ArrayList<User>();
  }
  
  public int getPort()
  {
	  return this.port;
  }

  public void run()
  {
	  
	try {
	  
    server = new ServerSocket(port) {
		// TO DO: this function is deprecated
      protected void finalize() throws IOException {
        this.close();
      }
      
    };
    System.out.println("new server");

    while (true) {
      // accepts a new client
      Socket client = server.accept();
      
      try {

      // get name of the new user
      String name = (new Scanner ( client.getInputStream() )).nextLine();
      name = name.replace(" ", "_");
      name = name.replace(",", "");
      
      // create new User
      User newUser = new User(client, name);

      // add newUser message to list
      this.clients.add(newUser);
          
      System.out.println("New user connected to the server ! :)");

      // create a new thread for newUser incoming messages handling
      new Thread(new UserHandler(this, newUser)).start();
      
	  } catch(Throwable e) {
		  System.out.println("Erreur nouvel utilisateur: "+e.getMessage());
	  }
      
    }
    
	} catch(IOException e){}
  }

  // delete a user from the list
  public void removeUser(User user)
  {
    this.clients.remove(user);
  }

  // send incoming msg to all Users
  public void broadcastMessages(String msg, User userSender)
  {
    for (User client : this.clients) {
      client.getOutStream().println(
          userSender.getName() + ": " + msg);
    }
  }

  // send list of clients to all Users
  public void broadcastAllUsers()
  {
    for (User client : this.clients) {
      client.getOutStream().println(this.clients);
    }
  }

  // send message to a User (String)
  public void sendMessageToUser(String msg, User userSender, String user)
  {
    for (User client : this.clients) {
      if (client.getName().equals(user))
      {
        userSender.getOutStream().println(userSender.toString() + " -> " + client.toString() +": " + msg);
        if(user != userSender.getName())
			client.getOutStream().println("(Prive)" + userSender.toString() + ": " + msg);
      }
    }
  }
}

class UserHandler implements Runnable
{
  private Server server;
  private User user;

  public UserHandler(Server server, User user)
  {
    this.server = server;
    this.user = user; 
    this.server.broadcastAllUsers();
  }

  public void run()
  {
    String message;


    Scanner sc = new Scanner(this.user.getInputStream());
    
    while (sc.hasNextLine())
    {
      message = sc.nextLine();

      // Private message management
      if (message.charAt(0) == '@')
      {
        if(message.contains(" ")){
          System.out.println("msg privee : " + message);
          int firstSpace = message.indexOf(" ");
          String userPrivate= message.substring(1, firstSpace);
          server.sendMessageToUser(
              message.substring(
                firstSpace+1, message.length()
                ), user, userPrivate
              );
        }


      } else {
        // send the message to all users
        server.broadcastMessages(message, user);
      }
    }
    // end of Thre
    server.removeUser(user);
    this.server.broadcastAllUsers();
    sc.close();
  }
}

class User
{
  private PrintStream streamOut;
  private InputStream streamIn;
  private String name;
  private Socket client;

  // constructor
  public User(Socket client, String nickname) throws IOException
  {
    this.streamOut = new PrintStream(client.getOutputStream());
    this.streamIn = client.getInputStream;
    this.client = client;
    this.name = nickname;
  }

  // getteur
  public PrintStream getOutStream(){
    return this.streamOut;
  }

  public InputStream getInputStream(){
    return this.streamIn;
  }

  public String getName(){
    return this.name;
  }
  
  // useful when printing array of users
  public String toString(){
	  return this.name;
  }
  
  
  
}
