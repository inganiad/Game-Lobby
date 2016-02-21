/*

    Client class different from LobbyClient which is a GUI, 
        works with LobbyServer and Room

*/

import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

public class Client implements Runnable{

    private Socket connection;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String name;
    private Room currentRoom;
    private LobbyServer serverParent;

    public Client(LobbyServer server, Socket socket){
        serverParent = server;
        connection = socket;
    }
    
    public void run(){
    
        try{
            output = new ObjectOutputStream(connection.getOutputStream());
            input = new ObjectInputStream(connection.getInputStream());

            name = (String)input.readObject();
            
            if(serverParent.hasName(name)){ //TODO make this communication happen without wasting a thread
                output.writeObject(false);
                output.flush();
                return;
            }
            else{
                output.writeObject(true);
                output.flush();
            }
            
            currentRoom = new Room();
            serverParent.addClient(this);
            
        }
        catch(Exception e){
            name = "";
            e.printStackTrace();
        }
        
        serverParent.serverPrintln("Client "+name+" joined");
        
        // Wait For Information
        loop:
        while(true){
        
            try{
            
                String hint = (String)input.readObject();
                
                switch(hint){
                
                    case "close":
                    
                        serverParent.removeClient(this);
                        break loop;
                        
                        
                    case "makeRoom":
                        
                        String[] roomData = (String[])input.readObject();
                        
                        currentRoom = new Room(serverParent, Integer.parseInt(roomData[0]), roomData[1], roomData[2], this);
                        serverParent.updateLists();
                        break;
                        
                        
                    case "joinRoom":
                        
                        String hostName = (String)input.readObject();
                        serverParent.serverPrintln("Client "+name+" requests to join host \""+hostName+"\"");
                        
                        try{
                            
                            Room room = serverParent.getRoomByHost(hostName);
                            
                            if(room.addClient(this)){
                                currentRoom = room;
                                serverParent.updateLists();
                            }
                            
                            serverParent.serverPrintln("Client "+getName()+" is in room? "+isInRoom());
                            
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        
                        break;
                        
                        
                    case "leaveRoom":
                    
                        if(!currentRoom.isEmpty()){
                            
                            currentRoom.removeClient(this);
                            currentRoom = new Room();
                            serverParent.updateLists();
                            
                        }
                        
                        break;
                        
                        
                    case "play":
                        currentRoom.launchGame();
                        break;
                    
                    
                    case "move":
                        currentRoom.handleMove(this, (String)input.readObject());
                        break;
                    
                    
                    case "endgame":
                        currentRoom.endGame(this);
                        break;
                        
                        
                    default:
                        serverParent.serverPrintln("Unknown hint \"" +hint+"\"");
                        break;
                
                }
            
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        
        }
    
    
    
    }
    
    
    public void send(String hint, Object obj){
        try{
            output.flush();
            output.writeObject(hint);
            output.flush();
            output.writeObject(obj);
            output.flush();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        
    }
    
    public void send(String hint){
        try{
            output.flush();
            output.writeObject(hint);
            output.flush();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    public boolean isInRoom(){
        return !currentRoom.isEmpty();
    }
    
    public Room getRoom(){
        return currentRoom;
    }
    
    public String getName(){
        return name;
    }
   
    public String toString(){
        return name;
    }

}