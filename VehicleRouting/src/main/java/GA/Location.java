package GA;

public class Location {
    private int x;
    private int y;
    private int index;
    private int demand;
    private int[][] distancesMatrix;

    public int[][] getDistancesMatrix() {
        return distancesMatrix;
    }

    public void setDistancesMatrix(int[][] distancesMatrix) {
        this.distancesMatrix = distancesMatrix;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    public Location(int x, int y, int index) {
        this(x, y, index, 0);
    }

    public Location(int x, int y, int index, int demand) {
        this.x = x;
        this.y = y;
        this.demand = demand;
        this.index = index;
    }

    public double distanceTo(Location location) {
        return distancesMatrix[this.index - 1][location.index - 1];
    }
}
