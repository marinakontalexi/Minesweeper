public class PrevGame {
    public String s, mines, moves, time, winner;

    public PrevGame(String s1, String m, String n, String t, String w) {
        s = s1;
        mines = m;
        moves = n;
        time = t;
        winner = w;
    }

    public String getS() {
        return s;
    }

    public String getMines() {
        return mines;
    }

    public String getMoves() {
        return moves;
    }
    
    public String getTime() {
        return time;
    }
    public String getWinner() {
        return winner;
    }
}
