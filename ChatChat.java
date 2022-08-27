import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.PrintStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;


////
// Main Class


public class ChatChat implements Runnable, ActionListener, ComponentListener, KeyListener
{
	private JFrame frame;
	private JMenuBar menuBar;
	private JMenu connectMenu;
	private JMenu hebergeMenu;
	private JMenu helpMenu;
	private JMenuItem newHMenuItem;
	private JMenuItem stopHMenuItem;
	private JMenuItem listeHMenuItem;
	private JMenuItem newConnectMenuItem;
	private JMenuItem disconnectMenuItem;
	private JMenuItem aideMenuItem;

	private JButton bouton;
	private JTextArea textArea;
	private JTextField textField;
	private JScrollPane textScroll;
	private JPanel container;

	private Socket socketUser;
	private ArrayList<Server> servers;
	private PrintStream userOutput;
	
	private final String aideString = "Bienvenu dans l'aide.\nVoici l'ensemble des commandes disponibles:\n- /?\n=> demander l'aide\n- /connect *ipadress* *port , then type yout username*\n=> se connecter a une adresse ip\nex: /connect 197.172.0.1 21\n- /disconect\n=> se d\u00E9connecter\n- /clear or /clearscreen\n=> efface l'\u00E9cran\n- /startserver *port*\n=> lance un server\nex: /startserver 3241\n- /stopserver *port*\n=> arrete le serveur du port donn\u00E9\n- /getip\n=> donne l'adresse ip locale de l'ordinateur\n---";
	

	public static void main(String[] args)
	{
	// only useful on osx: use the system bar
	System.setProperty("apple.laf.useScreenMenuBar", "true");

	// the proper way to show a jframe (invokeLater)
	SwingUtilities.invokeLater(new ChatChat());
	}

	public void run()
	{  
		userOutput = null;
		socketUser = null;
		servers = new ArrayList<Server>();
		
		//////
		// 1. Creating the WINDOW and the COMPONENTS
			
		// Creating the frame
		frame = new JFrame("ChatChat");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(600, 450));
		frame.setMinimumSize(new Dimension(300, 200)); // ! platform dependant (may not work for all platforms)
		frame.setLocationRelativeTo(null);
		frame.addComponentListener(this);
		frame.pack();
		
		// Creating the pane
		container = new JPanel();
		
		
		// Creating the text area, the button, the text field
		textArea = new JTextArea("Bienvenu dans le logiciel ChatChat ! :D\nTapez /? pour acc\u00E9der a l'aide.\n---\n");
		bouton = new JButton("Envoyer");
		textField = new JTextField("/?");
		textArea.setEditable(false);
		bouton.addActionListener(this);
		textArea.setVisible(true);
		bouton.setVisible(true);
		textField.setVisible(true);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		bouton.addKeyListener(this);
		textField.addKeyListener(this);
		textArea.addKeyListener(this);
		
		
		container.add(bouton);
		container.add(textField);
		
		
		
		textScroll = new JScrollPane(textArea);
		
		textScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		textScroll.setVisible(true);
		
		
		container.setAlignmentX(Component.CENTER_ALIGNMENT);
		container.add(textScroll, BorderLayout.CENTER);
		
		
		adaptComponentsBounds();
		
		
		//////
		// 2. Creating the MENU BAR
		
		// build the Connexion menu
		connectMenu = new JMenu("Connexion");
		newConnectMenuItem = new JMenuItem("Se connecter a...");
		disconnectMenuItem = new JMenuItem("Se d\u00E9connecter");
		newConnectMenuItem.addActionListener(this);
		disconnectMenuItem.addActionListener(this);
		connectMenu.add(newConnectMenuItem);
		connectMenu.add(disconnectMenuItem);
		
		// build the Herbergement menu
		hebergeMenu = new JMenu("H\u00E9bergement");
		newHMenuItem = new JMenuItem("Nouvel h\u00E9bergement");
		stopHMenuItem = new JMenuItem("Arreter h\u00E9bergement");
		listeHMenuItem = new JMenuItem("Liste h\u00E9bergements");
		newHMenuItem.addActionListener(this);
		stopHMenuItem.addActionListener(this);
		listeHMenuItem.addActionListener(this);
		hebergeMenu.add(newHMenuItem);
		hebergeMenu.add(stopHMenuItem);
		hebergeMenu.add(listeHMenuItem);
		
		// Build the '?' menu
		helpMenu = new JMenu("?");
		aideMenuItem = new JMenuItem("Aide");
		aideMenuItem.addActionListener(this);
		helpMenu.add(aideMenuItem);
		
		// add menus to menubar
		menuBar = new JMenuBar();
		menuBar.add(connectMenu);
		menuBar.add(hebergeMenu);
		menuBar.add(helpMenu);
		
		// Make the window visible
		frame.setContentPane(container);
		frame.setJMenuBar(menuBar);
		frame.setVisible(true);
	}
	
	
	public void write(String message)
	{
		textArea.append(message+'\n'); // add the message to string area
		textScroll.getVerticalScrollBar().setValue(100000); // set the scrollbar down
	}
	
	
	
	private void adaptComponentsBounds()
	{
		textScroll.setBounds(10,40, container.getSize().width-20, container.getSize().height-120);
		bouton.setBounds(container.getSize().width-95, container.getSize().height-76, 88, 40);
		textField.setBounds(10, container.getSize().height-80, container.getSize().width-110, 50);
	}
	
	
	// When an event is received from a component
	public void actionPerformed(ActionEvent ev)
	{
		if(ev.getSource() == bouton) {   // If the button "Envoyer" was pressed
			validateInput();
		}
		else if(ev.getSource() == aideMenuItem){
			JOptionPane.showMessageDialog(frame, aideString, "Aide", JOptionPane.PLAIN_MESSAGE);
		}
		else if(ev.getSource() == newHMenuItem){
			// create a new local server
			textField.setText("/startserver 1324");
		}
		else if(ev.getSource() == newConnectMenuItem){
			// the user wants to connect to a server
			textField.setText("/connect 127.0.0.1 1324");
		}
		else if(ev.getSource() == listeHMenuItem){
			// print all the server created by the user
			textField.setText("/listserver ");
		}
		else if(ev.getSource() == stopHMenuItem){
			// the user wants to stop a server
			textField.setText("/stopserver 1324");
		}
		else if(ev.getSource() == disconnectMenuItem){
			handleCommand("disconnect");
		}
		
		
	}
	
	private void validateInput()
	{
		final String str = textField.getText();
		if(str.isEmpty())
		{}
		else if(str.charAt(0) == '/')
		{
			handleCommand( str.substring(1) );
		}
		else
		{
			if(socketUser != null)
			{
				try {
					PrintStream output = new PrintStream(socketUser.getOutputStream());
					output.println(textField.getText());
				}
				catch(IOException ee) {
					System.out.println("Erreur, ce dernier message n'a pu etre envoy\u00E9.");
				}
			}
			else
			{
				write("D\u00E9sol\u00E9, vous n'etes pas connect\u00E9, vous ne pouvez pas envoyer de message.");
			}
		}
		textField.setText("");
	}
		
		
	private void handleCommand(String command)
	{
		String[] subCommands = command.split(" ");
		
		if( subCommands[0].equals("?") )
		{
			// The user asks for help, we give him some help
			write(aideString);
		}
		else if( subCommands[0].equals("clear") || command.equals("clearscreen") )
		{
			textArea.setText("");
		}
		else if( subCommands[0].equals("startserver") || subCommands[0].equals("newserver") || subCommands[0].equals("+server") )
		{
			try { // create a new server
				final int port = Integer.parseInt(subCommands[1]);
				servers.add(new Server(port));
				new Thread(servers.get(servers.size()-1)).start();
				write("Serveur cr\u00E9e avec succes (port:"+port+").");
			}
			catch(Throwable e)
			{
				write("Erreur, impossible de cr\u00E9er le server: "+e.getMessage());
			}
		}
		else if( subCommands[0].equals("stopserver") )
		{
			try {
			
			final int searchedPort = Integer.parseInt(subCommands[1]);
			boolean found=false;
			
			for(int i=0;i<servers.size();++i)
			{
				if(servers.get(i).getPort() == searchedPort)
				{
					//servers.get(i).close();
					servers.remove(i);
					found=true;
					break;
				}
				
			}
			
			if(!found){
				write("D\u00E9sol\u00E9, le serveur demande n'existe pas.'");
			}
			
			}
			catch(Throwable e) {
				write("Impossible d'arreter le serveur : "+e.getMessage());
			}
		}
		else if( subCommands[0].equals("connect") )
		{
			// try to connect to a chosen server
			try
			{
				if(subCommands[1].equals("localhost")) subCommands[1]="127.0.0.1";
				socketUser = new Socket(subCommands[1], Integer.parseInt(subCommands[2]));
				new Thread(new ReceivedMessagesHandler(socketUser.getInputStream(), this)).start();
				write("Connection effectu\u00E9e.");
			}
			catch(Throwable e){
				write("Erreur, connection impossible : "+e.getMessage());
			}
		}
		else if( subCommands[0].equals("listserver") )
		{
			if(servers.size()>0) {
				write("Voici l'ensemble des serveurs locaux existant:'");
				for(int i=0;i<servers.size();++i) write("- Serveur ouvert sur le port "+servers.get(i).getPort());
			}
			else {
				write("Aucun serveur n'a ete ouvert.'");
			}
		}
		else if( subCommands[0].equals("disconnect") )
		{
			if(socketUser == null){
				write("Vous n'etes pas connecte.");
			}
			else {
				// we disconnect the user from the server
				try {
					socketUser.close();
					write("Deconnexion effectuee.");
				}
				catch(Throwable e) {
					write("Impossible de se deconnecter: "+e.getMessage());
				}
				socketUser = null;
			}
		}
		else if( subCommands[0].equals("getip") )
		{
			try {
			// adresse ip locale
			write(InetAddress.getLocalHost().getHostAddress());
			}
			catch(Throwable ignore){}
		}
		else
		{
			write("D\u00E9sol\u00E9, la commande ''/" +command+ "'' n'existe pas.");
		}
	}
	
	//-----------
	// Keyboard listener methods
	public void keyTyped(KeyEvent e){}
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_ENTER){ // Invoked when the Enter key has been pressed.
			validateInput();
		}
	}
	public void keyReleased(KeyEvent e){}
	
	
	//------------
	// Component listener methods
	public void componentHidden(ComponentEvent e){}
	public void componentMoved(ComponentEvent e){}
	public void componentShown(ComponentEvent e){}
	public void componentResized(ComponentEvent e) // When the window is resized
	{ 	adaptComponentsBounds(); } // We change the size of the different components
	
}


class ReceivedMessagesHandler implements Runnable
{
  private InputStream server;
  private ChatChat slate;

  public ReceivedMessagesHandler(InputStream server, ChatChat app)
  {
    this.server = server;
    this.slate = app;
  }

  public void run()
  {
    // receive server messages and print out to the Gui
    Scanner s = new Scanner(server);
    
    while(s.hasNextLine())
    {
		String ssss=s.nextLine();
      slate.write(ssss);
    }
    s.close();
  }
}

