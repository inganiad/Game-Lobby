 /*
 
    Room class to work with LobbyServer and LobbyClient
 
 */

import java.util.HashSet;
import java.util.Random;
 
 public class Room{
    
    private LobbyServer serverParent; //Be careful about calling this if capacity is 0
    
    private int capacity;
    private String name, gameName;
    private Client host;
    private HashSet<Client> clients = new HashSet<Client>();
    private boolean empty = true;

    public Room(){
        capacity = 0;
        name = "EMPTY ROOM";
    }
    
    public Room(LobbyServer server, int i, String nameStr, String gameStr, Client c){
        
        serverParent = server;
        
        capacity = i;
        name = nameStr;
        gameName = gameStr;
        
        addClient(c);
        
        
        serverParent.addRoom(this);
    }
    
    public boolean addClient(Client c){
        
        empty = false;
        
        if(clients.size() == 0){
            serverParent.serverPrintln("Room "+name+" size is 0");
            assignHost(c);
        }
        
        if(clients.size() < capacity){
            clients.add(c);
            serverParent.updateLists();
            return true;
        }
        
        return false;
    }
    
    public void removeClient(Client c){
        
        clients.remove(c);
        
        if(clients.size() <= 0){
            empty = true;
            serverParent.removeRoom(this);
        }
        else if(host.equals(c)){
            for(Client cl: clients){ //Gets random client to be host, there probably is a better way to do this
                assignHost(cl);
                break;
            }
        }
        
    }
    
    public boolean isFull(){
        return clients.size() == capacity;
    }
    
    public int getCapacity(){
        return capacity;
    }
    
    public int getSize(){
        return clients.size();
    }
    
    public String getName(){
        return name;
    }
    
    public String[] getClientNames(){
    
        String[] names = new String[clients.size()+1];
        
        names[0] = this.name+"#"+this.gameName;
        int i = 1;
        
        for(Client c: clients){
            names[i++] = c.getName() + (c.equals(host)? " (host)" : "");
        }
        
        return names;
        
    }

    public Client getHost(){
        return host;
    }
        
    public boolean isEmpty(){
        return empty; //TODO: may need some aditional factors
    }
    
    public void launchGame(){
        
        switch(gameName){
            
            case "Backgammon":
                
                //TODO check if room has proper number of players
                
                boolean firstPlayerPicked = false;
                for(Client c: clients){
                    
                    if(!firstPlayerPicked){
                        c.send("Backgammon", true);
                        firstPlayerPicked = true;
                    }
                    else{
                        c.send("Backgammon", false);
                    }
                    
                }
                
                break;
            
            default:
                break;
            
        }
        
    }
    
    public void handleMove(Client mover, String move){
        
        for(Client c: clients){
            
            if(!c.equals(mover)){
                c.send("showMove", move);
            }
            
        }
        
    }
    
    public void endGame(Client winner){
        
        for(Client c: clients){
            
            if(!c.equals(winner)){
                c.send("endgame", false);
            }
            else{
                c.send("endgame", true);
            }
            
        }
        
    }
    
    private void assignHost(Client c){
        host = c;
        serverParent.serverPrintln("Room "+name+" assigned host to "+c.getName());
        //c.send("host"); //TODO: implement this hint in client and lobbyclient
    }
    
    public String toString(){
        return empty? "EMPTY ROOM" : name + 
                                     "   ("+clients.size()+"/"+capacity+")"+
                                     "    Host: "+host.getName(); //Host always last
    }
}