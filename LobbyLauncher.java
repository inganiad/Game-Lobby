

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import java.awt.event.*;

public class LobbyLauncher{

	private static ExecutorService executor = Executors.newFixedThreadPool(2);
    
    private static final int DEFAULT_SPAWN_NUMBER = 2;
    private static final String SELF_IP_ADDRESS = "127.0.0.1";
    
    private static int clientsToSpawn;
    private static String serverIP;
    
	public static void main(String[] args){
	
        try{
            clientsToSpawn = Integer.parseInt(args[0]);
        }
        catch(Exception ex){
            clientsToSpawn = DEFAULT_SPAWN_NUMBER;
        }
        
        try{
            serverIP = args[1];
        }
        catch(Exception ex){
            serverIP = SELF_IP_ADDRESS;
        }
	
		executor.submit(new Runnable(){
			public void run(){
				LobbyServer s = new LobbyServer();
			}
		});
		
		executor.submit(new Runnable(){
			public void run(){
				ClientMaker cm = new ClientMaker();
			}
		});
		
		executor.shutdown();

	}
	
	@SuppressWarnings("serial") private static class ClientMaker extends JFrame{
	
		public ClientMaker(){
		
            try{
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SwingUtilities.updateComponentTreeUI(this);
            }
            catch(Exception e){}
        
			setTitle("Maker");
			setResizable(false);
			setSize(200,200);
			// setLocationByPlatform(true);
			setLocation(1000, 350);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			JButton button = new JButton("Create");
			
			button.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					new Thread(new Runnable(){
						public void run(){
							LobbyClient c = new LobbyClient(serverIP);
						}
					}).start();
				}
			});
			
			getContentPane().add(button);
			
			setVisible(true);
			
            for(int i = 0; i < clientsToSpawn; i++){
                new Thread(new Runnable(){
                    public void run(){
                        LobbyClient c = new LobbyClient(serverIP);
                    }
                }).start();
            }
            
		}
	
	
	}
	
	


}