public class Field {
    private boolean isBomb = false;
    private boolean isRevealed = false;
    private boolean isFlagged = false;
    private int adjacentBombs = 0;

    public boolean isBomb()        { return isBomb; }
    public boolean isRevealed()    { return isRevealed; }
    public boolean isFlagged()     { return isFlagged; }
    public int getAdjacentBombs()  { return adjacentBombs; }

    public void setBomb(boolean b)         { isBomb = b; }
    public void setRevealed(boolean r)     { isRevealed = r; }
    public void setFlagged(boolean f)      { isFlagged = f; }
    public void setAdjacentBombs(int n)    { adjacentBombs = n; }
}
