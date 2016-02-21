
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors; 


public class LobbyServer{

    private ServerSocket serverSocket;
    private HashSet<Client> clients = new HashSet<Client>();
    private HashSet<Room> rooms = new HashSet<Room>();

    

    
    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            public void run(){
                // try/catch for IOException
                try{
                    LobbyServer lobby = new LobbyServer();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        
    }
    
    public LobbyServer(){
        try {
            
            serverSocket = new ServerSocket(6789, 100);
            
            
            new Thread(new Runnable(){
                public void run(){
                    waitForConnections();
                }
            }).start();
            
                
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void waitForConnections(){
        
        // Loop that receives and controls connections
        // Creates a new object per connection 
        while(true){
            try{
            
                Socket connection = serverSocket.accept();
                Client client = new Client(this, connection);
                new Thread(client).start();
                
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        
    
    }
    
    public void updateLists(){
    
        // serverPrintln("Rooms size = "+rooms.size());
        
        String[] roomNames = new String[rooms.size()];
        
        int i = 0;
        for(Room r: rooms){
            roomNames[i++] = r.toString();
        }
        
        for(Client client: clients){
        
            if(client.isInRoom()){
                client.send("roomView", client.getRoom().getClientNames());
            }
			else{
                client.send("update", roomNames);
                // serverPrintln("Client not in room "+client.getName());
			}

        }
        
    }
    
    public void addClient(Client c){
        clients.add(c);
        updateLists();
    }
    
    public void addRoom(Room r){
        rooms.add(r);
        updateLists();
    }
    
    public void removeRoom(Room r){
        rooms.remove(r);
        // serverPrintln("Removed Room ("+r.getName()+"), rooms size now "+rooms.size());
        updateLists();
    }
    
    public boolean hasName(String s){
        
        for(Client c: clients){
            if(c.getName().equals(s)){
                return true;
            }
        }
        
        return false;
    }
    
    public void removeClient(Client c){
        
        if(c.isInRoom()){
            c.getRoom().removeClient(c);
        }
        
        String n = c.getName();
        clients.remove(c);
        updateLists();
        serverPrintln("Client "+n+" left the server");
        
    }
    
    public void serverPrintln(String s){
        System.out.println("\nSERVER - "+s);
    }
    
    public void serverPrint(String s){
        System.out.print("\nSERVER - "+s);
    }
    
    public Room getRoomByHost(String hostName){
        
        for(Client c: clients){
            
            if(c.getName().equals(hostName)){
                serverPrintln("Successfully found room with host \""+hostName+"\"");
                return c.getRoom();
            }
            
        }
        
        serverPrintln("Error finding room with host "+hostName);
        return new Room();
    }
    
    
    

}

