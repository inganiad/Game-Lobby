
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;



public class LobbyClient{

	private JFrame frame;
    private JPanel cards;
    private final JList<String> lobbyRoomNamesList, roomClientsNamesList;
    private JLabel loginNameLabel, roomViewNameLabel, roomNameLabel, roomGameLabel, builderErrorLabel;
    private CardLayout cardLayout;
	private Socket connection;
	private ObjectOutputStream output;
	private ObjectInputStream input;
    private boolean isRoomHost = false;
    
    private static HashMap<String,int[]> gameCapacityMap = new HashMap<String,int[]>();
    
	private String name, currentCardName;
    
    private final String CONNECT_NAME = "connect",
                         BUILDER_NAME = "builder",
                         LOBBY_VIEW_NAME = "lobbyView",
                         ROOM_VIEW_NAME = "roomView";
                         
    private static final String[] GAME_NAMES = {
        "",
        "Backgammon"
    };
    
    private static final int[][] GAME_SPINNER_VALUES = {
        {0,0,0,0},
        {2,2,2,2}
    };
	
	private static String serverIP;
    
    //Components only visible if this is host
    private JButton playRoomGameButton = new JButton(), kickClientButton = new JButton();
    
    private JComponent[] hostComponents;
    
    private Backgammon backgammon;
	
	
	public static void main(final String[] args){
        
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				// try/catch for IOException
                try{
                    serverIP = args[0]; //TODO: If fails, display message "unable to connect"
                }
                catch(Exception ex){
                    serverIP = "127.0.0.1";
                }
                
				try{
					LobbyClient client = new LobbyClient(serverIP);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}
	
	
	@SuppressWarnings("unchecked") 
	public LobbyClient(String hostServerIP){
        
        // serverIP = hostServerIP;
        // clientPrintln("HostServerIP = "+hostServerIP);
        // clientPrintln("ServerIP = "+serverIP);
        
        for(int i = 0; i < GAME_NAMES.length; i++){
            gameCapacityMap.put(GAME_NAMES[i], GAME_SPINNER_VALUES[i]);
        }
	
		frame = new JFrame("Lobby Client Window");
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setMinimumSize(new Dimension(500,300));
		frame.setResizable(true);
		frame.setLocationByPlatform(true);
		frame.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                send("close");
                ((JFrame)(e.getComponent())).dispose();
            }
        });
        
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(frame);
        }
        catch(Exception e){}
        
		
		Container pane = frame.getContentPane();
		
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        
        GridBagConstraints c = new GridBagConstraints();
		
        
        JPanel tempPanel;
        
    //Connection panel
		JPanel connectPanel = new JPanel(new GridBagLayout());
        
		
		final String initialText = "Enter your name here";
		final JTextField clientNameField = new JTextField(initialText, 25);
		
		
        
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.weightx = 1;
		c.weighty = 1;
		
		connectPanel.add(clientNameField,c);
		
		c.gridwidth = 1;
		c.weighty = 0.2;	
		c.gridy = 1;
		c.gridx = 0;
		connectPanel.add(new JPanel(), c);
		
		JButton joinServerButton = new JButton("Join");
							
					
		c.gridx = 1;
		connectPanel.add(joinServerButton,c);
		
		c.gridx = 2;
		connectPanel.add(new JPanel(), c);
	
        //Listeners
        clientNameField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				clientNameField.selectAll();
			}
			public void focusLost(FocusEvent e){}
		});
		
		clientNameField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                            
                try{
                    String nm = clientNameField.getText();
                    if(nm.length() > 0 && !nm.equals(initialText)){
                        name = nm;
                        if(!establishConnection(nm)){
                            clientNameField.setText("Please choose a different name, someone is already using \""+nm+"\"");
                            clientNameField.selectAll();
                        }
                        else{
                            loginNameLabel.setText("Logged in as \""+name+"\"");
                            roomViewNameLabel.setText("Logged in as \""+name+"\"");
                            updateCard(LOBBY_VIEW_NAME);
                        }
                    }
                    
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
                
            }           
        });
        
        joinServerButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
            
                try{
                    String nm = clientNameField.getText();
                    if(nm.length() > 0 && !nm.equals(initialText)){
                        name = nm;
                        if(!establishConnection(nm)){
                            clientNameField.setText("Please choose a different name, someone is already using \""+nm+"\"");
                            clientNameField.selectAll();
                        }
                        else{
                            loginNameLabel.setText("Logged in as \""+name+"\"");
                            roomViewNameLabel.setText("Logged in as \""+name+"\"");
                            updateCard(LOBBY_VIEW_NAME);
                        }
                    }
                    
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
                
            }
        });
		
    
    
    //Lobby view pannel
    
        JPanel lobbyViewPanel = new JPanel(new GridBagLayout());
        
        JButton joinRoomButton = new JButton("Join Room"),
                makeButton = new JButton("Make Room");
                
        //Label
        loginNameLabel = new JLabel("");
        
        c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 1;
		c.weighty = 1;
        
        lobbyViewPanel.add(loginNameLabel, c);
        
        // List
        lobbyRoomNamesList = new JList<String>();
    
        JScrollPane scroll = new JScrollPane(lobbyRoomNamesList);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		
        c.fill = GridBagConstraints.HORIZONTAL;
		lobbyViewPanel.add(scroll, c);
        c.fill = GridBagConstraints.NONE;
		
		// Buffer panel
		c.gridy = 2;
		c.gridwidth = 1;
		lobbyViewPanel.add(new JPanel(), c);
		
		// joinRoomButton
		c.gridx = 1;
		
		lobbyViewPanel.add(joinRoomButton, c);
        
        //Make button
        c.gridx = 2;
        c.weightx = 0.5;
        
        lobbyViewPanel.add(makeButton, c);
        
        
        //Listeners
        
        lobbyRoomNamesList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
                
				JList l = (JList)evt.getSource();
                
				if (evt.getClickCount() == 2) {
                    
					int index = l.locationToIndex(evt.getPoint());
					ListModel<String> model = l.getModel();
					String selectedName = "", tempStr, hostDelim = "Host: ";
					
					try{
                        
                        tempStr = model.getElementAt(index);
                        int startIndex = tempStr.indexOf(hostDelim)+hostDelim.length();
						selectedName = tempStr.substring(startIndex);
                        
					}
                    
					catch(Exception ex){
						selectedName = model.getElementAt(index);
                        clientPrintln("Error with parse of room host");
					}
                    
                    send("joinRoom", selectedName);
					
				}
			}
		});
        
        joinRoomButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                
                String selectedName = "", tempStr, hostDelim = "Host: ";
                Room selectedRoom;
                
                try{
                    
                    tempStr = lobbyRoomNamesList.getSelectedValue();
                    int startIndex = tempStr.indexOf(hostDelim)+hostDelim.length();
                    selectedName = tempStr.substring(startIndex);
                    
                }
                catch(Exception ex){
                    selectedName = lobbyRoomNamesList.getSelectedValue();
                    clientPrintln("Error with parse of room host");
                }
                
                send("joinRoom", selectedName);
                
            }
        });
        
        makeButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                try{
                    updateCard(BUILDER_NAME);
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });

        
        
    //Room builder panel
        
        
        final JPanel builderPanel = new JPanel(new GridBagLayout());
        
        builderErrorLabel = new JLabel("sample");
        builderErrorLabel.setForeground(builderPanel.getBackground());
        
        JButton cancelBuildButton = new JButton("Cancel"),
                buildRoomButton = new JButton("OK");
               
        final String roomNameInitialText = "Name";
        final JTextField nameField = new JTextField(roomNameInitialText, 15);

        final JComboBox gamesComboBox = new JComboBox(GAME_NAMES);
        
        final SpinnerNumberModel capacitySpinnerModel = new SpinnerNumberModel(0,0,0,0);
        JSpinner capacitySpinner = new JSpinner(capacitySpinnerModel);
        ((JSpinner.DefaultEditor) capacitySpinner.getEditor()).getTextField().setEditable(false);
                         
        tempPanel = new JPanel(new GridBagLayout());
        
        //Error label
        c.insets = new Insets(5,5,5,5);
        c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 1;
		c.weighty = 0;
        
        tempPanel.add(builderErrorLabel, c);
        
        builderPanel.add(tempPanel, c);
        
        tempPanel = new JPanel(new GridBagLayout());
        
        
        //Name label
        c.weighty = 1.0;
        
        tempPanel.add(new JLabel("Room Name: "), c);
        
        //Name field
        c.gridx = 1;
        tempPanel.add(nameField ,c);
        
        //Game label
        c.gridx = 0;
        c.gridy = 1;
        tempPanel.add(new JLabel("Game: "), c);
        
        //Game combobox
        c.gridx = 1;
        gamesComboBox.setEditable(false);
        c.anchor = GridBagConstraints.LINE_START;
        tempPanel.add(gamesComboBox, c);
        c.anchor = GridBagConstraints.CENTER;
        
        //Capacity label
        c.gridx = 0;
        c.gridy = 2;
        tempPanel.add(new JLabel("Room Capacity: "), c);
        
        //Capacity spinner
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        tempPanel.add(capacitySpinner, c);
        c.anchor = GridBagConstraints.CENTER;
        
        //Temp panel holding labels and fields
        c.gridx = 0;
		c.gridy = 1;
        builderPanel.add(tempPanel, c);
        
        tempPanel = new JPanel(new GridBagLayout());
                      
        //Build button
        
        c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 0.5;
		c.weighty = 0.5;
                
        tempPanel.add(buildRoomButton, c);       
                
                
        //Cancel button
        c.gridx = 1;
                
        tempPanel.add(cancelBuildButton, c);
        
        //Temp panel holding buttons
        c.insets = new Insets(0,0,0,0);
        c.gridx = 0;
		c.gridy = 2;
        builderPanel.add(tempPanel, c);
        
        tempPanel = new JPanel(new GridBagLayout());
        
        
        //Listeners
        gamesComboBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                
                try{
                    String gameName = (String)(((JComboBox)e.getSource()).getSelectedItem());
                    
                    
                    int[] values = gameCapacityMap.get(gameName);
                    
                    capacitySpinnerModel.setValue(values[0]);
                    capacitySpinnerModel.setMinimum(values[1]);
                    capacitySpinnerModel.setMaximum(values[2]);
                    capacitySpinnerModel.setStepSize(values[3]);
                    
                    
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        
        buildRoomButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                
                try{
                    
                    String roomName = nameField.getText(),
                           gameName = (String)gamesComboBox.getSelectedItem();
                           
                    int i = capacitySpinnerModel.getNumber().intValue();
                    
                    boolean isInvalid = false;
                    
                    if(roomName.equals("") || roomName.equals(roomNameInitialText)){
                        builderErrorLabel.setText("Please enter a different name for the room");
                        isInvalid = true;
                    }
                    else if(gameName.equals("")){
                        builderErrorLabel.setText("Please select a game to play");
                        isInvalid = true;
                    }
                    else if(i < 2){
                        builderErrorLabel.setText("That is an invalid capacity for a room");
                        isInvalid = true;
                    }
                    else{
                        send("makeRoom", new String[]{""+i, roomName, gameName});
                        updateCard(ROOM_VIEW_NAME);
                    }
                        
                    if(isInvalid){
                        builderErrorLabel.setForeground(Color.BLACK);
                    }
                    
                }
                catch(Exception ex){
                    builderErrorLabel.setText("Error with input");
                }
                
            }
        });
        
        cancelBuildButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                builderErrorLabel.setText("sample");
                builderErrorLabel.setForeground(builderPanel.getBackground());
                updateCard(LOBBY_VIEW_NAME);
            }
        });
		
		nameField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        nameField.selectAll();
                    }
                });
			}
			public void focusLost(FocusEvent e){}
		});
        
        nameField.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               try{
                    
                    String s = nameField.getText();
                    
                    if(s.equals("") || s.equals(roomNameInitialText)){
                        builderErrorLabel.setText("Please enter a different name for the room");
                    }
                    else{
                        int i = capacitySpinnerModel.getNumber().intValue();
                        send("makeRoom", new String[]{""+i, s});
                        updateCard(ROOM_VIEW_NAME);
                    }
                        
                    
                }
                catch(Exception ex){
                    builderErrorLabel.setText("Error with input");
                }
           } 
        });
    
    //Room view pannel
    
        //TODO add current name label above jlist area 
        JPanel roomViewPanel = new JPanel(new GridBagLayout());
        
        roomViewNameLabel = new JLabel(loginNameLabel.getText());
        roomNameLabel = new JLabel("");
        roomGameLabel = new JLabel("");
        
        JButton leaveRoomButton = new JButton("Leave Room");
        
        playRoomGameButton = new JButton("Launch Game");
        kickClientButton = new JButton("Kick Player");
        
        roomClientsNamesList = new JList<String>();
        scroll = new JScrollPane(roomClientsNamesList);
        
        //Game label
        c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 1;
		c.weighty = 1;
        
        roomViewPanel.add(roomGameLabel);
        
        //Name label
        c.gridx = 1;
    
        roomViewPanel.add(roomViewNameLabel, c);
        
        //Room name label
        c.gridx = 2;
        
        roomViewPanel.add(roomNameLabel, c);
    
        //List
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		
        c.fill = GridBagConstraints.HORIZONTAL;
		roomViewPanel.add(scroll, c);
        c.fill = GridBagConstraints.NONE;
        
        // Kick button
		c.gridy = 2;
		c.gridwidth = 1;
        kickClientButton.setEnabled(false);
        
		roomViewPanel.add(kickClientButton, c);
        
        //Play button
        c.gridx = 1;
        playRoomGameButton.setEnabled(false);
        
        roomViewPanel.add(playRoomGameButton, c);
		
		// Leave Button
		c.gridx = 2;
		c.weightx = 0.5;
		c.insets = new Insets(5,5,5,5);
		
		roomViewPanel.add(leaveRoomButton, c);
        
        
        //Listeners
        leaveRoomButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                try{
                    send("leaveRoom");
                    changeHostStatus(false);
                    updateCard(LOBBY_VIEW_NAME);
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        
        playRoomGameButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                send("play");
            }
        });
        
    //Add to card layout and show
        
        hostComponents = new JComponent[]{playRoomGameButton, kickClientButton};
        
		cards.add(connectPanel, CONNECT_NAME);
        cards.add(builderPanel, BUILDER_NAME);
        cards.add(lobbyViewPanel, LOBBY_VIEW_NAME);
        cards.add(roomViewPanel, ROOM_VIEW_NAME);
        
        updateCard(CONNECT_NAME);
        
		pane.add(cards);
		
		frame.setVisible(true);
		
	
	}


    
	private boolean establishConnection(String nm){
	
		try{
            // clientPrintln("ServerIP = "+serverIP);
			connection = new Socket(InetAddress.getByName(serverIP), 6789);
			output = new ObjectOutputStream(connection.getOutputStream());
			input = new ObjectInputStream(connection.getInputStream());
			
			name = nm;
			
			output.writeObject(name);
			output.flush();
            
            if((boolean)input.readObject()){    //Server returns whether name is taken
                new Thread(new Runnable(){
                    public void run(){
                        waitForCommands();
                    }
                }).start();
            }
            else{
                return false;
            }
			
		}
		catch(Exception ex){
			ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot connect to server at "+serverIP);
		}
		
        return true;
	
	}
	
	private void clientPrintln(Object o){
		System.out.println("\nCLIENT ("+this.name+") - "+o.toString());
	}
	
	private void clientPrint(Object o){
		System.out.print("\nCLIENT ("+this.name+") - "+o.toString());
	}
	
    private void launchGame(String name, boolean isFirst){
        
        switch(name){
            
            case "Backgammon":
                backgammon = new Backgammon(this, isFirst);
                clientPrintln("Received Backgammon instruction");
                break;
            
            
            default:
                break;
            
        }
        
    }
    
	private void showList(String[] roomNames){
    
        lobbyRoomNamesList.setListData(roomNames);
        
        if(currentCardName.equals("connect")){
            cardLayout.show(cards, "lobbyView");
            currentCardName = "lobbyView";
        }
        
        updatePane(cards);
    
	}
    
    private void showRoom(String[] clientNames){
		
        try{
            String[] roomInfo = clientNames[0].split("#");
            roomNameLabel.setText(roomInfo[0]);
            roomGameLabel.setText(roomInfo[1]);
        }
        catch(Exception e){
            clientPrintln("Bad room data send in clientNames array");
        }
        
        //First index is Room's name so must remove
        clientNames = Arrays.copyOfRange(clientNames, 1, clientNames.length);
        
        for(String str: clientNames){
            
            if(str.indexOf("(host)") > 0 && str.substring(0, str.indexOf(" ")).equals(this.name)){
                changeHostStatus(true);
                clientPrintln("This is host of room "+roomNameLabel.getText());
                break;
            }
            
        }
        
		roomClientsNamesList.setListData(clientNames);
        
        updateCard("roomView");
            
        
		
    }
    
    private void updateCard(String cardName){
        
        cardLayout.show(cards, cardName);
        currentCardName = cardName;
        updatePane(cards);
        
    }
    
	private void send(String hint){
	
		try{
			output.writeObject(hint);
			output.flush();
		}
		catch(NullPointerException nPE){}
		catch(Exception ex){
			ex.printStackTrace();
		}
	
	}
	
	private void send(String hint, Object o){
	
		try{
			output.writeObject(hint);
			output.flush();
			output.writeObject(o);
			output.flush();
		}
		catch(NullPointerException nPE){}
		catch(Exception ex){
			ex.printStackTrace();
		}
	
	}
	
	private void updatePane(final JPanel panel){
	
		SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){
		
			protected Void doInBackground() throws Exception{
				return null;
			}
		
			protected void done(){
				Container pane = frame.getContentPane();
				
				pane.removeAll();
				
				pane.add(panel);
				
				pane.revalidate();
				pane.repaint();
			}
		
		};
		
		worker.execute();
		
	
	}
	
    private void changeHostStatus(boolean isHost){
        
        isRoomHost = isHost;
        
        for(JComponent component: hostComponents){
            component.setEnabled(isHost);
        }
        
    }
    
    public void sendMove(String move){
        send("move",move);
    }
    
    public void endGame(){
        send("endgame");
    }
    
    @SuppressWarnings("unchecked") 
	private void waitForCommands(){
	
		while(true){
            
			try{
				String hint = (String)input.readObject();
				
				switch(hint){
				
					case "update":
						showList( ((String[])input.readObject()) );
						break;
                        
                    case "roomView":
                        showRoom( ((String[])input.readObject()) );
                        break;
                        
                    case "Backgammon":
                        boolean isFirst = (boolean)input.readObject();
                        launchGame(hint,isFirst);
                        break;
                        
                    case "showMove":
                        backgammon.processOpponentMove((String)input.readObject()); //TODO must be changed to general case                        
                        break;
                        
                    case "endgame":
                        backgammon.endGame((boolean)input.readObject());
                        break;
						
					default:
						clientPrintln("\n\tUnknown hint \"" +hint+"\"");
						break;
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		
		}
	}
	

}