
package quartoGame;

/**
 * Simple AI to play against in Quarto game
 * 
 * Comes in two difficulties:
 * -Completely random, which ends up being generally very easy
 * 
 * -Uses basic strategies: do not give free wins to player and will take a win if it is offered
 * 
 */

import java.util.Random;

/**
 *
 * @author 100661873
 */
public class QuartoAI {
    
    private final int difficulty;
    private final Random rand;
    
    //set the AI difficulty and set up the random seed
    //set to difficulty level supplied by user or set to default
    public QuartoAI(int difficulty){
        if(difficulty>=1 && difficulty<=2)
            this.difficulty = difficulty;
        else 
            this.difficulty = 1;
        rand =  new Random(System.nanoTime());
    }
    
    //default constructor
    public QuartoAI(){
        this.difficulty = 1;
        rand =  new Random(System.nanoTime());
    }
    
    //choose logic for piece selection based on difficulty setting
    public int selectPiece(){
        int piece = 0;
        switch(difficulty) {
            case 1:
                piece = piece1();
                break;
            case 2:
                piece = piece2();
                break;
        }
        return piece;
    }
    
    //level 1 piece selection
    private int piece1(){
        int piece;
        //randomly select from 0 to 15 as the piece value
        //if piece is unavailable reroll until it is
        do {
            piece = rand.nextInt(16);
        }while(!Quarto.moves[piece]);
        return piece;
    }
    
    //level 2 piece selection
    private int piece2(){
        int piece;
        int moveCount = moveCount();
        if(moveCount > 14){
            //does not matter that much with less than 3 pieces left on board, cannot lose yet
            piece = piece1();
        }
        else {
            //now we have to score each move based on what is on the board
            //first generate the values for each of the 8 attributes
            //should pick the piece with the least value to the current state of the board
            //then choose the 'worst' piece for the opponent
            int value[] = {0, 0, 0, 0, 0, 0, 0, 0};
            for (int i = 0; i < 4; i++){
                int tempval[] = {i, 0, i, 1, i, 2, i, 3};
                tempval =  attributeValue(tempval);
                for (int j = 0; j < 8; j ++){
                    //set value to the highest score
                    value[j]=Math.max(value[j], tempval[j]);
                    
                    //at same time generate new values for tempval
                    if (j % 2 == 0)
                        tempval[j] = j/2;
                    else 
                        tempval[j]=i;
                }
                tempval =  attributeValue(tempval);
                for (int j = 0; j < 8; j ++)
                    //set value to the highest score
                    value[j]=Math.max(value[j], tempval[j]);  
            }
            
            //if 2x2 is enabled each square also has to be checked
            if(Quarto.enable2x2){
                for (int x = 0; x < 3; x++)
                    for (int y = 0; y < 3; y++){
                        int tempval[] = {x, y, x+1, y, x, y+1, x+1, y+1};
                        tempval =  attributeValue(tempval);
                        for (int j = 0; j < 8; j ++)
                            //set value to the highest score
                            value[j]=Math.max(value[j], tempval[j]);
                    }
            }
            
            //now check diagonal
            int tempval[] = {0, 0, 1, 1, 2, 2, 3, 3};
            tempval =  attributeValue(tempval);
            for (int j = 0; j < 8; j ++)
                    //set value to the highest score
                    value[j]=Math.max(value[j], tempval[j]);
            //other diagonal
            int tempval2[] = {0, 3, 1, 2, 2, 1, 3, 0};
            tempval2 =  attributeValue(tempval2);
            for (int j = 0; j < 8; j ++)
                    //set value to the highest score
                    value[j]=Math.max(value[j], tempval2[j]);
            
            //now that each value has been selected each piece should be tested
            int best = Integer.MAX_VALUE;
            piece = -1;
            
            //check the viability of each remaining piece
            for(int i = 0; i < 16; i++){
                if(Quarto.moves[i]){
                    int currentScore = calculateScore(i, value, true);
                    //pick the lowest score
                    if(currentScore < best){
                        best = currentScore;
                        piece = i;
                    }
                }                
            }
            
            //if all moves are terrible choose the piece with the least bad score
            for(int i = 0; i < 16; i++){
                if(Quarto.moves[i]){
                    int currentScore = calculateScore(i, value, false);
                    //pick the lowest score
                    if(currentScore < best){
                        best = currentScore;
                        piece = i;
                    }
                }                
            }
            
        }
        return piece;
    }
    
    
    //choose logic for move selection based on difficulty
    public int[] selectMove(int piece){
        int move[];
        if (difficulty == 1){
            move = move1();
        } else {
            move = move2(piece);
        }
        return move;
    }
    
    //level 1 move selection
    public int[] move1(){
        int move[] = new int[2];
        //generate bound for row selection by not selecting full rows
        int lbound;
        int ubound;
        for (lbound = 0; lbound < 3; lbound++){
            if (Quarto.lineTotal(Quarto.board, lbound, 0)<4)
                break;
        }
        for (ubound = 3; ubound > 0; ubound --){
            if (Quarto.lineTotal(Quarto.board, ubound,0)<4)
                break;
        }
        //randomly generate x and y coordinate for move
        do {
            move[0] = rand.nextInt(ubound+1) +lbound;
            move[1] = rand.nextInt(4);
        }while(Quarto.board[move[0]][move[1]] != -1);
        
    
        return move;
    }
    
    //level 1 move selection
    public int[] move2(int piece){
        int move[] = new int[2];
        
        //4 pieces have yet to be placed: can place piece anywhere
        if(moveCount()< 4){
            move = move2(piece);
        }
        
        //last move: pick empty slot on board
        else if (moveCount() == 16){
            for (int x = 0; x < 4; x++)
                for(int y = 0; y < 4; y++)
                    if(Quarto.board[x][y]<0){
                        move[0]=x;
                        move[1]=y;
                    }
        }
        
        //default stratagy: try to pick a move that wins, otherwise pick randomly
        else {
            //check each spot on the board
            for (int i = 0; i < 4; i++)
                for(int j = 0; j < 4; j++){
                    //check if spot is empty 
                    if(Quarto.board[i][j]<0){
                        //copy board
                        int[][] board = new int[4][4];
                        for (int x = 0; x < 4; x++)
                            for(int y = 0; y < 4; y++)
                                board[x][y]=Quarto.board[x][y];
                        //place piece
                        board[i][j] = piece;
                        //return if move wins
                        if(Quarto.winCondition(board)){
                            move[0]=i;
                            move[1]=j;
                            return move;
                        }
                    }
                }
            //if no win is found return a random move
            move = move1();
        }
    
        return move;
    }
    
    //count number of remaining moves
    public int moveCount(){
        int count = 0;
        for(boolean move: Quarto.moves)
            if(move)
                count++;
        return count;
    }
    
    //calculate the value of each attribute of the pieces
    public int[] attributeValue(int vect[]){
        int value[] = {0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < 4; i+=2){
            if(Quarto.board[i][i+1]>=0){
                if (Quarto.pieces[Quarto.board[i][i+1]].colour){
                    value[0]++;
                } else {
                    value[1]++;
                }
                if (Quarto.pieces[Quarto.board[i][i+1]].hollow){
                    value[2]++;
                } else {
                    value[3]++;
                }
                if (Quarto.pieces[Quarto.board[i][i+1]].size){
                    value[4]++;
                } else {
                    value[5]++;
                }
                if (Quarto.pieces[Quarto.board[i][i+1]].shape){
                    value[6]++;
                } else {
                    value[7]++;
                }
            }
        }
        return value;
    }
    
    //score a given piece based on information from the board
    public int calculateScore(int index, int attrScore[], boolean noWin){
        int score = 0;
        
        //if noWin is enabled return Max value for any attribute with a win condition
        if (Quarto.pieces[index].colour){
            if (attrScore[0] == 3 && noWin)
                return Integer.MAX_VALUE;
            score+=attrScore[0];
        } else {
            if (attrScore[1] == 3 && noWin)
                return Integer.MAX_VALUE;
            score+=attrScore[1];
        }
        
        if (Quarto.pieces[index].hollow){
            if (attrScore[2] == 3 && noWin)
                return Integer.MAX_VALUE;
            score+=attrScore[2];
        } else {
            if (attrScore[3] == 3 && noWin)
                return Integer.MAX_VALUE;
            score+=attrScore[3];
        }
        
        if (Quarto.pieces[index].size){
            if (attrScore[4] == 3 && noWin)
                return Integer.MAX_VALUE;
            score+=attrScore[4];
        } else {
            if (attrScore[5] == 3 && noWin)
                return Integer.MAX_VALUE;
            score+=attrScore[5];
        }
        
        if (Quarto.pieces[index].shape){
            if (attrScore[6] == 3 && noWin)
                return Integer.MAX_VALUE;
            score+=attrScore[6];
        } else {
            if (attrScore[7] == 3 && noWin)
                return Integer.MAX_VALUE;
            score+=attrScore[7];
        }
        
        return score;
    }
    
}
