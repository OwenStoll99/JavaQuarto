/**
Quarto is owned by Gigamic.
*/

package quartoGame;
import java.util.Scanner;

/**
 *
 * @author 100661873
 */


public class Quarto {
    
    
    public static class Pieces {
        //each piece has 4 binary attributes, represented here by boolean variables
        public boolean colour;
        public boolean hollow;
        public boolean size;
        public boolean shape;
        
        public Pieces(boolean colour, boolean hollow, boolean size, boolean shape){
            this.colour = colour;
            this.hollow = hollow;
            this.size = size;
            this.shape = shape;
        }
    }
    
    //board starts out with -1s to indicate that a space is free
    public static int board[][]={{-1,-1,-1,-1},{-1,-1,-1,-1},{-1,-1,-1,-1},{-1,-1,-1,-1}};
    public static Pieces pieces[] = new Pieces[16];
    public static boolean moves[] = {true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true};
    public static boolean enable2x2 = false;
    
    //this function sets up an array of pieces so that each is unique
    public static void initializePieces(){
        boolean c = true, h = true, s = true, sh = true;
        
        for (int i = 0; i < pieces.length; i++){            
            h ^= true;
            if (i%2 == 0)
                sh ^= true;
            if(i%4 == 0)
                s ^= true;
            if(i%8 == 0)
                c ^= true;
            pieces[i]= new Pieces(c, h, s, sh);
        }
    }
    
    //used to determine if a boolean array is all the same value as the check
    public static boolean allSame (boolean check, boolean attribute[]){
        
        for (int i = 0; i < attribute.length; i++){
            if (check != attribute[i])
                return false;

        }        
        return true;
    }
    
    //this checks if each of the attributes is all the same
    public static boolean attributeCheck(int board[][], int x1, int y1,int x2, int y2,int x3, int y3,int x4, int y4){
        boolean[] colours = {pieces[board[x2][y2]].colour, pieces[board[x3][y3]].colour, pieces[board[x4][y4]].colour};
        
        if(allSame(pieces[board[x1][y1]].colour, colours))
            return true;
        
        boolean[] hollowness = {pieces[board[x2][y2]].hollow, pieces[board[x3][y3]].hollow, pieces[board[x4][y4]].hollow};
        if(allSame(pieces[board[x1][y1]].hollow, hollowness))
            return true;
        
        boolean[] sizes = {pieces[board[x2][y2]].size, pieces[board[x3][y3]].size, pieces[board[x4][y4]].size};
        if(allSame(pieces[board[x1][y1]].size, sizes))
            return true;
        
        boolean[] shapes = {pieces[board[x2][y2]].shape, pieces[board[x3][y3]].shape, pieces[board[x4][y4]].shape};
        return allSame(pieces[board[x1][y1]].shape, shapes);
    }
    
    //this function counts the total number of pieces contained in a line with a few special cases
    public static int lineTotal(int board[][], int k, int inv){
        int total = 0;
        for (int i = 0; i < 4; i++){
            switch(inv){
                //case 0: row
                case 0:
                    if(board[k][i]>=0)
                        total++;
                    break;
                //case 1: col    
                case 1:
                    if(board[i][k]>=0)
                        total++;
                    break;
                //top left to bottom right    
                case 2:
                    if(board[i][i]>=0)
                        total++;
                    break;
                //top right to bottom left    
                case 3:
                    if(board[i][3-i]>=0)
                        total++;
                    break;
            }
        }
        return total;
    }
    
    //counts the number of pieces in a 2x2 square on the board
    public static int quadrantTotal(int board[][], int x, int y){
        int total = 0;
        if(board[x][y]>=0)
            total++;
        if(board[x+1][y]>=0)
            total++;
        if(board[x][y+1]>=0)
            total++;
        if(board[x+1][y+1]>=0)
            total++;
        return total;
    }
    
    //returns true if the game has been won
    public static boolean winCondition(int board[][]){
        //check if the diagonals have 4 pieces and return the result if they do
        if (lineTotal(board, 0, 2)==4)
            if (attributeCheck(board, 0, 0, 1, 1, 2, 2, 3, 3))
                return true;
        if(lineTotal(board, 0, 3)==4) 
            if(attributeCheck(board, 0, 3, 1, 2, 2, 1, 3, 0))
                return true;
        
        //next check the horizontal and vertical rows of pieces and return the results if there is a win
        for (int i = 0; i < 4; i++){
            if (lineTotal(board, i, 0)==4)
                if(attributeCheck(board, i, 0, i, 1, i, 2, i, 3))
                    return true;
            
            if (lineTotal(board, i, 1)==4)
                if(attributeCheck(board, 0, i, 1, i, 2, i, 3, i))
                    return true;     
        }
        //lastly if 2x2 is enabled check each 2x2 square for 4 pieces and then if there is a win
        if (enable2x2){
            for (int x = 0; x < 3; x++)
                for (int y = 0; y < 3; y++)
                    if(quadrantTotal(board, x, y)==4)
                        if(attributeCheck(board, x, y, x+1, y, x, y+1, x+1, y+1))
                            return true;
                    
        }
        //if there is no winning condition return false
        return false;
    }
    
    
    //used to display a piece to the user
    private static void printPiece(int index, int height){
        //default is 'b'
        String token = "b";
        //if red set to 'r'
        if (pieces[index].colour)
            token = "r";
        //set to uppercase to indicate the the piece is 'big'
        if (pieces[index].size)
            token = token.toUpperCase();
        
        //change the colour of the token if the command prompt supports it
        if(token.matches("b")||token.matches("B")){
            token = "\u001B[34m" + token + "\u001B[0m";
        } else {
            token = "\u001B[31m"+ token + "\u001B[0m";
        }
        
        //print the token with conditions to handle hollow and differently shaped pieces
        if (height%2==0&&pieces[index].shape)
            System.out.print("  "+token+"  ");
        else if ((height+3)%4==0&&pieces[index].hollow)
            System.out.print(token+"   "+token);
        else
            System.out.print(token+" "+token+" "+token);                    
    }
    
    //display the board for the user
    public static void printBoard(){
        //print indeces for the top
        System.out.println("    1       2       3       4  ");
        //then print each row
        for (int i = 0; i < 15; i++){
            if ((i+1)%4==0){
                System.out.println("  - - - - - - - - - - - - - - -");
            } else {
                if((i+3)%4==0)
                    System.out.print((i/4 + 1)+" ");
                else 
                    System.out.print("  ");
                for (int j = 0; j < 4; j++){
                    if (board[i/4][j] >= 0)
                        printPiece(board[i/4][j], i);
                    else 
                        System.out.print("     ");
                    if (j != 3)
                        System.out.print(" | ");
                }
                System.out.print("\n");
            }         
        }
    }
    
    
    //let user select piece
    public static int choosePiece(Scanner scanner){
        Integer piece = null;
        
        //prompt the user
        System.out.println("Choose a piece for the opponent!");

        //set up piece menu for the user to choose from
        System.out.printf("%-10d %-10d %-10d %-10d %-10d %-10d %-10d %-10d\n", 1, 2, 3, 4, 5, 6, 7, 8);           
        for (int i = 0; i < 3; i++){
            for(int j = 0; j < 8; j++){    
                if (moves[j])
                    printPiece(j, i);
                else
                    System.out.print("     ");
                
                System.out.print("      ");
            }
            System.out.print("\n");
        }
        
        System.out.printf("\n%-10d %-10d %-10d %-10d %-10d %-10d %-10d %-10d\n", 9, 10, 11, 12, 13, 14, 15, 16);           
        for (int i = 0; i < 3; i++){
            for(int j = 8; j < 16; j++){    
                if (moves[j])
                    printPiece(j, i);
                else
                    System.out.print("     ");
                System.out.print("      ");
            }
            System.out.print("\n");
        }
        //finished printing menu
        
        
        //repeat until user chooses a valid move
        do {
            if (piece != null)
                System.out.println("Please choose a valid piece!");
            
            //repeat until user enters a number
            String p = null;
            do {
                if(p != null)
                    System.out.println("Please enter a number!");
                
                p = scanner.next();
                
            } while(!p.matches(".*\\d.*"));
            piece = Integer.parseInt(p)-1;
            
        } while(!moves[Math.abs(piece)%16]||piece < 0 || piece > 15);    
        
        return piece;
    }
    
    public static int [] chooseMove(int piece, Scanner scanner){
        Integer move[] = {null, null};
        //display the piece the user must place
        System.out.println("Place this piece:");
        for (int i =0; i < 3; i++){
            printPiece(piece, i);
            System.out.print("\n");
        }
        //repeat until user chooses a valid move
        do {
            if (move[0]!= null || move[1]!= null)
                System.out.println("Please choose a valid move!");
            
            //repeat until user enters a String
            String m1 = null, m2 = null;
            do {
                if(m1 != null || m2 != null)
                    System.out.println("Please enter a number!");
                
                m1 = scanner.next();
                m2 = scanner.next();
            }while(!m1.matches(".*\\d.*")||!m2.matches(".*\\d.*"));    
            move[0] = Integer.parseInt(m1)-1;
            move[1] = Integer.parseInt(m2)-1;
            
        } while(board[Math.abs(move[0])%4][Math.abs(move[1])%4] >= 0 || move[0]<0 || move[1]<0 || move[0]>3 || move[1]>3  );    
        
        int m [] = {move[0], move[1]};
        return m;
    }
    
    
    public static void main (String args[]){
        //set up values of Pieces object
        initializePieces();
        
        
        Scanner scanner = new Scanner(System.in);
        //determine if current player is the bot or human
        boolean player = true;
        int dif = 1;
        
        System.out.println("Do you wish to move first? (answer 'y' or 'n')");
        //switch value of player if yes
        if(Character.toLowerCase(scanner.next().charAt(0))=='y')
            player ^=true;
        
        System.out.println("Do you wish to enable 2x2 mode? (answer 'y' or 'n')");
        //if the answer is yes 2x2 squares can also win games
        if(Character.toLowerCase(scanner.next().charAt(0))=='y')
            enable2x2=true;
        
        System.out.println("Choose a difficulty: 1 or 2");
        //switch difficulty to 2 if chosen
        if(Character.toLowerCase(scanner.next().charAt(0))=='2')
            dif = 2;
        //initialize bot
        QuartoAI bot = new QuartoAI(dif);
        
        do {
            //display board at start of every turn
            printBoard();
            int piece;
            //if it is the players turn let them select the piece
            if(player)
                piece = choosePiece(scanner);
            //otherwise the bot does
            else
                piece = bot.selectPiece();
            //once a piece has been chosen it is no longer in play
            moves[piece] = false;
            
            //switch to other player
            player ^= true;
            
            //holds move
            int move[];
            //if it is the players turn let them place the piece
            if(player)
                move = chooseMove(piece, scanner);
            //otherwise the bot does it
            else
                move = bot.selectMove(piece);
            //update the board to reflect the move
            board[move[0]][move[1]] = piece;
            
            //continue until there are no moves left or a player wins
        } while (!winCondition(board) && !allSame(false, moves));
        scanner.close();
        //print the final board
        printBoard();
        //declare the result of the game based on who moved last
        if (player && winCondition(board))
            System.out.println("You win!");
        
        else if(!player && winCondition(board))
            System.out.println("You lose!");
        
        else 
            System.out.println("Draw!");
    }
    
}
