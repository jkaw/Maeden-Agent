
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Random;


/**
 * class MemoryAgent: Provides a Keyboard user-interface to a Grid world simulation.
 * Currently, only provides smell direction to the food, agent inventory, visual picture,
 * and ground contents of current location.  Accepts commands from the keyboard and communicates
 * with the Grid simulation.  Other agents in field of view appear as a four-pointed star
 * since the heading is not reported by the server.
 * 
 *@author:  Josh Holm, Wayne Iba
 *@date:    2-25-12
  @version: Beta 0.2
 */
public class MemoryAgent extends Frame {
    
    //private data field
    private Socket gridSocket;				// socket for communicating w/ server
    private PrintWriter gridOut;                        // takes care of output stream for sockets
    private BufferedReader gridIn;			// bufferedreader for input reading
    private String myID;
    private static final int MAEDENPORT = 7237;         // uses port 1237 on localhost
    private Insets iTrans;
    private LinkedList visField;                    // stores GOB's for painting the visual field
    private boolean termOut = false;
    private String memorymap[][] = new String[50][50];
    private String agentState = "React";
    private int vheading = 25;
    private int hheading = 25;
    private String lastdirection= "";
    private String sensoryinfo[][] = new String[7][5];
    private String compass = "n";
    private ArrayList<Point> lastfourmove = new ArrayList<Point>();
    private Point cheese;
    private Point door;
    private Point key;
    private Point boulder;
    private Point hammer;

    /**
     * MemoryAgent constructor takes a string and an int
     * and creates a socket and connects with a serverSocket
     * PRE: h is a string and p is an int (preferably above 1024)
     * POST: GridClient connects to Grid via network sockets
     */
    public MemoryAgent(String h, int p) {
        registerWithGrid(h, p);      //connect to the grid server socket
	visField = new LinkedList(); //the visual field contents will be held in linked list
	setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	iTrans = getInsets();
	setTitle("Manual Agent: " + myID);                          //window title
	//setTitle("Agent View");                          //window title for generating figure for paper
	
	

	setVisible(true);
    }

    
    /**
     * registerWithGrid takes a string and an int
     * and creates a socket with the specified network name and port number
     * PRE: h is the name of the machine on the network, p is the port number of the server socket
     * POST: socket connects with the server socket on the given host
     */
    public void registerWithGrid(String h, int p) {
        try {
	    // connects to h machine on port p
            gridSocket = new Socket(h, p);

	    // create output stream to communicate with grid
            gridOut = new PrintWriter(gridSocket.getOutputStream(), true); 
	    gridOut.println("base"); // send role to server

	    //buffered reader reads from input stream from grid
            gridIn = new BufferedReader(new InputStreamReader(gridSocket.getInputStream()));
	    myID = gridIn.readLine(); // read this agent's ID number
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + h);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + h);
            System.exit(1);
        }
    }
 
    /**
     * removedub will take a string a remove all instances of a char except for the one indicated
     * ie. if the string "hello world", the int 3, and the char 'l' were given, the output would be "heo world"
     * @param s is a String which you are removing duplicates from
     * @param c is the char you are removing
     * @param keep is the number of the char you are keeping.
     */
    
    
    
    public static String removedub(String s, char c, int keep) {
		String a = s;
		String tmpa = "";
		String tmpb = "";
		int length = s.length();
		int counta = 1;
		int countb = 0;
		for(int i = 1; i <= a.length(); i++) {
			if (counta != keep && a.charAt(i - 1) == c) {
				tmpa = a.substring(0, i - 1);
				tmpb = a.substring(i, length);
				if (tmpa.equals(" ")) a = tmpb;
				else {a = tmpa + tmpb;}
				//System.out.println(a);
				length -= 1;
				counta += 1;
				//i = 1;
			}
			else if (counta == keep && a.charAt(i - 1) != c) {}
			
			else if (counta == keep && a.charAt(i - 1) == c) {
				counta += 1;
			}
		}
		return findit(a);
		//System.out.println(a);
	}
    
    /**
     * absDirToPt: compute the absolute (compass) direction to a given target point
     * with respect to a given reference point
     * INPUT: agent point, target location
     * OUTPUT: char: one of N(orth), S(outh), E(ast) or W(est) or H(ere)
     */
    public char absDirToPt(Point aPt, Point target){
	int xdiff = aPt.x - target.x;             //difference in x and y directions
	int ydiff = aPt.y - target.y;
	if (xdiff == 0 && ydiff == 0) return 'H'; //if no difference, point is here
	if (Math.abs(xdiff) > Math.abs(ydiff)){   // absolute heading either E or W
	    if (xdiff > 0) return 'W';
	    else return 'E';
	} else {
	    if (ydiff > 0) return 'N';
	    else return 'S';
	}
    }
    
    /**
     * totalrepeat counts the amount of times a char appears in a String
     * @param s is the String you are iterating over
     * @param a is the char you are counting
     */
    public static int totalrepeat(String s, char a) {
		int count = 0;
		for (int i = 0; i < s.length(); i++){
			if (s.charAt(i) == (a)){
				count += 1;
			}
		}
		return count;
		//System.out.println(count);
	}
    /**
     * findit takes a String and removes all quotes that are just standing there alone.
     * @param s the string to iterate over
     * @return it returns a new string without quotes without anything between them
     */
    public static String findit(String s) {
		String a = s;
		String tmpa = " ";
		String tmpb = " ";
		int length = s.length();
		for (int i = 1; i <= length; i++){
			if (a.charAt(i - 1) == '\"' && (i - 1) <a.length() && a.charAt(i) == '\"'){
				tmpa = a.substring(0, i - 1);
				tmpb = a.substring(i + 1);
				a = tmpa + tmpb;
				length -= 2;
			}
			
		}
		return a;
		//System.out.println(a);
	}
    
    
    /**
     * findanystring takes a String and removes all occurrences of a char
     * @param s the string to iterate over
     * @param c is the char to delete
     * @return it returns a new string without the char
     */
    public static String findanystring(String s, char c) {
    	String a = s;
    	String tmpa = " ";
    	String tmpb = " ";
    	int length = a.length();
    	for (int i = 1; i <= length; i++){
    		if (a.charAt(i - 1) == c){
    			tmpa = a.substring(0, i - 1);
    			tmpb = a.substring(i);
    			a = tmpa + tmpb;
    			length -= 1;
    			i -= 1;
    		}
    	}
    	return a;

    }
    
   /**
    * This function takes the 2d memory map and replaces all the nulls with an underscore. Easier on the eyes.
    */
    public void fillmap(){
    	for (int i = 0; i < memorymap.length; i ++){
    		for (int j = 0; j < memorymap[0].length; j ++){
    			memorymap[i][j] = "_";
    		}
    	}
    	
    }
  
    
    
    
    
    /**
     * simplestring will take the ground sensory info and either return empty or just the symbol on the ground
     * @param s the ground sensory info
     * @return the string returned
     */
    public String simplestring(String s) {
    	String a = "";
    	for (int i = 0; i < s.length(); i ++) {
    		if (s.charAt(i) == '(' && s.charAt(i + 1) == ')'){
    			a += "e";
    		}
    		if (s.charAt(i) != '(' && s.charAt(i) != ')' && s.charAt(i) != '\"'){
    			a += s.charAt(i);
    		}
    	}
    	return a;
    }
    
    /**
     * replacestring takes a string of sensory data and turns it into a 7x5 2d array
     * @param s the string to turn into an array
     * @return returns the 2d array of sensory info.
     */
    public String[][] replacestring(String s, String g) {
    	String asdf[][] = new String[7][5]; 
    	int j = 0;
    	int k = 0;
    	
    for (int i = 0; i < s.length(); i++) {
    	if (k == 5) {
    		k = 0;
    		j += 1;
    	}
    	if (j == 7) break;
    	if (s.charAt(i) == '(' && s.charAt(i+1) == ')'){
    		asdf[j][k] = "e";
    		k += 1;
    	}
    	if (s.charAt(i) == "0".charAt(0)){
    		asdf[j][k] = simplestring(g);
    		k += 1;
    	}
    	
    	if (s.charAt(i) != '(' && s.charAt(i) != '"' && s.charAt(i) != ' ' && s.charAt(i) != ')' && s.charAt(i) != "0".charAt(0)){
    		asdf[j][k] = Character.toString(s.charAt(i));
    		k += 1;
    	}
    	
    }
    return asdf;

    }   
    
    /**
     * printwod is simply for printing out the array in a visually pleasing manner
     * @param s the 2d array to print out.
     */
    public static void Printtwod(String[][] s) {
    	for(int i = 0; i < s.length; i ++){
    		for(int j = 0; j < s[0].length; j ++) {
    			System.out.print(s[i][j]);
    		}
    		System.out.println();
    	}
    		
    }
    
    
  /**
   * addToMap takes the 2d array of the sensory info and adds it to the bigger memory map 2d array for a world map.  
   * @param map the 2d array that contains the sensory information the agent has just recieved.
   */
public void addToMap(String[][] map ) {
    	
    	if (compass.equals("n")){
    		int l = 0;
        	int m = 6;
      
        	
        	
    		for (int i = vheading - 5; i <= vheading + 1; i ++){
    			for (int j = hheading - 2; j <= hheading + 2; j++){
    				memorymap[i][j] = sensoryinfo[m][l]; 
    				l++;
    				if (l == 5) {
    	        		l = 0;
    	        		m -= 1;
    	        	}
    			}
    		}
    		
    	}
    	
    	else if (compass.equals("e")){
    		int l = 0;
    		int m = 0;
    		
    		for (int i = vheading - 2; i <= vheading + 2; i ++){
    			for (int j = hheading - 1; j <= hheading + 5; j ++ ){
    				memorymap[i][j] = sensoryinfo[l][m];
    				l++;
    				if (l == 7) {
    	        		l = 0;
    	        		m += 1;
    	        	}
    			}
    		}
    		
    	}
    	
    	else if (compass.equalsIgnoreCase("w")){
    		int l = 0;
    		int m = 4;
    		
    		for (int i = vheading - 2; i <= vheading + 2; i ++){
    			for (int j = hheading - 5; j <= hheading + 1 ; j ++){
    				memorymap[i][j] = sensoryinfo[l][m];
    				l++;
    				if (l == 7) {
    	        		l = 0;
    	        		m -= 1;
    	        	}
    			}
    		}
    	}
    	
    	else if (compass.equals("s")){
    		int l = 0;
        	int m = 4;
        	
        	
    		for (int i = vheading - 1; i <= vheading + 5; i ++){
    			for (int j = hheading - 2; j <= hheading + 2; j++){
    				memorymap[i][j] = sensoryinfo[l][m]; 
    				m--;
    				if (m == -1) {
    	        		l += 1;
    	        		m = 4;
    			}
    		}
    		
    	}
    	}
	
    	
    }

public void clearlist(){
	if (lastfourmove.size() == 4){
		if (lastfourmove.get(0).equals(lastfourmove.get(1)) && lastfourmove.get(1).equals(lastfourmove.get(2)) &&
				lastfourmove.get(2).equals(lastfourmove.get(3))){
			agentState = "Explore";
			
		}
		else if (lastfourmove.size() == 4) lastfourmove.clear();
		
	}
	else {
		lastfourmove.add(new Point(hheading, vheading));
	}
}


//door is #
//key is K
//Hammer is T
//boulder is @


public boolean contains(String s) {
    int rows = memorymap.length;
    int cols = memorymap[0].length;
    for (int row=0; row<rows; row++)
      for (int col=0; col<cols; col++)
        if (memorymap[row][col].equals(s)){
          if (s.equals("+")) cheese = new Point(row, col);
          if (s.equals("K")) key = new Point(row, col);
          if (s.equals("#")) door = new Point(row, col);
          if (s.equals("@")) boulder = new Point(row, col);
          if (s.equals("T")) hammer = new Point(row, col);
          return true;
        }
    return false;
  }




//public boolean pathtocheese(){
	//if (contains("+") == true){
		//if (contains("#")) return false;
		//if (contains("@")) return false;
			
//	}
	
	//else return false;
//}
public boolean contains(String s,
        int startRow, int startCol) {
int rows = memorymap.length;
int cols = memorymap[0].length;
for (int dRow=-1; dRow<=1; dRow++)
  for (int dCol=-1; dCol<=1; dCol++)
    if (((dRow != 0) || (dCol != 0)) &&
        (contains(s,startRow,startCol,dRow,dCol)))
      return true;
return false;
}

// Returns true if the given board contains the given string,
// starting from the given startRow and startCol location,
// heading in the given drow,dcol direction.
public boolean contains(String s,
                             int startRow, int startCol,
                             int dRow, int dCol) {
int rows = memorymap.length;
int cols = memorymap[0].length;
for (int i=0; i<s.length(); i++) {
  int row = startRow + i*dRow;
  int col = startCol + i*dCol;
  if ((row < 0) || (row >= rows) || (col < 0) || (col >= cols))
    // we're off the board, so we did not match
    return false;
  if (memorymap[row][col] != String.valueOf(s.charAt(i)))
    // we're on the board, but we don't match
    return false;
}
return true;
}













public void exploreMode(String heading, String info, String inventory, String ground){
	if (contains("#") && contains("K") == false && inventory.equals("(\"K\")") == false){
		String[] lr = new String[2];
		 Random r = new Random();
		   lr[0] = "r";
		   lr[1] = "l";
		   int mycount = totalrepeat(info, '*');
			ArrayList<String> tmp = new ArrayList<String>();
			for(int i = 1; i <= mycount; i ++){
		    	String tmpa;
		    	tmpa = removedub(info, '*', i);
		    	int space = tmpa.indexOf("*") - tmpa.indexOf(String.valueOf(0));
		    	tmp.add(String.valueOf(space));
		    } 
		   
		   if (ground.equals("(\"K\")")) //&& inventory.equals("(\"+\")")) {
		    	{sendEffectorCommand("g");
		    }
		    
		    else if (tmp.contains(String.valueOf(16)) && (tmp.contains(String.valueOf(35))) && heading.equals("left")) {
				sendEffectorCommand("r");
			}
		    
		    else if (tmp.contains(String.valueOf(16)) && (tmp.contains(String.valueOf(35))) && heading.equals("forward")) {
				sendEffectorCommand("l");
			}
		    
		    else if (tmp.contains(String.valueOf(22)) && (tmp.contains(String.valueOf(35))) && (heading.equals("right") || heading.equals("forward"))) {
				sendEffectorCommand("l");
			}
		    
		    else if (tmp.contains(String.valueOf(22)) && (tmp.contains(String.valueOf(35))) && (heading.equals("forward") || heading.equals("forward"))) {
				sendEffectorCommand("r");
			}
		    
		    else if (tmp.contains(String.valueOf(-6)) && heading.equals("left")) {
				sendEffectorCommand("b");
			}
		    
		    else if (tmp.contains(String.valueOf(-6)) && heading.equals("left")) {
				sendEffectorCommand("b");
			}
		    
		    else if (tmp.contains(String.valueOf(6)) && heading.equals("right")) {
				sendEffectorCommand("b");
			}
		    
		    else if (tmp.contains(String.valueOf(19)) && heading.equals("forward")) {
				sendEffectorCommand(lr[r.nextInt(2)]);
			}
		    
		    else if ((tmp.contains(String.valueOf(9)) || (tmp.contains(String.valueOf(6)))) && (heading.equals("right") || heading.equals("forward"))) {
				sendEffectorCommand("b");
			}
		    
		    else if ((tmp.contains(String.valueOf(-9)) || (tmp.contains(String.valueOf(-6)))) && (heading.equals("left") || heading.equals("forward"))) {
				sendEffectorCommand("b");
			}
		    
		    	
		    
		    else if (heading.equals("back")) {
		    	sendEffectorCommand("l");
		    }
		    else if (heading.equals("forward")) {
		    	sendEffectorCommand("b");
		    }
		    else if (heading.equals("left")) {
		    	sendEffectorCommand("r");
		    }
		    else if (heading.equals("right")) {
		    	sendEffectorCommand("l");
		    }
	}
	
	
	
	if (contains("#") && contains("K")){
		reactivemovecustom(direction(absDirToPt(new Point(hheading, vheading), key)), inventory, info, "g");
	}
	if (contains("#") && inventory.equals("(\"K\")")){
		reactivemovecustom(direction(absDirToPt(new Point(hheading, vheading), door)), inventory, info, "u");
		agentState = "React";
	}
	if (contains("@") && contains("T") == false && inventory.equals("(\"T\")") == false){
		//reactivemovecustom(direction(absDirToPt(new Point(hheading, vheading), key)), inventory, info, "g");
	
		String[] lr = new String[2];
		 Random r = new Random();
		   lr[0] = "r";
		   lr[1] = "l";
		   int mycount = totalrepeat(info, '*');
			ArrayList<String> tmp = new ArrayList<String>();
			for(int i = 1; i <= mycount; i ++){
		    	String tmpa;
		    	tmpa = removedub(info, '*', i);
		    	int space = tmpa.indexOf("*") - tmpa.indexOf(String.valueOf(0));
		    	tmp.add(String.valueOf(space));
		    } 
		   
		   if (ground.equals("(\"T\")")) //&& inventory.equals("(\"+\")")) {
		    	{sendEffectorCommand("g");
		    }
		    
		    else if (tmp.contains(String.valueOf(16)) && (tmp.contains(String.valueOf(35))) && heading.equals("left")) {
				sendEffectorCommand("r");
			}
		    
		    else if (tmp.contains(String.valueOf(16)) && (tmp.contains(String.valueOf(35))) && heading.equals("forward")) {
				sendEffectorCommand("l");
			}
		    
		    else if (tmp.contains(String.valueOf(22)) && (tmp.contains(String.valueOf(35))) && (heading.equals("right") || heading.equals("forward"))) {
				sendEffectorCommand("l");
			}
		    
		    else if (tmp.contains(String.valueOf(22)) && (tmp.contains(String.valueOf(35))) && (heading.equals("forward") || heading.equals("forward"))) {
				sendEffectorCommand("r");
			}
		    
		    else if (tmp.contains(String.valueOf(-6)) && heading.equals("left")) {
				sendEffectorCommand("b");
			}
		    
		    else if (tmp.contains(String.valueOf(-6)) && heading.equals("left")) {
				sendEffectorCommand("b");
			}
		    
		    else if (tmp.contains(String.valueOf(6)) && heading.equals("right")) {
				sendEffectorCommand("b");
			}
		    
		    else if (tmp.contains(String.valueOf(19)) && heading.equals("forward")) {
				sendEffectorCommand(lr[r.nextInt(2)]);
			}
		    
		    else if ((tmp.contains(String.valueOf(9)) || (tmp.contains(String.valueOf(6)))) && (heading.equals("right") || heading.equals("forward"))) {
				sendEffectorCommand("b");
			}
		    
		    else if ((tmp.contains(String.valueOf(-9)) || (tmp.contains(String.valueOf(-6)))) && (heading.equals("left") || heading.equals("forward"))) {
				sendEffectorCommand("b");
			}
		    
		    	
		    
		    else if (heading.equals("back")) {
		    	sendEffectorCommand("l");
		    }
		    else if (heading.equals("forward")) {
		    	sendEffectorCommand("b");
		    }
		    else if (heading.equals("left")) {
		    	sendEffectorCommand("r");
		    }
		    else if (heading.equals("right")) {
		    	sendEffectorCommand("l");
		    }
	}

	if (contains("@") && contains("T")){
		reactivemovecustom(direction(absDirToPt(new Point(hheading, vheading), hammer)), inventory, info, "g");
	}

	if (contains("@") && inventory.equals("(\"T\")")){
		reactivemovecustom(direction(absDirToPt(new Point(hheading, vheading), boulder)), inventory, info, "u");
	agentState = "React";
	}
	else {
	 } 
	String[] lr = new String[2];
	 Random r = new Random();
	   lr[0] = "r";
	   lr[1] = "l";
	   int mycount = totalrepeat(info, '*');
		ArrayList<String> tmp = new ArrayList<String>();
		for(int i = 1; i <= mycount; i ++){
	    	String tmpa;
	    	tmpa = removedub(info, '*', i);
	    	int space = tmpa.indexOf("*") - tmpa.indexOf(String.valueOf(0));
	    	tmp.add(String.valueOf(space));
	   if (ground.equals("(\"K\")")) //&& inventory.equals("(\"+\")")) {
	    	{sendEffectorCommand("g");
	    }
	    
	    else if (tmp.contains(String.valueOf(16)) && (tmp.contains(String.valueOf(35))) && heading.equals("left")) {
			sendEffectorCommand("r");
		}
	    
	    else if (tmp.contains(String.valueOf(16)) && (tmp.contains(String.valueOf(35))) && heading.equals("forward")) {
			sendEffectorCommand("l");
		}
	    
	    else if (tmp.contains(String.valueOf(22)) && (tmp.contains(String.valueOf(35))) && (heading.equals("right") || heading.equals("forward"))) {
			sendEffectorCommand("l");
		}
	    
	    else if (tmp.contains(String.valueOf(22)) && (tmp.contains(String.valueOf(35))) && (heading.equals("forward") || heading.equals("forward"))) {
			sendEffectorCommand("r");
		}
	    
	    else if (tmp.contains(String.valueOf(-6)) && heading.equals("left")) {
			sendEffectorCommand("b");
		}
	    
	    else if (tmp.contains(String.valueOf(-6)) && heading.equals("left")) {
			sendEffectorCommand("b");
		}
	    
	    else if (tmp.contains(String.valueOf(6)) && heading.equals("right")) {
			sendEffectorCommand("b");
		}
	    
	    else if (tmp.contains(String.valueOf(19)) && heading.equals("forward")) {
			sendEffectorCommand(lr[r.nextInt(2)]);
		}
	    
	    else if ((tmp.contains(String.valueOf(9)) || (tmp.contains(String.valueOf(6)))) && (heading.equals("right") || heading.equals("forward"))) {
			sendEffectorCommand("b");
		}
	    
	    else if ((tmp.contains(String.valueOf(-9)) || (tmp.contains(String.valueOf(-6)))) && (heading.equals("left") || heading.equals("forward"))) {
			sendEffectorCommand("b");
		}
	    
	    	
	    
	    else if (heading.equals("back")) {
	    	sendEffectorCommand("l");
	    }
	    else if (heading.equals("forward")) {
	    	sendEffectorCommand("b");
	    }
	    else if (heading.equals("left")) {
	    	sendEffectorCommand("r");
	    }
	    else if (heading.equals("right")) {
	    	sendEffectorCommand("l");
	    }
	}
	
	
	
	
	
	
	
	
}

















    /**
     * sendEffectorCommand sends the specified command to the grid
     * *NOTE: GOBAgent only looks at first letter of command string unless talk or shout is sent*
     * pre: command is either f, b, l, r, g, u, d, "talk" + message, or "shout" + message
     * post: command is sent via the printwriter
     */
    public void sendEffectorCommand(String command) {
	gridOut.println(command);
	lastdirection = command;
	if (lastdirection.equals("f") && compass.equals("n")){vheading -= 1;}
	else if (lastdirection.equals("f") && compass.equals("w"))
	{
		hheading -=1; clearlist();}
	else if (lastdirection.equals("f") && compass.equals("s")){vheading += 1;clearlist();}
	else if (lastdirection.equals("f") && compass.equals("e")){hheading += 1;clearlist();}
	else if (lastdirection.equals("b") && compass.equals("n")){vheading += 1;clearlist();}
	else if (lastdirection.equals("b") && compass.equals("w")){hheading += 1;clearlist();}
	else if (lastdirection.equals("b") && compass.equals("s")){vheading -= 1;clearlist();}
	else if (lastdirection.equals("b") && compass.equals("e")){hheading -= 1;clearlist();}
	else if (lastdirection.equals("r") && compass.equals("n")){compass = "e";clearlist();}
	else if (lastdirection.equals("r") && compass.equals("s")){compass = "w";clearlist();}
	else if (lastdirection.equals("r") && compass.equals("e")){compass = "s";clearlist();}
	else if (lastdirection.equals("r") && compass.equals("w")){compass = "n";clearlist();}
	else if (lastdirection.equals("l") && compass.equals("s")){compass = "e";clearlist();}
	else if (lastdirection.equals("l") && compass.equals("n")){compass = "w";clearlist();}
	else if (lastdirection.equals("l") && compass.equals("w")){compass = "s";clearlist();}
	else if (lastdirection.equals("l") && compass.equals("e")){compass = "n";clearlist();}
    }
    
    
    public void reactivemovecustom(String heading, String inventory, String info, String use){
   	 String[] lr = new String[2];
   	 Random r = new Random();
   	   lr[0] = "r";
   	   lr[1] = "l";
   	   int mycount = totalrepeat(info, '*');
   		ArrayList<String> tmp = new ArrayList<String>();
   		for(int i = 1; i <= mycount; i ++){
   	    	String tmpa;
   	    	tmpa = removedub(info, '*', i);
   	    	int space = tmpa.indexOf("*") - tmpa.indexOf(String.valueOf(0));
   	    	tmp.add(String.valueOf(space));
   	    } 
   	   
   	 //  if (heading.equals("here!") && inventory.equals("(\"+\")")) {
   	   // 	sendEffectorCommand("u");
   	    //}
   	    
   	    if (tmp.contains(String.valueOf(16)) && (tmp.contains(String.valueOf(35))) && heading.equals("left")) {
   			sendEffectorCommand("l");
   		}
   	    
   	    else if (tmp.contains(String.valueOf(16)) && (tmp.contains(String.valueOf(35))) && heading.equals("forward")) {
   			sendEffectorCommand("r");
   		}
   	    
   	    else if (tmp.contains(String.valueOf(22)) && (tmp.contains(String.valueOf(35))) && (heading.equals("right") || heading.equals("forward"))) {
   			sendEffectorCommand("r");
   		}
   	    
   	    else if (tmp.contains(String.valueOf(22)) && (tmp.contains(String.valueOf(35))) && (heading.equals("forward") || heading.equals("forward"))) {
   			sendEffectorCommand("l");
   		}
   	    
   	    else if (tmp.contains(String.valueOf(-6)) && heading.equals("left")) {
   			sendEffectorCommand("f");
   		}
   	    
   	    else if (tmp.contains(String.valueOf(-6)) && heading.equals("left")) {
   			sendEffectorCommand("f");
   		}
   	    
   	    else if (tmp.contains(String.valueOf(6)) && heading.equals("right")) {
   			sendEffectorCommand("f");
   		}
   	    
   	    else if (tmp.contains(String.valueOf(19)) && heading.equals("forward")) {
   			sendEffectorCommand(lr[r.nextInt(2)]);
   		}
   	    
   	    else if ((tmp.contains(String.valueOf(9)) || (tmp.contains(String.valueOf(6)))) && (heading.equals("right") || heading.equals("forward"))) {
   			sendEffectorCommand("f");
   		}
   	    
   	    else if ((tmp.contains(String.valueOf(-9)) || (tmp.contains(String.valueOf(-6)))) && (heading.equals("left") || heading.equals("forward"))) {
   			sendEffectorCommand("f");
   		}
   	    
   	    	
   	    else if (heading.equals("here!")) {    	
   	    	sendEffectorCommand(use);
   	    }
   	    else if (heading.equals("back")) {
   	    	sendEffectorCommand("r");
   	    }
   	    else if (heading.equals("forward")) {
   	    	sendEffectorCommand("f");
   	    }
   	    else if (heading.equals("left")) {
   	    	sendEffectorCommand("l");
   	    }
   	    else if (heading.equals("right")) {
   	    	sendEffectorCommand("r");
   	    }
   }
    
    
/**
 * This is the reactive agent from the first project. It is one of the states of the agent.    
 * @param heading what way the cheese is
 * @param inventory what the agent has
 * @param info the sensory info
 */
public void reactivemove(String heading, String inventory, String info){
	 String[] lr = new String[2];
	 Random r = new Random();
	   lr[0] = "r";
	   lr[1] = "l";
	   int mycount = totalrepeat(info, '*');
		ArrayList<String> tmp = new ArrayList<String>();
		for(int i = 1; i <= mycount; i ++){
	    	String tmpa;
	    	tmpa = removedub(info, '*', i);
	    	int space = tmpa.indexOf("*") - tmpa.indexOf(String.valueOf(0));
	    	tmp.add(String.valueOf(space));
	    } 
	   
	   if (heading.equals("here!") && inventory.equals("(\"+\")")) {
	    	sendEffectorCommand("u");
	    }
	    
	    else if (tmp.contains(String.valueOf(16)) && (tmp.contains(String.valueOf(35))) && heading.equals("left")) {
			sendEffectorCommand("l");
		}
	    
	    else if (tmp.contains(String.valueOf(16)) && (tmp.contains(String.valueOf(35))) && heading.equals("forward")) {
			sendEffectorCommand("r");
		}
	    
	    else if (tmp.contains(String.valueOf(22)) && (tmp.contains(String.valueOf(35))) && (heading.equals("right") || heading.equals("forward"))) {
			sendEffectorCommand("r");
		}
	    
	    else if (tmp.contains(String.valueOf(22)) && (tmp.contains(String.valueOf(35))) && (heading.equals("forward") || heading.equals("forward"))) {
			sendEffectorCommand("l");
		}
	    
	    else if (tmp.contains(String.valueOf(-6)) && heading.equals("left")) {
			sendEffectorCommand("f");
		}
	    
	    else if (tmp.contains(String.valueOf(-6)) && heading.equals("left")) {
			sendEffectorCommand("f");
		}
	    
	    else if (tmp.contains(String.valueOf(6)) && heading.equals("right")) {
			sendEffectorCommand("f");
		}
	    
	    else if (tmp.contains(String.valueOf(19)) && heading.equals("forward")) {
			sendEffectorCommand(lr[r.nextInt(2)]);
		}
	    
	    else if ((tmp.contains(String.valueOf(9)) || (tmp.contains(String.valueOf(6)))) && (heading.equals("right") || heading.equals("forward"))) {
			sendEffectorCommand("f");
		}
	    
	    else if ((tmp.contains(String.valueOf(-9)) || (tmp.contains(String.valueOf(-6)))) && (heading.equals("left") || heading.equals("forward"))) {
			sendEffectorCommand("f");
		}
	    
	    	
	    else if (heading.equals("here!")) {    	
	    	sendEffectorCommand("g");
	    }
	    else if (heading.equals("back")) {
	    	sendEffectorCommand("r");
	    }
	    else if (heading.equals("forward")) {
	    	sendEffectorCommand("f");
	    }
	    else if (heading.equals("left")) {
	    	sendEffectorCommand("l");
	    }
	    else if (heading.equals("right")) {
	    	sendEffectorCommand("r");
	    }
}
 
    /**
     * getSensoryInfo gets the direcion to the food
     * LINE0: # of lines to be sent or one of: die, success, or End
     * LINE1: smell (food direction)
     * LINE2: inventory
     * LINE3: visual contents
     * LINE4: ground contents
     * LINE5: messages
     * LINE6: remaining energy
     * LINE7: lastActionStatus
     * LINE8: world time
     * pre: gridIn is initialized and connected to the grid server socket
     * post: heading stores direction to the food f, b, l, r, or h
     */
    public void getSensoryInfo() {
	try {
	    String status = gridIn.readLine().toLowerCase();
	    if((status.equals("die") || status.equals("success")) || status.equals("end")) {
		System.out.println("Final status: " + status);
		System.exit(1);
	    }
	    if ( ! status.equals("8") ){
		System.out.println("getSensoryInfo: Unexpected number of data lines - " + status);
		System.exit(1);
	    }

		
	    // 1: get the smell info
	    String heading = direction(gridIn.readLine().toCharArray()[0]);
	    // 2: get the inventory
	    String inventory = gridIn.readLine();
	    // 3: get the visual info
	    String info = gridIn.readLine();
	    
	    System.out.println(info);
	    // 4: get ground contents
	    String ground = gridIn.readLine();
	    // 5: get messages
	    String message = gridIn.readLine(); //CHECKS MESSAGES ****CHANGE****
	    // 6: energy
	    String energy = gridIn.readLine();
	    // 7: lastActionStatus
	    String lastActionStatus = gridIn.readLine();
	    // 8: world time
	    String worldTime = gridIn.readLine();
	   // System.out.println(info.indexOf("*") -info.indexOf("O"));
	    //System.out.println(info.indexOf("*"));
	    //System.out.println(info.indexOf(String.valueOf(0)));
	    
		
		//System.out.println(compass);
		//replacestring(info, ground);
		sensoryinfo = replacestring(info, ground);
		//Printtwod(sensoryinfo);
		addToMap(sensoryinfo);
		Printtwod(memorymap);
		
		if (agentState.equals("React")) {
			reactivemove(heading, inventory, info);
		}
		
		else if (agentState.equalsIgnoreCase("Explore")){
			
		}

	    //System.out.println(Arrays.deepToString(memorymap));
	    //System.out.println(tmp.contains(String.valueOf(19)));
	    // store or update according to the data just read. . . .
	    //System.out.println(vheading);
	   // System.out.println(hheading);
	    
	    
	    
	    
	    //System.out.println(starlessstrings);
	    //System.out.println("spacer");
	    //System.out.println(tmp.contains(String.valueOf(19)));
	  
	    
	    //System.out.println(compass);
	    //System.out.println(hheading);
	    //System.out.println(vheading);
	    
	}
	catch(Exception e) {}
    }

    /* processRetinalField: takes a string input from the Maeden server and converts it into the GridObjects
     * Pre: String info contains list of list of list of chars(?)
     * Post: visual raphical map is constructed
     */
    
    

    /**
     * direction  and returns a string to display in the terminal
     * pre: heading has char value f, b, l, r, or h
     * post: corresponding string is returned
     */
    public String direction(char h) {
	switch(h) {
	case 'f': return "forward";
	case 'b': return "back";
	case 'l': return "left";
	case 'r': return "right";
	case 'h': return "here!";
	}
	return "error with the direction";
    }
 
    /**
     * run iterates through program commands
     * pre: sockets are connected to each other
     * post: program is run and exited when the agent reaches the food
     * @throws InterruptedException 
     */
    public void run() throws InterruptedException {
    	fillmap();
    	getSensoryInfo();
      	//System.out.println("The food is " + heading + " " + direction());
	while(true) {
	    
	    getSensoryInfo();
	    //Thread.sleep(5000);
        }
    }
    
 


    /**
     * main: creates new gridclient w/ the name of the server machine and the port number
     * pre: none
     * post: the program is run
     * @throws InterruptedException 
     */
    public static void main(String [] args) throws InterruptedException {
	MemoryAgent client = new MemoryAgent("localhost", MAEDENPORT);
	client.run();
    }
    
    //-------------------------------------------------------------------------
 
    

	class GridDisplayListener implements ActionListener{
	    
	    PrintWriter gridOut;
	    TextField text;
	    
	    public GridDisplayListener(PrintWriter pw, TextField tf){
		gridOut=pw;
		text=tf;
		//System.out.println("initialized");
	    }

	    public void actionPerformed(ActionEvent e){
		CommandCheck check = new CommandCheck();
		String command = text.getText();
		command = check.validateCommand(command);
		gridOut.println(command);
		text.setText("");
		//System.out.println("SENT: " + text.getText().toLowerCase());
	    }
	    
	}
	
		
    

    public class CommandCheck {
	    
	private String commandString;   //stores the string that is sent in by the user
	    	   
	private void printHelp() {
	    System.out.println("Maeden help information");
	    System.out.println("Allowable commands are:");
	    System.out.println("f: move forward");
	    System.out.println("b: move backward");
	    System.out.println("r: turn right");
	    System.out.println("l: turn left");
	    System.out.println("g: grab an object in the current spot");
	    System.out.println("d: drop an object currently being carried");
	    System.out.println("u: apply a carried object (tool or food)");
	    System.out.println("a: attack an agent ahead");
	    System.out.println("w: wait");
	    System.out.println("k: remove yourself from world");
	    System.out.println("?: print this help information");
	}
	
	/**
	 * invalidCommand: string -> boolean
	 * In general, a command is invalid if its length is 0 (the user just pressed enter),
	 * or the command starts with an invalid letter (one that's not fbrldguwst).
	 * If the first letter of the command is g, if additional requirments are not met the command
	 * is invalid. A valid "g command" is of the form "g [item]" where [item] begins with +, k or t.   
	 */
	public boolean invalidCommand(String commandString)
	{
	    return (commandString.length() == 0
		    || "fbrldguwstka?".indexOf(commandString.substring(0,1)) < 0);
	}

	public String validateCommand(String commandString) {
	    try {
		    
		commandString=commandString.toLowerCase();
		while ( invalidCommand(commandString) )
		    {
			printHelp();
			return null;
		    }
	    }
	    catch(Exception e) {System.out.println("validateCommand: " + e);}
	    return commandString;
	}
    }


}
