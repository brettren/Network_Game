//BOARD MODULE
package player;

import java.util.*;

/**
 * A class that is extended by all Network players (human and machine). 
 * This is the player's internal representation of the board. 
 * This representation of the board is intended for the Game Tree Search Module to get and set contents on the board.  
 */

//Constructor for Board class - constructs an 8 by 8 2D array
 
public class Board {
    //square
    public static final int EMPTY = -1;
    public static final int BLACK = 0;
    public static final int WHITE = 1;

    //Directions
    private static final int UP_DIRECTION = 0;
    private static final int UP_RIGHT_DIRECTION = 1;
    private static final int RIGHT_DIRECTION = 2;
    private static final int DOWN_RIGHT_DIRECTION = 3;
    private static final int DOWN_DIRECTION = 4;
    private static final int DOWN_LEFT_DIRECTION = 5;
    private static final int LEFT_DIRECTION = 6;
    private static final int UP_LEFT_DIRECTION = 7;

    private int[][] gameBoard = new int[8][8];
    private int whitePiecesLeft = 10;
    private int blackPiecesLeft = 10;

    /**
     * Constructs a Board object, setting each spot on the board to EMPTY initially
     */
    public Board() {
		for (int x = 0; x<8; x++) {
			for (int y =0; y<8; y++) {
				gameBoard[x][y] = EMPTY;
			}
		}
	}
     
    /** This constructor duplicates a board.
    * It takes in a Board and duplicates its contents. 
    * @param b is the current Board we are duplicating.
    */
    public Board (Board b){
        for (int x = 0; x<8; x++) {
            for (int y =0; y<8; y++) {
                this.gameBoard[x][y] = b.gameBoard[x][y];
            }      
        }
        this.whitePiecesLeft = b.whitePiecesLeft;
        this.blackPiecesLeft = b.blackPiecesLeft;
    }

/**This method's purpose is to return if a coordinate is a corner on the board. 
 * It uses the static final constants declared in this class. 
     @param x - The x coordinate of the position. 
     @param y - The y coordinate of the position.
     @return true is the position is on a corner, false otherwise
      */
     
    private boolean isCorner(int x, int y){
        if (x == 0 && y == 7) return true;
        if (x == 0 && y == 0) return true;
        if (x == 7 && y == 7) return true;
        if (x == 7 && y == 0) return true;
        return false;
    }

    /**This method finds the neighbors of a coordinate. It returns the coordinate positions. 
    * @param x - The x coordinate of the position. 
    * @param y - The y coordinate of the posistion. 
    * @return an int array containing the positions of each neighbor
    */
    private int[][] neighbors(int x, int y){
        int[][] n = {
            {x,y+1}, 
            {x,y-1}, 
            {x+1,y+1}, 
            {x+1,y-1}, 
            {x+1,y},
            {x-1,y},
            {x-1,y-1},
            {x-1,y+1}
        };
        return n;
    }
 
    /**
    * Determines whether the move with create a cluster if a chip is moved there
    * @param m the Move being considered 
    * @param color the color of the chip being placed
    * @return true if a cluster is created, false otherwise
    */
    private boolean isClustered(Move m, int color){
        int count = 0;
        int[][] n = neighbors(m.x1, m.y1); // 以x1，y1为中点找周围8个neighbor  这里是在落下这个chip之前检查是否有两个的cluster
        if (m.moveKind == Move.ADD){
            for (int i = 0; i< 8; i++) { // 遍历外围一圈的8个neighbor
                if (getSquare(n[i][0], n[i][1]) == color) {  // 发现一个挨着的同色chip，还要再检查这个chip的neighbor
                    count++;
                    int[][] othersNeighbors = neighbors(n[i][0], n[i][1]);
                    for (int k = 0; k< 8; k++){
                        if (getSquare(othersNeighbors[k][0], othersNeighbors[k][1]) == color){
                            return true; // 一旦发现有二级neighbor就说明必然是cluster
                        }
                    }
                }   
            }
            if (count > 1) return true;  // 只要大于1，肯定是clustered
        }
        else if (m.moveKind == Move.STEP){
            gameBoard[m.x2][m.y2] = EMPTY;  // 假设先移走起始位置的chip，原来的位置变成empty
            Move addMove = new Move(m.x1, m.y1);  // 移动到新的位置
            boolean result = isClustered(addMove, color);  
            gameBoard[m.x2][m.y2] = color; // 因为这里只是为了用来判断结果，所以最后还要恢复原来位置的color
            return result;
        }
        return false;		
    }
 
 
 
    /** hasNetwork returns true if this board has a network with 6 or greater length. It otherwise 
    *returns false. It takes in a player because it is called on the board. It calls a recursive, private function travel
     that repeatedly 
    *@param player the player whose network is being determined
    *@return true if this board has a network with 6 or greater length or false otherwise
    * 
   */

    protected boolean hasNetwork (int playerColor) {
        if (playerColor == WHITE) {
            if (this.whitePiecesLeft > 4 || !inGoalArea(playerColor)) {  // 如果还没有6个以上的chip，或者goalarea还没有chip
                return false;
            }
        }
        if (playerColor == BLACK) {
            if (this.blackPiecesLeft > 4 || !inGoalArea(playerColor)) {
                return false;
            }
        }
        LinkedList<Coordinate> startGoals = this.goalPieces(playerColor, 0); // 得到一边goal的chips链表
        LinkedList<Coordinate> endGoals = this.goalPieces(playerColor, 7);  // 得到另一边goal的chips链表
        ListIterator<Coordinate> it = startGoals.listIterator();

        while (it.hasNext()){  // valid 表示 不是 null
            Coordinate chip = it.next();
            int[] startCoord = chip.getCoord();
            LinkedList<Coordinate> connections = currentConnections(startCoord);  
            LinkedList<Coordinate> visited = new LinkedList<Coordinate>();
            int piecesUsed = 1; // start算一个
            // 一个path必定是以startgoal作起始点的，所以尝试遍历每个startgoal里的chip
            if (travel(startCoord, piecesUsed, endGoals, connections, visited, -1)){  // 因为一开始没有方向，所以设为-1
                return true;  // 如果这个list满足network，那就返回true
            }
        }
        return false;
    }

    // check if the list contains the coord
    public boolean listContains(LinkedList<Coordinate> list, int[] coord){
        ListIterator<Coordinate> it = list.listIterator();
        while(it.hasNext()){
            Coordinate cur = it.next();
            if(cur.equals(coord)) return true;
        }
        return false;
    }

    // DFS

    // 要注意三点： 
    // 1. 探索到9个以上就停止
    // 2. 如果发现到了endgoal，检查是否满足6个以上的要求
    // 3. 检查是否重复使用了同一个chip作path
    private boolean travel(int[] startCoord, int piecesUsed, LinkedList<Coordinate> endGoals, LinkedList<Coordinate> connections, LinkedList<Coordinate> visited, int lastDirection){   // connections是startCoord周围可连接的chip 表示以startCoord为原点能够形成connections的chips的list
        if (piecesUsed > 10){ // 最多10个算一个path
            return false;
        }
        if (listContains(endGoals, startCoord)){ // 说明已经到达终点goal area
            if (piecesUsed > 5){   // 如果最后是endgoals，那就要看这个path是否满足6个chip以上的要求
                return true;
            }else{
                return false;
            }
        }
        if (listContains(visited, startCoord)){  // 如果visited path已经包含这个chip，就不可能形成path，同一个chip不能用两次
            return false;
        }
        
        ListIterator<Coordinate> it = connections.listIterator();
        while (it.hasNext()){
            Coordinate chip = it.next();
            int[] coord = chip.getCoord();
            int currentDirection = direction(startCoord, coord);
            if (currentDirection == lastDirection){  // 比较上一个connection的方向，说明方向一致不能形成下一个connection，跳过
                continue;
            }
            visited.add(new Coordinate(startCoord)); // 把这个chip尝试加入path，然后继续往下面search
            if (travel(coord, piecesUsed + 1, endGoals, currentConnections(coord), visited, currentDirection)) {
                return true;
            } else{  // 如果search结果是false，那就说明这个chip不能找到network，再把它remove掉
                visited.remove();         
            }
        }
        return false;
    }


    // Find the max path length on the board according to specified color, used to evaluate the score
    public int maxPathLength(int color){
        LinkedList<Coordinate> startGoals = this.goalPieces(color, 0); // 得到一边goal的chips链表
        LinkedList<Coordinate> endGoals = this.goalPieces(color, 7);  // 得到另一边goal的chips链表
        ListIterator<Coordinate> it = startGoals.listIterator();
        int max = 0;
        while (it.hasNext()){  // valid 表示 不是 null
            Coordinate chip = it.next();
            int[] startCoord = chip.getCoord();
            LinkedList<Coordinate> connections = currentConnections(startCoord);  
            LinkedList<Coordinate> visited = new LinkedList<Coordinate>();
            int piecesUsed = 1; // start算一个
            // 一个path必定是以startgoal作起始点的，所以尝试遍历每个startgoal里的chip
            int tmpMax = maxPathLength(startCoord, piecesUsed, endGoals, connections, visited, -1);
            if (max < tmpMax){  // 因为一开始没有方向，所以设为-1
                max = tmpMax;
            }
        }
        return max;
    }

    public int maxPathLength(int[] startCoord, int piecesUsed, LinkedList<Coordinate> endGoals, LinkedList<Coordinate> connections, LinkedList<Coordinate> visited, int lastDirection){   // connections是startCoord周围可连接的chip 表示以startCoord为原点能够形成connections的chips的list
        if (piecesUsed > 10){ // 最多10个算一个path
            return 0;
        }
        if (listContains(endGoals, startCoord)){ // 说明已经到达终点goal area
            return piecesUsed;
        }
        if (listContains(visited, startCoord)){  // 如果visited path已经包含这个chip，就不可能形成path，同一个chip不能用两次
            return piecesUsed-1;
        }
        int max = piecesUsed;
        ListIterator<Coordinate> it = connections.listIterator();
        while (it.hasNext()){
            Coordinate chip = it.next();
            int[] coord = chip.getCoord();
            int currentDirection = direction(startCoord, coord);
            if (currentDirection == lastDirection){  // 比较上一个connection的方向，说明方向一致不能形成下一个connection，跳过
                continue;
            }
            visited.add(new Coordinate(startCoord)); // 把这个chip尝试加入path，然后继续往下面search
            int tmpMax = maxPathLength(startCoord, piecesUsed, endGoals, connections, visited, -1);
            if (max < tmpMax){  // 因为一开始没有方向，所以设为-1
                max = tmpMax;
            }
            visited.remove();         
        }
        return max;
    }



    // check if both goals hava chips
    private boolean inGoalArea(int playerColor) {
        boolean goalarea1 = false;  // 两个goal areas
        boolean goalarea2 = false;
        if (playerColor == WHITE){
            for (int y = 1; y < 7; y++){
                if (this.getSquare(0,y) == WHITE){
                        goalarea1 = true;
                }
                if (this.getSquare(7,y) == WHITE){
                        goalarea2 = true;
                }
            }
        }
        if (playerColor == BLACK){
            for (int x = 1; x < 7; x++){
                if(this.getSquare(x,0) == BLACK){
                        goalarea1 = true;
                }
                if(this.getSquare(x,7) == BLACK){
                        goalarea2 = true;
                }
            }
        }
        return goalarea1 && goalarea2;
    }

    // check if the target position is goal area
    private boolean isGoalPosition(Move m, int color){
        if (color == WHITE){
            if ((m.x1 == 0) || (m.x1 == 7)){
                return true;
            }
        }
        else if (color == BLACK){
            if (m.y1 == 0 || m.y1 == 7){
                return true;
            }
        }
        return false;
    }
        
        
        
     /**
      * Finds the pieces in the player's goal areas
      * @param playerColor the color of the player who's goals are being searched
      * @param x the row or column being searched (depending on color)
      * @return a DList of the coordinates containing goal pieces
      */
    private LinkedList<Coordinate> goalPieces (int playerColor, int x) {
        LinkedList<Coordinate> piecesInGoalArea = new LinkedList<Coordinate>();
        for (int y = 1; y < 7; y++) {
            if (playerColor == WHITE) {
                if (this.getSquare(x, y) == WHITE) {  // x = 0 || 7
                    int[] coord = {x, y};
                    piecesInGoalArea.add(new Coordinate(coord));
                }
            } 
            else{   
                if (this.getSquare(y, x) == BLACK) {  // x = 0 || 7
                    int[] coord = {y, x};
                    piecesInGoalArea.add(new Coordinate(coord));
                }
            }
        }
        return piecesInGoalArea;  // 返回链表
    }
         
      

    /** currentConnections returns a DList with all the pieces containing a connection to given coordinate. 
     * This is used to build a network. 
     *@param x the x coordinate of the board
     *@param y the y coordinate of the board
     *@return a DList with all the spots on the board (Move items) containing a connection to the coordinate
     */

     protected LinkedList<Coordinate> currentConnections(int[] startcoord){ // 以x，y起始的所有可能的connection
        int x = startcoord[0];
        int y = startcoord[1];
        int color = getSquare(x,y);
        int opp = (color == BLACK) ? WHITE : BLACK;
        LinkedList<Coordinate> connections = new LinkedList<Coordinate>();

        // 下面是往8个方向search，找到能够相连的chip

         //TOP-LEFT
        int i = x-1;
        int j = y-1;
        while (i >= 0 && j >= 0){  // 一直向左上角search
            if (getSquare(i,j) == color) {
                int[] coord = {i,j};
                connections.add(new Coordinate(coord));
                break;
            }
            if (getSquare(i,j) == opp) {  // 如果被对手block就不再search
                break;
            }
            i--;
            j--;
        }
         //TOP
         i =x;
         j = y-1;
         while (j >= 0){
             if (getSquare(i,j) == color) {
                int[] coord = {i,j};
                connections.add(new Coordinate(coord));
                break;
            }
             if (getSquare(i,j) == opp) {
                break;
             }
             j--;
         }
         //TOP-RIGHT
         i =x+1;
         j = y-1;
         while (i<= 7 && j >= 0){
             if (getSquare(i,j) == color) {
                int[] coord = {i,j};
                connections.add(new Coordinate(coord));
                 break;
             }
             if (getSquare(i,j) == opp) {
                 break;
             }
             i++;
             j--;
         }
         //RIGHT
         i = x+1;
         j = y;
         while (i <= 7){
             if (getSquare(i,j) == color) {
                int[] coord = {i,j};
                connections.add(new Coordinate(coord));
                 break;
             }
             if (getSquare(i,j) == opp) {
                 break;
             }
             i++;
         }
         //BOTTOM-RIGHT
         i = x+1;
         j = y+1;
         while (i<= 7 && j <=7){
             if (getSquare(i,j) == color) {
                int[] coord = {i,j};
                connections.add(new Coordinate(coord));
                 break;
             }
             if (getSquare(i,j) == opp) {
                 break;
             }
             i++;
             j++;
         }
         //BOTTOM
         i = x;
         j = y+1;
         while (j <= 7){
             if (getSquare(i,j) == color) {
                int[] coord = {i,j};
                connections.add(new Coordinate(coord));
                 break;
             }
             if (getSquare(i,j) == opp) {
                 break;
             }
             j++;
         }
         //BOTTOM-LEFT
         i = x-1;
         j = y+1;
         while (i >=0 && j <=7){
             if (getSquare(i,j) == color) {
                int[] coord = {i,j};
                connections.add(new Coordinate(coord));
                 break;
             }
             if (getSquare(i,j) == opp) {
                 break;
             }
             i--;
             j++;
         }
         //Left
         i =x-1;
         j = y;
         while (i>= 0){
             if (getSquare(i,j) == color) {
                int[] coord = {i,j};
                connections.add(new Coordinate(coord));
                 break;
             }
             if (getSquare(i,j) == opp) {
                 break;
             }
             i--;
         }
        return connections;
    }

     /**
      * Finds which direction a connection is traveling in, based on positions of first and second chips in a connection
      * @param first the position of the first chip
      * @param next the position of the second coordinate
      * @return an int represeting the direction of the connection (UP, DOWN, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT)
      */
    private int direction(int[] first, int[] next){
        int x1 = first[0];
        int y1 = first[1];
        int x2 = next[0];
        int y2 = next[1];
        if (x2 < x1) {
            if (y2 == y1) {
                return LEFT_DIRECTION;
            }
            if (y2 > y1) {
                return DOWN_LEFT_DIRECTION;
            }
            if (y2 < y1) {
                return UP_LEFT_DIRECTION;
            }
        }
        if (x2 > x1) {
            if (y2 == y1) {
                return RIGHT_DIRECTION;
            }
            if (y2 > y1) {
                return DOWN_RIGHT_DIRECTION;
            }
            if (y2 < y1) {
                return UP_RIGHT_DIRECTION;
            }
        }
        else {
            if (y2 > y1) {
                return DOWN_DIRECTION;
            }
            if (y2 < y1) {
                return UP_DIRECTION;
            }
        }
        return -1;
    }

    /** getSquare determines the contents of the board at a given coordinate
    *@param x the x coordinate of the spot
    *@param y the y coordinate of the spot
    *@return the contents of the board in that spot (-1, 0, 1 for empty, black, or white)
    */

    protected int getSquare(int x, int y) {
         if (x<0 || x>7 || y<0 || y>7) {
            return Board.EMPTY;
         }
         else {
            return gameBoard[x][y];
        }
    }
    
    
    //VALID MOVES MODULE


  /** Uses the rules of the game and positions of the current chips
    * to generate a list of valid moves
    * @param playerColor the color of the player whose valid moves are being determiend
    * @return an array of Move objects which represent valid moves for the current game situation
    */
    protected LinkedList<Move> validMoves(int playerColor){  // 返回所有可能的valid moves
    	LinkedList<Move> moves = new LinkedList<Move>();
    	int chips = (playerColor == WHITE) ? whitePiecesLeft : blackPiecesLeft;
    	if (chips > 0){ //if there are chips left, generate valid add moves
    		for (int i = 0; i<8; i++){
    			for (int j = 0;j<8;j++){  // 对board上每一个格子check
    				Move m = new Move(i,j);
    				if(isValidMove(m,playerColor)){
    					moves.add(m);
    				}
    			}
    		}
    	}
        else{// generate valid STEP moves
    		LinkedList<Coordinate> positions = new LinkedList<Coordinate>();
    		int[] pos = new int[2];
    		for (int j=0;j<8;j++){ //generate a list of positions where there are chips of the desired color
    			for (int i=0; i<8; i++){
    				if (getSquare(i,j) == playerColor){ 
    					pos[0] = i;
    					pos[1] = j;
    					positions.add(new Coordinate(pos)); // 记下原来的十个chips的位置
    				}
    			}
    		}
            // 下面是再遍历每个格子，看十个原有的chip能否move到某个位置
    		for (int j=0; j<8;j++){
    			for (int i=0;i<8;i++){
                    if(getSquare(i, j) != EMPTY) continue; // 如果目标位置不为空就跳过
    				ListIterator<Coordinate> it = positions.listIterator();
					while (it.hasNext()){ // 遍历十个原来的chips
                        Coordinate squre = it.next();
						int[] coord = squre.getCoord();
						Move m = new Move(i,j,coord[0], coord[1]); //try to move each chip in the list from its current position to 
						if(isValidMove(m, playerColor)){ //every other spot on the board, if possible add it to the moves list
							moves.add(m);
						}
					}
    			}
    		}
    	}	
        return moves;
    }

    /**
     * Determines whether a move is on the board
     * @param m the move being evaulated
     * @return true if it is on the board, or false if not
     */
    private boolean onBoard(Move m){
    	if (m.x1 < 0 || m.x1 > 7){
    		return false;
    	}
    	if (m.y1 < 0 || m.y1 > 7){
    		return false;
    	}
    	return true;
    }

    /**Determines whether a move is valid or not
     * @param m the move being evaluated
     * @param playerColor the player being evauluated
    *@return true if the move is valid, false otherwise
    */
    protected boolean isValidMove(Move m, int playerColor) {
	    if (m.moveKind == Move.STEP){
            if (m.x1 == m.x2 && m.y1 == m.y2){  // 不能不动
                return false;
            }
        }

        if (onBoard(m)){
            if (getSquare(m.x1,m.y1) != EMPTY){ // 目标位置不是empty
                return false;
            }
            if (isCorner(m.x1,m.y1)){
                return false;
            }
            if (playerColor == BLACK){
                if (m.moveKind == Move.ADD && blackPiecesLeft == 0){  // 如果是ADD, 需要还有chips能用
                    return false;
                }
                if (isGoalPosition(m,WHITE) || isClustered(m, playerColor)){  // 不能是在对方的goal，也不能形成cluster
                    return false;
                }
                return true;    
            }
            else if (playerColor == WHITE){
                if (m.moveKind == Move.ADD && whitePiecesLeft == 0){
                    return false;
                }
                if (isGoalPosition(m,BLACK) || isClustered(m,playerColor)){
                    return false;
                }
                return true;
            }
        }
        return false;

	}


    /** makeMove takes a legal move and an int player and changes the board. It can make
        an add move or a step move and it labels the move as which it is. It adds the piece and needs to 
        decrement the amount of pieces left for each player.
        @param m - Takes in a move to make
        @ param playerColor - Determines which player the move is made for for chip color
        @return NOTHING HA!
    */

    protected void makeMove(Move m, int playerColor){
        if (m.moveKind == Move.ADD) {
            gameBoard[m.x1][m.y1] = playerColor;
            if (playerColor == Board.WHITE) {
                whitePiecesLeft--;
        	}
            if (playerColor == Board.BLACK) {
                blackPiecesLeft--;
        	}
    	}
    	else if (m.moveKind == Move.STEP) {
    		gameBoard[m.x2][m.y2] = Board.EMPTY; 
    		gameBoard[m.x1][m.y1] = playerColor; 
    	}
    }
    
    protected void withdrawMove(Move m, int playerColor){
        if (m.moveKind == Move.ADD) {
            gameBoard[m.x1][m.y1] = EMPTY;
            if (playerColor == Board.WHITE) {
                whitePiecesLeft++;
            }
            if (playerColor == Board.BLACK) {
                blackPiecesLeft++;
            }
        }
        else if (m.moveKind == Move.STEP) {
            gameBoard[m.x2][m.y2] = playerColor; 
            gameBoard[m.x1][m.y1] = Board.EMPTY; 
        }
    }
}

