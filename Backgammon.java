/*

    Key concepts of Backgammon.java:
    
        "Spike" panels represent the triangle-looking spaces on a backgammon board
        
        board indicies are numbered:
            -1 is one's own "jail" where captured pieces are storred
            0-23 are the 24 "spikes" where 0 is the farthest from the goal and 23 is closest
            24 represents one's goal which is the objective
            -100 represents the other player's jail
            -999 represents the other player's goal


*/


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;


// public class Backgammon extends MultiplayerGame{
public class Backgammon{

    private final int SCALE = 135, 
                      MY_GOAL_INDEX = 24, 
                      MY_JAIL_INDEX = -1, 
                      THEIR_GOAL_INDEX = -999, 
                      THEIR_JAIL_INDEX = -100,
                      CURRENT_SELECTION_VALUE = -1234;
                      
    private JFrame frame;
    private BackgammonPanel[] gamePanels;
    private BackgammonPanel myJail, myGoal, theirJail, theirGoal;
    private HashMap<Integer, BackgammonPanel> panelMap;
    private JButton rollButton;
    private JLabel infoLabel;
    private JPanel controlPanel;
    
    private Color myColor = new Color(230,190,138), 
            theirColor = new Color(153,101,21), 
            bg = Color.WHITE, 
            highlightColor = new Color(255,215,0);
            
    private final BoardPanel board;
    
    private static int testConfigurations = 0;
    
    private boolean testing = false; //Testing values; Must change
    
    private LobbyClient client;
    
    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            public void run(){
                
                // try{
                    // testConfigurations = Integer.parseInt(args[0]);
                    // if(testConfigurations < 1 || testConfigurations > 4){
                        // throw new Exception();
                    // }
                // }
                // catch(Exception e){
                    // testConfigurations = 1;
                // }
                
                try{
                    Backgammon backgammon = new Backgammon(null, true);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
    
    
    
    public Backgammon(LobbyClient cl, boolean isFirst){
    
        frame = new JFrame("Backgammon");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(8*SCALE, 6*SCALE);
        
        Container pane = frame.getContentPane();
        
        board = new BoardPanel(new GridLayout(2,14), isFirst);
        pane.add(board);
        
        client = cl;
        
        if(testing){
            JMenuBar bar = new JMenuBar();
            
            JMenu newGameMenu = new JMenu("New Game");
            newGameMenu.addMouseListener(new MouseAdapter(){
                public void mousePressed(MouseEvent e){
                    newGame();
                    updateControlPanel(rollButton);
                }
            });
            
            bar.add(newGameMenu);
            
            
            
            JMenu randomCapMenu = new JMenu("Random Cap");
            randomCapMenu.addMouseListener(new MouseAdapter(){
                public void mousePressed(MouseEvent e){
                    System.out.println("");
                    captureRandomPiece();
                }
            });
            
            bar.add(randomCapMenu);
            
            
            frame.setJMenuBar(bar);
        }
        
        //Control panel
        controlPanel = new JPanel(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 1;
		c.weighty = 1;
        
        rollButton = new JButton("Roll");
        rollButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                ((JButton)e.getSource()).setEnabled(false);
                board.roll();
            }
        });
        
        infoLabel = new JLabel("Waiting for other player to move...");
        infoLabel.setFont(new Font("Default",Font.PLAIN,20)); //20 keeps panels from shifting when going from btn<->label
        
        if(isFirst){
            controlPanel.add(rollButton, c);
        }
        else{
            controlPanel.add(infoLabel, c);
        }
        
        
        
        //Configure pane
        c.fill = GridBagConstraints.BOTH;
        pane.setLayout(new GridBagLayout());
        pane.add(board, c);
        
        c.gridy = 1;
        c.weighty = 0.1;
        
        pane.add(controlPanel, c);
        
        
        
        
        
        
        newGame();
        
        frame.revalidate();
        frame.repaint();
        
        frame.setVisible(true);
        
    
    }
    
    private void newGame(){
        
        //Follows the flow of gridlayout so the game board can add panels simply
        int[] pieceHints = {11,10,9,8,7,6,
                            THEIR_JAIL_INDEX,
                            5,4,3,2,1,0,
                            THEIR_GOAL_INDEX,
                            12,13,14,15,16,17,
                            MY_JAIL_INDEX,
                            18,19,20,21,22,23,
                            MY_GOAL_INDEX};
                            
        gamePanels = new BackgammonPanel[28];
        panelMap = new HashMap<Integer, BackgammonPanel>();
        
        //Board and add panels
        for(int i = 0; i < 2*14; i++){
        
            BackgammonPanel tempPanel;
        
            if( (i+1)%7 == 0){
                tempPanel = new BackgammonPanel(bg, pieceHints[i] >= MY_JAIL_INDEX? myColor:theirColor, 0, pieceHints[i], i > 13);
            }
            else{
                if(i > 5 && i < 20){
                    tempPanel = new SpikePanel((i%2==1? Color.RED:Color.BLACK), Color.BLUE, 0, pieceHints[i], i > 13);
                }
                else{
                    tempPanel = new SpikePanel((i%2==0? Color.RED:Color.BLACK), Color.BLUE, 0, pieceHints[i], i > 13);
                }
                
                
            }
            
            tempPanel.addMouseListener(new MouseAdapter(){
                public void mousePressed(MouseEvent e){
                    board.manageClick(((BackgammonPanel)e.getSource()).getID());
                }
            });
            
            panelMap.put(pieceHints[i], tempPanel);
            gamePanels[i] = tempPanel;
        
        }
        
        myJail = panelMap.get(MY_JAIL_INDEX);
        myGoal = panelMap.get(MY_GOAL_INDEX);
        
        
        theirJail = panelMap.get(THEIR_JAIL_INDEX);
        theirGoal = panelMap.get(THEIR_GOAL_INDEX);
        
        myJail.setBGColor(bg);
        theirJail.setBGColor(bg);        
        
        
        //Fill with appropriate pieces
        HashMap<Color, int[][]> pieceMap = new HashMap<Color, int[][]>();
        
        
        
        if(testing){
        
            if(testConfigurations == 1){
                /*Normal setup*/
                pieceMap.put(myColor, new int[][]{
                    {0, 2},
                    {11, 5},
                    {16, 3},
                    {18, 5}
                });
                pieceMap.put(theirColor, new int[][]{
                    {5, 5},
                    {7, 3},
                    {12, 5},
                    {23, 2}
                });
            }
            else if(testConfigurations == 2){
                /*Have no moves*/
                pieceMap.put(myColor, new int[][]{
                    {0, 20},
                });
                pieceMap.put(theirColor, new int[][]{
                    {1, 5},
                    {2, 3},
                    {3, 5},
                    {4, 2},
                    {5, 2},
                    {6, 2}
                });   
            }
            else if(testConfigurations == 3){
                /*Goal movement setup*/
                pieceMap.put(myColor, new int[][]{
                    {18, 2},
                    {19, 5},
                    {20, 3},
                    {21, 5}
                });
                pieceMap.put(theirColor, new int[][]{
                    {22, 2},
                    {23, 2}
                });    
            }
            else if(testConfigurations == 4){
               /*Jail movement setup*/
                pieceMap.put(myColor, new int[][]{
                    {-1, 5}
                });
                pieceMap.put(theirColor, new int[][]{
                    {0, 2},
                    {1, 2},
                    {2, 1},
                    {4, 2},
                    {5, 2},
                    {6, 1},
                    {7, 2},
                    {8, 2},
                    {9, 2},
                    {10, 2},
                    {11, 2}
                }); 
            }
            
        }
        else{
            
            pieceMap.put(myColor, new int[][]{
                {0, 2},
                {11, 5},
                {16, 3},
                {18, 5}
            });
            
            pieceMap.put(theirColor, new int[][]{
                {5, 5},
                {7, 3},
                {12, 5},
                {23, 2}
            });
            
        }
            
            
        for(Color c: pieceMap.keySet()){
            for(int x[]: pieceMap.get(c)){
                panelMap.get(x[0]).addPiece(c, x[1]);
            }
        }
        
        board.setBoard();
        
        
    }
    
    private void updateControlPanel(Component com){
        
        GridBagConstraints c = new GridBagConstraints();
        
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 1;
		c.weighty = 1;
        
        controlPanel.removeAll();
        controlPanel.add(com);
        controlPanel.revalidate();
        controlPanel.repaint();
        
    }
    
    private String translateMove(int from, int to){
        
        int theirFrom, theirTo;
        // System.out.println("\n\nThis side's move: "+from+" --> "+to);
        
        if(to == MY_GOAL_INDEX){
            theirTo = THEIR_GOAL_INDEX;
            theirFrom = 23-from;
        }
        else if(from == MY_JAIL_INDEX){
            theirFrom = THEIR_JAIL_INDEX;
            theirTo = 23-to;
        }
        else{
            theirFrom = 23-from;
            theirTo = 23-to;
        }
        
        return theirFrom+","+theirTo;
        // System.out.println("\n\nTheir side's move: "+theirFrom+" --> "+theirTo);
    
    }
    
    private void captureRandomPiece(){
    
        for(BackgammonPanel panel: panelMap.values()){
        
            if(panel.equals(myGoal) || panel.equals(myJail)){
                continue;
            }
            
            if(panel.pieceCount() > 0 && panel.pieceColor.equals(myColor)){
                myJail.addPiece(myColor);
                panel.removePiece();
                board.revalidate();
                board.repaint();
                break;
            }
            
        }
        
    }
    
    public void processOpponentMove(String move){
        
        board.setOpponentMove(move);
        
    }
    
    public void endGame(boolean iWin){
        board.endGame(iWin);
    }
    
    
    private class BackgammonPanel extends JPanel{
    
        public boolean pieceDirection, selected, highlighted, drawSpike;
        public Color spikeColor, pieceColor, backgroundColor;
        public int pieces, id;
        
        public BackgammonPanel(Color sColor, Color pColor, int numPieces, int id, boolean pDirection){
        
            this.spikeColor = sColor;
            this.pieceColor = pColor;
            this.pieces = numPieces;
            this.id = id;
            this.pieceDirection = pDirection;
            
            this.selected = false;
            this.highlighted = false;
            this.drawSpike = false;
            
            this.backgroundColor = bg;
        
        }
    
        public void setBGColor(Color c){
            backgroundColor = c;
        }
    
        public void addPiece(Color c){
            pieceColor = c;
            pieces++;
            repaint();
        }
        
        public void addPiece(Color c, int i){
            pieceColor = c;
            pieces += i;
            repaint();
        }
        
        public void removePiece(){
            if(pieces > 0){
                pieces--;
                repaint();
            }
        }
        
        public void select(){
            if(!selected){
                selected = true;
                repaint();
            }
        }
        
        public void deselect(){
            if(selected){
                selected = false;
                repaint();
            }
        }
        
        public boolean isSelected(){
            return this.selected;
        }
        
        public void highlight(){
            if(!highlighted){
                highlighted = true;
                repaint();
            }
        }
        
        public void unhighlight(){
            if(highlighted){
                highlighted = false;
                repaint();
            }
        }
        
        public boolean isHighlighted(){
            return this.highlighted;
        }
        
        public boolean hasPiece(){
            return pieces > 0;
        }
        
        public int pieceCount(){
            return pieces;
        }
        
        public Color pieceColor(){
            return pieceColor;
        } 
    
        public int getID(){
            return id;
        }
    
        public void paint(Graphics g){
        
            super.paint(g);
            
            Graphics2D g2 = (Graphics2D)g;
            
            RenderingHints rh = new RenderingHints( 
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setRenderingHints(rh);
            
            int h = getHeight(),
                w = getWidth(),
                y = pieceDirection? h - w: 0,
                gap = w;
                    
            if(drawSpike){
                
                g2.setColor(spikeColor);
            
                int[] xCords = {0, w/2, w};
            
                int[] yCords = pieceDirection? new int[]{h, h/10, h} : new int[]{0, 9*h/10, 0};
                
                g2.fillPolygon(xCords, yCords, xCords.length);
                
                if(highlighted){
                
                    g2.setColor(highlightColor);
                    g2.setStroke(new BasicStroke(10));
                    g2.drawPolygon(xCords, yCords, xCords.length);
                    
                    
                    g2.setStroke(new BasicStroke(1));
                }
            }
            else if(highlighted){
                setBackground(highlightColor);
            }
            else{
                setBackground(backgroundColor);
            }
            
            if(pieces > 0){

                g2.setColor(pieceColor);
            
                while( (pieces-1)*gap + w > 9*h/10){
                    gap /= 2;
                }
                
                for(int i = 0; i < pieces; i++, y += pieceDirection? -1*gap : gap){
                    g2.setColor(pieceColor);
                    g2.fillOval(0, y, w, w);
                    
                    g2.setColor(Color.WHITE);
                    g2.drawOval(0, y, w, w);
                }
                
                if(selected){
                    g2.setColor(highlightColor);
                    g2.fillOval(0, y -= pieceDirection? -1*gap : gap, w, w);
                }
            }
        }
    }
    
    private class SpikePanel extends BackgammonPanel{
    
        public SpikePanel(Color bgColor, Color pColor, int numPieces, int id, boolean pDirection){
            
            super(bgColor, pColor, numPieces, id, pDirection);
            this.drawSpike = true;
            
        }
    
        
    }
    
    private class BoardPanel extends JPanel{
    
        private int[] roll;
        private Random rnd;
        private String rollStr, currentMove;
        private boolean hasMoves, myTurn;
        private int currentSelection;
    
    
        public BoardPanel(GridLayout gL, boolean isFirst){
        
            this.rnd = new Random();
            rollStr = "";
            currentMove = "";
            hasMoves = false;
            currentSelection = CURRENT_SELECTION_VALUE;
            
            myTurn = isFirst;
        
            setLayout(gL);
            
        }
        
        public void setBoard(){
            
            roll = new int[2];
            rollStr = "";
            hasMoves = false;
            currentSelection = CURRENT_SELECTION_VALUE;
            removeAll();
            
            for(BackgammonPanel panel: gamePanels){
                add(panel);
            }
            
            revalidate();
            repaint();
        }
        
        public void paint(Graphics g){
        
            super.paint(g);
            
            
            Graphics2D g2 = (Graphics2D)g;
        
            RenderingHints rh = new RenderingHints( 
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setRenderingHints(rh);
            
            //Draw roll in center of panel
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Default",Font.BOLD,40));
            
            
            //Places the roll string in the middle of the board
            FontMetrics fm = g2.getFontMetrics();
            Dimension d = this.getSize();
            
            int x = (d.width - fm.stringWidth(rollStr)) / 2 - (d.width/28); // - width/28 to place in center of board, not panel
            int y = (fm.getAscent() + (d.height - (fm.getAscent() + fm.getDescent())) / 2);
            g2.drawString(rollStr, x, y);
            
        
        }
        
        public void setOpponentMove(String move){
            
            String[] moves = move.split("&");
            
            for(String s: moves){
                System.out.println(s);
            }
            
            for(int i = 0; i < moves.length; i++){
                int index = moves[i].indexOf(',');
                movePiece(Integer.parseInt(moves[i].substring(0,index)), Integer.parseInt(moves[i].substring(index+1)));
            }
            
            
            myTurn = true;
            currentMove = "";
            rollButton.setEnabled(true);
            updateControlPanel(rollButton);
            
        }
        
        private void roll(){
                
            // updateControlPanel(infoLabel);
            
            roll = new int[]{rnd.nextInt(6)+1, rnd.nextInt(6)+1};
            // roll = new int[]{6,6};
            
            if(roll[0] == roll[1]){
                int value = roll[0];
                roll = new int[4];
                Arrays.fill(roll, value);
            }
            
            rollStr = roll[0]+" "+roll[1];
            hasMoves = true;
            
            repaint();
            
            
            
            checkForValidMove();
                        
        }
        
        public void manageClick(int id){
            
            BackgammonPanel panel = panelMap.get(id);
            
            if(id >= MY_JAIL_INDEX && id <= MY_GOAL_INDEX && hasMoves){
                
                if(myJail.hasPiece() && panel.isHighlighted()){
                    
                    hideMoves(MY_JAIL_INDEX);
                    myJail.deselect();
                    movePiece(currentSelection, id);
                    
                }
                else if(panel.isHighlighted()){
                
                    panelMap.get(currentSelection).deselect();
                    hideMoves(currentSelection);
                        
                    if(id < MY_GOAL_INDEX){
                        movePiece(currentSelection, id);
                    }
                    else{
                        handleGoalMoveFrom(currentSelection);
                    }
                    
                    currentSelection = CURRENT_SELECTION_VALUE;
                    
                }
                else if(panel.pieceCount() > 0 && panel.pieceColor().equals(myColor) && id < MY_GOAL_INDEX){
                
                    if(id == currentSelection){
                    
                        panel.deselect();
                        currentSelection = CURRENT_SELECTION_VALUE;
                        hideMoves(id);
                        
                    }
                    else if(currentSelection < -1){
                        panel.select();
                        currentSelection = id;
                        showMoves(id);
                    }
                    else{
                    
                        panelMap.get(currentSelection).deselect();
                        hideMoves(currentSelection);
                        
                        panel.select();
                        currentSelection = id;
                        showMoves(id);
                        
                    }
                    
                }
                
            }
            
        }
        
        private void movePiece(int giverID, int takerID){
        
            
            
            panelMap.get(giverID).removePiece();
            
            
            if(myTurn){
                
                currentMove += translateMove(giverID, takerID)+"&";
                
                if(panelMap.get(takerID).pieceColor().equals(theirColor)){
                    capture(takerID, THEIR_JAIL_INDEX, theirColor);
                }
                
                panelMap.get(takerID).addPiece(myColor);
                
                refreshBoard();
                
                useMove(takerID-giverID);
            }
            else{
                
                if(panelMap.get(takerID).pieceColor().equals(myColor)){
                    capture(takerID, MY_JAIL_INDEX, myColor);
                }
                
                panelMap.get(takerID).addPiece(theirColor);
                
            }
            
            
            
            
            
        }
        
        private void handleGoalMoveFrom(int id){
        
            currentMove += translateMove(id, MY_GOAL_INDEX)+"&";
        
            myGoal.addPiece(myColor);
            panelMap.get(id).removePiece(); 
            
            int leastMove = -1;
            
            if(roll.length > 2){ //Doubles, mostly to handle double 1s
            
                for(int i = 0; i < roll.length; i++){
                
                    if( (i+1)*roll[0] + id >= MY_GOAL_INDEX ){
                    
                        useMove((i+1)*roll[0]);
                        break;
                        
                    }
                    
                }
            
            }
            else if(roll.length == 2){
            
                boolean oneEnough = false;
                int index = 0;
                
                for(int move: roll){
                
                    if(move+id >= MY_GOAL_INDEX){
                    
                        oneEnough = true;
                        
                        if(leastMove == -1 || move < roll[leastMove]){
                            leastMove = index;
                        }
                        
                    }
                    index++;
                    
                }
                
                if(!oneEnough){
                    useMove(roll[0]+roll[1]);
                }
                else{
                    useMove(roll[leastMove]);
                }
                
            }
            else{
                useMove(roll[0]);
            }
            
            refreshBoard();
            
            if(myGoal.pieceCount() == 15){
                client.endGame(); //End the game with you being the winner
            }
        }
        
        private void useMove(int distance){
        
            int len = roll.length;
            // System.out.println("\nUsed move of value "+distance);
            
            if(len > 2){
                int i = roll[0];
                if(distance/i == len){
                    endRoll();
                }
                else{
                    roll = new int[len-(distance/i)];
                    Arrays.fill(roll, i);
                }
                // System.out.println("\nDouble found. New length is "+(len-(distance/i)));
            }
            else if (len == 2){
                if(distance > 6 || distance == roll[0]+roll[1]){
                    endRoll();
                }
                else{
                    roll = new int[]{roll[0]==distance? roll[1] : roll[0]};
                }
            }
            else{
                endRoll();
                return;
            }
            
            checkForValidMove();
            
        }
        
        private void endRoll(){
            
            roll = new int[2];
            hasMoves = false;
            rollStr = "";
            refreshBoard();
            
            myTurn = false;
            updateControlPanel(infoLabel);
            
            System.out.println("Current move = "+currentMove);
            client.sendMove(currentMove);
            
        }
        
        private void endGame(boolean iWin){
        
            hasMoves = false;
            updateControlPanel(infoLabel);
            refreshBoard();
            JOptionPane.showMessageDialog(null,"You "+(iWin? "WIN!\nYou are awesome ;)" : "LOSE! \nLoser!!!!!!!!!!!!"));
            
            
        }
        
        public void showMoves(int id){
        
        
            int sum = 0, position;
            boolean goalOpen = enoughPiecesPast(), hasOneLegalMove = false, isDoubleRoll = roll.length > 1 && roll[0] == roll[1];
            // System.out.println("\n\n\n\nShowing moves for spike id "+id);
            
            
            for(int move: roll){
            
                position = id+move;
                // System.out.println("\n\tNow examning move value of "+move+" giving final position of "+position);
                
                if (hasMoves){
                
                    if(position < MY_GOAL_INDEX && isMoveLegal(position)){
                        
                        panelMap.get(position).highlight();
                        hasOneLegalMove = true;
                        // System.out.println("\n\t\t"+position+" is a legal move");
                        
                    }
                    else if(position >= MY_GOAL_INDEX && goalOpen){
                    
                        myGoal.highlight();
                        hasOneLegalMove = true;
                        // System.out.println("\n\t\tThe goal is a legal move");
                    
                    }
                    
                    if(!myJail.hasPiece()){
                        //Check sums
                        sum += move;
                        position = id+sum;
                        
                        // System.out.println("\n\tNow examning sum value of "+sum+" giving final position of "+position);
                        
                        if(position < 24 && isMoveLegal(position) && sum != move && hasOneLegalMove){
                        
                            panelMap.get(position).highlight();
                            // System.out.println("\n\t\t"+position+" is a legal move");
                        
                        }
                        else if(isDoubleRoll && !isMoveLegal(position)){
                            break;
                        }
                        
                        
                        if(position >= MY_GOAL_INDEX && goalOpen && hasOneLegalMove){
                            
                            myGoal.highlight();
                            hasOneLegalMove = true;
                            // System.out.println("\n\t\tThe goal is a legal move");
                            
                        }
                    }
                }
                
            }
            
        }
        
        private boolean isMoveLegal(int id){
        
            if(id > MY_GOAL_INDEX || id < MY_JAIL_INDEX){
                return false;
            }
            
            return panelMap.get(id).pieceColor().equals(myColor) || panelMap.get(id).pieceCount() < 2;
        }
        
        private boolean hasAnotherMove(){
            
            // if(!hasMoves){
                // return false;
            // }
            
            if(myJail.hasPiece()){
            
                for(int move: roll){
                    if(isMoveLegal(move+MY_JAIL_INDEX)){
                        return true;
                    }
                }
                
                return false;
            }
            
            boolean isGoalOpen = enoughPiecesPast(), isDoubleRoll = roll.length > 1 && roll[0] == roll[1];
            
            for(BackgammonPanel panel: panelMap.values()){
            
                if(panel.equals(myGoal)){
                    continue;
                }
            
                if(panel.pieceColor().equals(myColor) && panel.pieceCount() > 0){
                
                    int sum = 0, id = panel.getID();
                    
                    for(int move: roll){
                    
                        int position = id+move;
                        
                        if(position < MY_GOAL_INDEX && isMoveLegal(position)){
                            // System.out.println("\n\t\t\t\tMove found for "+id+" at "+position);
                            return true;
                        }
                        else if(position >= MY_GOAL_INDEX && isGoalOpen){
                            // System.out.println("\n\t\t\t\tMove found for "+id+" at "+position);
                            return true;
                        }
                    }
                    
                }
                
            }
            
            
            return false;
        }
        
        private void hideMoves(int id){
            
            int sum = 0;
            for(int move: roll){
            
                if(id+move < MY_GOAL_INDEX){
                    panelMap.get(id+move).unhighlight();
                }
                else{
                    myGoal.unhighlight();
                }
                
                sum += move;
                if(sum != move ){
                    if(id+sum < MY_GOAL_INDEX){
                        panelMap.get(id+sum).unhighlight();
                    }
                    else{
                        myGoal.unhighlight();
                    }
                }
                
            }
            
            
        }
        
        private boolean enoughPiecesPast(){
        
            int sum = 0;
            for(int i = 18; i <= MY_GOAL_INDEX; i++){
            
                BackgammonPanel panel = panelMap.get(i);
                if(panel.pieceColor().equals(myColor)){
                    sum += panel.pieceCount();
                }
                
            }
            
            return sum > 14;
        
        }
        
        private void checkForValidMove(){
            
            if(!hasAnotherMove()){
                endRoll();
                JOptionPane.showMessageDialog(null, "You have no available moves! \nYour turn is over.");
            }
            else if(myJail.hasPiece()){
                useJail();
            }
            else{
                freeJail();
            }
            
        }
        
        private void useJail(){
            
            myJail.select();
            currentSelection = MY_JAIL_INDEX;
            showMoves(MY_JAIL_INDEX);
            
        }
        
        private void freeJail(){
        
            myJail.deselect();
            currentSelection = CURRENT_SELECTION_VALUE;
            hideMoves(MY_JAIL_INDEX);
        
        }
        
        public void refreshBoard(){
            this.revalidate();
            this.repaint();
        }
    
        private void capture(int id, int jail, Color c){
        
            panelMap.get(id).removePiece();
            panelMap.get(jail).addPiece(c);
        
        }
    
    }

}