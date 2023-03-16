import java.util.*;
import java.io.*;   
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;
import javafx.scene.text.*;

public class State {
    private int numMines, marked, supermine = -1, size, moves = 0, timeleft = 0;
    private int[] current[], board[];
    private Rectangle[][] tile;
    private Text[][] t;
    private boolean loss;
    private Color[] colour_letter = {Color.LIGHTGRAY, Color.LIGHTGREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.CHOCOLATE, Color.PURPLE, Color.RED, Color.BLACK};
    ImagePattern mine_icon = new ImagePattern(new Image(App.path_to_project + "\\media\\mine_icon.png"));
    ImagePattern unknown_mine_icon =  new ImagePattern(new Image(App.path_to_project + "\\media\\unknown_mine_icon.png"));
    ImagePattern known_mine_icon =  new ImagePattern(new Image(App.path_to_project + "\\media\\known_mine_icon.png"));
    ImagePattern flag_icon = new ImagePattern(new Image(App.path_to_project + "\\media\\flag_icon.png"));
    ImagePattern marked_mine_icon = new ImagePattern(new Image(App.path_to_project + "\\media\\marked_mine_icon.png"));

    /*
     * current values:
     *                  -11 -> unknown
     *                  0-8 -> known
     *                  -1 -> marked
     *                  10 -> known mine
     */

    /**
     * Constructor for class State.
     * 
     * State is used to describe the current game.
     * 
     * @param table_size size of the board - can be either 9 or 16
     * @param n number of mines in given scenario
     * @param supermine_bool supermine existance parameter
     * @param time maximum time for completing the board
     */
    public State(int table_size, int n, int supermine_bool, int time) {
        size = table_size;
        int s = size*size;
        int sm = -1;
        loss = false;
        current = new int[size][size];
        board = new int[size][size];
        numMines = n;
        timeleft = time;
        moves = 0;
        marked = 0;
        Random rand = new Random();
        Set<Integer> mines = new HashSet<Integer>();
        String det = "";
        File mine_file = new File(App.path_to_project + "\\details\\mines.txt");
        FileWriter myWriter = null;
        try {myWriter = new FileWriter(mine_file);}
        catch (IOException e) {System.out.println("Cannot write to file mines.txt");}

        // finding random spots for mines
        if (supermine_bool > 0) sm = rand.nextInt(n);
        for (int i = 0; i < n; i++) {
            Integer x;
            do {
                x = rand.nextInt(s);
            }
            while (mines.contains(x));
            mines.add(x);
            if (i == sm) supermine = x;
        }

        // building the board
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                current[i][j] = -11;
                if (mines.contains(i*size + j)) {
                    board[i][j] = -1;
                    det =   det + Integer.toString(i) + ',' + Integer.toString(j) + 
                           ((i == supermine/size && j == supermine%size) ? ",1\n" : ",0\n");
                }
                else board[i][j] = 0;
            }
        }

        try {
            myWriter.write(det);
            myWriter.close();
        }
        catch (IOException e) {
            System.out.println("Cannot write to file mines.txt");
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] != -1) board[i][j] = count(i, j);
            }
        }
    }

    private int count(int i, int j) {
        int sum = 0;
        for (int k = i - 1; k < i + 2; k++) {
            for (int l = j - 1; l < j + 2; l++) {
                try {
                    if (board[k][l] == -1) sum += 1;
                }
                catch (ArrayIndexOutOfBoundsException e) {}
            }
        }
        return sum;
    }

    /**
     * Function to determine whether current state is a final winning state.
     * 
     * The function checks for any covered safe tile.
     * 
     * @return true if game is won - false if game is not won yet.
     */
    public boolean isWin() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) 
                if (board[i][j] != -1 && current[i][j] < 0) return false; 
                // if there's a safe square that's unopened return false
        } 
        return true;
    }

    /**
     * Function to determine whether current state is a final losing state.
     * 
     * @return true if game is lost - false if game is not lost yet.
     */
    public boolean isLoss() {
        return loss;
    }

    private Paint next(Move move) {
        int t = move.getType(), r = move.getRow(), c = move.getColumn();
        int curr = current[r][c], sq = board[r][c];
        if (curr == -2) {
            System.out.println("Illegal move. You have already uncovered this mine.");
            return null;
        }
        switch(t) {
            case(-1):       // mark tile     
                if (curr >= 0) {
                    System.out.println("Illegal move. You cannot mark an open tile.");
                    return null;
                }           
                if (curr == -11 && marked == numMines) {
                    System.out.println("Illegal move. You have reached the maximum number of marked tiles.");
                    return null;
                }
                if (curr == -1) {      // if already marked then unmark
                    current[r][c] = -11; 
                    marked -= 1;
                    return Color.WHITE;
                }

                // regular mark
                current[r][c] = -1;      
                marked += 1; 
                return flag_icon;

            case(1):        // show tile
                if (curr >= -1) return null;   // cannot open already marked or flagged tiles
                moves += 1;
                if (sq == -1) {
                    current[r][c] = -2;
                    return mine_icon;         // player lost
                }
                if (sq > 0) {
                    current[r][c] = sq;
                    return Color.GREY;
                }               
                current[r][c] = sq;
                return Color.LIGHTGRAY;
        }
        return Color.BLACK;  
    }

    private Paint supermine_next(int i, int j) {
        int b = board[i][j];
        if (current[i][j] == -1) marked--;
        current[i][j] = (b >= 0 ? b : 10);
        return (b == -1 ? known_mine_icon : ((b == 0) ? Color.LIGHTGRAY : Color.GRAY));

    }

    /**
     * Function to build the board.
     * 
     * This function builds a 9x9 or 16x16 GridPane that contails the game tiles. It also adds the subsequent eventhandlers on each tile.
     * 
     * @param solution true if the unsolved board is to be build - false if the solution board is to be built
     * @return a GridPane object
     */
    public GridPane show_board(boolean solution) {
        GridPane gameBoard = new GridPane();
        if (solution) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    Rectangle newtile = new Rectangle(30, 30);
                    Text newt = new Text("");
                    Paint paint;
                    int curr = current[i][j];
                    if (curr >= 0) {
                        gameBoard.add(new StackPane(tile[i][j], t[i][j]), j, i);
                        continue;
                    }
                    if (curr == -1 && board[i][j] == -1) paint = marked_mine_icon;
                    else if (curr == -2) paint = mine_icon;
                    else if (curr == -11 && board[i][j] == -1) paint = unknown_mine_icon;
                    else paint = Color.WHITE;
                    newtile.setFill(paint);
                    newtile.setStroke(Color.BLACK);
                    gameBoard.add(new StackPane(newtile, newt), j, i);
                }
            }
        }
        else {
            tile = new Rectangle[size][size];
            t = new Text[size][size];
            for (int i = 0; i < size; i++) {
                final int I = i;
                for (int j = 0; j < size; j++) {
                    final int J = j;
                    tile[i][j] = new Rectangle(30, 30);
                    tile[i][j].setId(Integer.toString(i) + "," + Integer.toString(j));
                    t[i][j] = new Text("");
                    t[i][j].setFont(Font.font("Verdana", 12));
                    t[i][j].setFill(board[I][J] >= 0 ? colour_letter[board[I][J]] : Color.WHITE);
                    t[i][j].setStroke(board[I][J] >= 0 ? colour_letter[board[I][J]] : Color.WHITE);
                    t[i][j].setStrokeWidth(1);
                    tile[i][j].setFill(Color.WHITE);
                    tile[i][j].setStroke(Color.BLACK);
                    if ((i == supermine/size && j == supermine%size) || board[i][j] == 0) {}
                    else {
                        tile[i][j].setOnMouseClicked(new EventHandler<MouseEvent> () { 
                            @Override 
                            public void handle(MouseEvent e) {
                                if (e.getButton() == MouseButton.PRIMARY) {
                                    Paint color = next(new Move(1, I, J));
                                    if (color != null) tile[I][J].setFill(color);
                                    if (color == Color.GREY) t[I][J].setText(Integer.toString(current[I][J]));
                                    if (tile[I][J].getFill() == mine_icon) loss = true;
                                }
                                if (e.getButton() == MouseButton.SECONDARY) {
                                    Paint color = next(new Move(-1, I, J));
                                    if (color != null) tile[I][J].setFill(color);
                                }
                            }});
                        }
                    gameBoard.add(new StackPane(tile[i][j], t[i][j]), j, i);
                }
            }
            
            if (supermine != -1) {
                Rectangle supermine_tile = tile[supermine/size][supermine%size];
                supermine_tile.setOnMouseClicked(new EventHandler<MouseEvent> () { 
                    @Override 
                    public void handle(MouseEvent e) {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            current[supermine/size][supermine%size] = -2;
                            supermine_tile.setFill(mine_icon);
                            loss = true;
                        }
                        if (e.getButton() == MouseButton.SECONDARY) {
                            if (moves < 5) {
                                for (int i = 0; i < size; i++) {
                                    Paint color = supermine_next(i, supermine%size);
                                    tile[i][supermine%size].setFill(color);
                                    if (color == Color.GREY) t[i][supermine%size].setText(Integer.toString(board[i][supermine%size]));
                                }
                                for (int j = 0; j < size; j++) {
                                    Paint color = supermine_next(supermine/size, j);
                                    tile[supermine/size][j].setFill(color);
                                    if (color == Color.GREY) t[supermine/size][j].setText(Integer.toString(board[supermine/size][j]));
                                }
                            }
                            else 
                            supermine_tile.setFill(next(new Move(-1, supermine/size, supermine%size)));
                        }
                    }});
            }
            
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (board[i][j] == 0) {
                        final int I = i, J = j;
                        tile[i][j].setOnMouseClicked(new EventHandler<MouseEvent> () { 
                            @Override 
                            public void handle(MouseEvent e) {
                                if (e.getButton() == MouseButton.PRIMARY) {
                                    current[I][J] = 0;
                                    tile[I][J].setFill(Color.LIGHTGRAY);
                                    Queue<Integer> q = new LinkedList<>();
                                    q.add(I*size + J);
                                    while(!q.isEmpty()) {
                                        int head = q.remove();
                                        int R = head / size, C = head % size;
                                        for (int k = R - 1; k < R + 2; k++) {
                                            for (int l = C - 1; l < C + 2; l++) {
                                                if (k == R && l == C) continue;
                                                try {
                                                    if (board[k][l] == 0 && current[k][l] == -11) q.add(k*size + l);
                                                    if (current[k][l] == -1) marked--;
                                                    current[k][l] = board[k][l];
                                                    if (board[k][l] == 0) tile[k][l].setFill(Color.LIGHTGRAY);
                                                    else {
                                                        tile[k][l].setFill(Color.GRAY);
                                                        t[k][l].setText(Integer.toString(board[k][l]));
                                                    }
                                                }
                                                catch (ArrayIndexOutOfBoundsException exc) {}
                                            }
                                        }
                                    }
                                }
                                if (e.getButton() == MouseButton.SECONDARY) {
                                    Paint color = next(new Move(-1, I, J));
                                    if (color != null) tile[I][J].setFill(color);
                                }
                            }
                        });
                    }
                }
            }
        }
        return gameBoard;
    }

    /**
     * Function to get the private numMines parameter
     * @return this.numMines
     */
    public int getTotalMines() {
        return numMines;
    }

    /**
     * Function to get the private marked parameter
     * @return this.marked
     */
    public int getMarked() {
        return marked;
    }

    /**
     * Function to get the private tileleft parameter
     * @return this.timeleft
     */
    public int getTime() {
        return timeleft;
    }

    /**
     * Function to get the private moves parameter
     * @return this.moves
     */
    public int getMoves() {
        return moves;
    }

    /**
     * Function to get the difficulty of current game
     * @return 1 if easy - 2 if difficult
     */
    public int getDifficulty() {
        return (size == 9) ? 1 : 2;
    }
}
