/* MachinePlayer.java */

package player;

import java.util.*;


/**
 *  An implementation of an automatic Network player.  Keeps track of moves
 *  made by both players.  Can select a move for itself.
 */
public class MachinePlayer extends Player {

    protected int color;
    protected int opponentColor;
    protected int searchDepth;
    protected Board board;
    public static final boolean COMPUTER = true;

  
    /**
    * 
    * Creates a machine player with the given color.  Color is either 0 (black)
    * or 1 (white).  (White has the first move.)
    * @param color the color of the player
    */
    public MachinePlayer(int color) {
      this.color = color;
      this.opponentColor = (color == Board.BLACK) ? Board.WHITE : Board.BLACK;
      this.searchDepth = 3;  // 默认depth是 3
      board = new Board();
    }

    /**Creates a machine player with the given color and search depth.  Color is
    * either 0 (black) or 1 (white).  (White has the first move.)
    * @param color the color of the plater
    * @param searchDepth the depth you want to go to
    */
    public MachinePlayer(int color, int searchDepth) {
        this.color = color;
        this.opponentColor = (color == Board.BLACK) ? Board.WHITE : Board.BLACK;
        this.searchDepth = searchDepth; // 自定一个depth
        board = new Board();
    }

    // Returns a new move by "this" player.  Internally records the move (updates
    // the internal game board) as a move by "this" player.
    public Move chooseMove() {
        Move best = findBest(color);
        board.makeMove(best,color);
        return best;
    } 

    // If the Move m is legal, records the move as a move by the opponent
    // (updates the internal game board) and returns true.  If the move is
    // illegal, returns false without modifying the internal state of "this"
    // player.  This method allows your opponents to inform you of their moves.
    public boolean opponentMove(Move m) {
        if (board.isValidMove(m, opponentColor)){  // 检查对手的move是否valid
          board.makeMove(m,opponentColor);  // 更新到自己的board
          return true;
        }
        return false;
    }

    // If the Move m is legal, records the move as a move by "this" player
    // (updates the internal game board) and returns true.  If the move is
    // illegal, returns false without modifying the internal state of "this"
    // player.  This method is used to help set up "Network problems" for your
    // player to solve.
    public boolean forceMove(Move m) {
        if (board.isValidMove(m, color)){  // 检查自己的move是否valid
          board.makeMove(m,color);
          return true;
        }
        return false;
    }

    /// GAME-TREE SEARCH MODULE

    /**  Assigns a score to a board on the game tree. It is part of the Game Tree Search Module. It
    contains two important functions. It primarily checks if there is a network on the current board (by calling the and 
    then assigns a score based on recursively evaluating the next boards to a certain depth in order to 
    choose the next best possible move. It also needs to check if it is more advantageous to move forward for itself 
    or to ruin the opponent's network in progress. 
        * @param side is the color of the player who's moves we are currently looking for.
        * @param depth is how many more turns the algorithm can look ahead for a network or to evaluate a score.
        * @param alpha is the score the computer knows with certainty it can achieve. 表示电脑知道自己一定能够达到什么结局
        * @param beta is the score the opponent knows with certainty it can achieve.  表示对手知道自己一定能够达到什么结局
        * @return a Move which holds the highest scoring move. This is known as the BestMove.
        */
    private BestMove minimax(boolean side, int depth, double alpha, double beta){  // 这里就是不断变化side来进行推测后面几步的结果
        BestMove best = new BestMove();
        BestMove reply;

          // 首先要检查是否已经有network了
        if (board.hasNetwork(color)) {
            best.score = 100.0;
            return best;
        }
        if (board.hasNetwork(opponentColor)) { // 如果对手会形成network，那么score就是-100，AI会识别这种情况来避免
            best.score = -100.0;
            return best;
        }
        if (depth == 0){
            best.score = evaluateBoard(board);  //search直到depth为0的时候，根据evaluate得到score，也就是预测双方在3个move之后的情况
            return best;  // 如果有一方已经有network了，或者search depth为0，那就不需要做出move了
        } 
        if (side == COMPUTER){
            best.score = alpha;
        }else{
            best.score = beta;
        }

        LinkedList<Move> moves = board.validMoves(color);  // 得到当前board下所有可能的valid moves
        ListIterator<Move> it = moves.listIterator();
        while (it.hasNext()){  // 在每一个递归程序的level里，遍历每一个valid move，找到当前子结点的best move返回
                          //遍历每一个valid move
            Move currentMove = it.next();
            board.makeMove(currentMove, color); 
            reply = minimax(!side, depth-1, alpha, beta); //一方下过一子后depth减一，以当前的move为开头，返回这个支线得到的bestmove
            board.withdrawMove(currentMove, color);
            // 这里computer努力增大score，opponent努力减小score
            if ((side == COMPUTER) && (reply.score > best.score)){  // score越大，越有利于computer，更新更好的bestmove
              best.move = currentMove; // 更新best move，直到返回给最上层
              best.score = reply.score;
              alpha = reply.score; // α only changes during a computer (MAX) move
            }
            else if ((side != COMPUTER) && (reply.score < best.score)){  // score越小，越有利于opponent， 这里是对手的score
              best.move = currentMove;
              best.score = reply.score;
              beta = reply.score;  // β only changes during an opponent (MIN) move
            }
            if (alpha >= beta){  // 说明一定能够达到平局或者胜利  也就是这个方法其实是一直在找一个move，能够使自己成功率score大于对方
              return best;  // 用到 Alpha-Beta pruning
            }
        }
        return best;

    }

    /** This method uses minimax to find the best move possible. It uses alpha beta pruning to a certain depth, and evauluates each board, scoring it based on chance of winning
        * @param color is the color of the player who's moves we are currently looking for.
        * @returns a Move which holds the highest scoring move, and hence gives best chance of winning.
        */
    protected Move findBest(int color){  // 这里的推测是基于AI自己的findBest方法，来假设对手的best move来做出反应
        int alpha = Integer.MIN_VALUE;  // 一开始假定自己胜的几率最低
        int beta = Integer.MAX_VALUE;   // 假定对手胜的几率最高
        BestMove bestMove;
        boolean side = (this.color == color) ? true : false;
        bestMove = minimax(side, searchDepth, alpha, beta);
        return bestMove.move;
    }



    /**
    * evaluateBoard gives the current Board a score. This score reflects how likely it is to win if it is positive 
    * and if it is negative, how likely the opponent is to win. 
    * This method is underneath the Game Tree Search Module and specifically underneath the minimax interface. 
    * Minimax calls evaluateBoard after determining that the Board does not have a Network. It scores each board to represent the outcome. 
    * Scores closer to 100 mean the MachinePlayer is more likely to win and
    * scores closer to -100 mean the opponent is more likely to win.
    * @param b the Board object to be evaluated.
    * @return a double that determines the likelihood of winning. 
    */
    private double evaluateBoard(Board b){  // 这个评估方法，目的是产生更多chips之间的connection
        double myScore;
        double opponentScore;
        double blackScore = 0.0;
        double whiteScore = 0.0;
        if (b.hasNetwork(this.color)){
            return 100.0;
        }
        if (b.hasNetwork(this.opponentColor)){
            return -100.0;
        }

        // 检查四边的goal area，如果有一边有chip就+5
        for (int i = 1; i < 7; i ++){
            if(b.getSquare(i,0) == Board.WHITE){
                whiteScore += 5.0;
                break;
            }
        }
        for (int i = 1; i < 7; i ++){
            if(b.getSquare(i,7) == Board.WHITE){
                whiteScore += 5.0;
                break;
            }
        }   
        for (int i = 1; i < 7; i ++){
            if(b.getSquare(0,i) == Board.BLACK){
                blackScore += 5.0;
                break;
            }
        }
        for (int i = 1; i < 7; i ++){
            if(b.getSquare(7,i) == Board.BLACK){
                blackScore += 5.0;
                break;
            }
        }
    
        double basePoint = 10.0; // 每一个connection计为10分

        whiteScore += (double)(board.maxPathLength(Board.WHITE) * basePoint);
        blackScore += (double)(board.maxPathLength(Board.BLACK) * basePoint);
      
        // 先得到black和white的score，再来根据当前颜色赋值给myscore和opponentscore
        if (this.color == Board.WHITE) {
            myScore = whiteScore;
            opponentScore = blackScore;
        } else {
            opponentScore = whiteScore;
            myScore = blackScore;
        }
        return (myScore - opponentScore); //All of the pieces possible

    }

}

