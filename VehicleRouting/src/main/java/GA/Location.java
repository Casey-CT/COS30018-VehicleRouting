package GA;

public class Location {
    private int index;
    private int demand;
    private int[] distancesMatrix;

    public int[] getDistancesMatrix() {
        return distancesMatrix;
    }

    public void setDistancesMatrix(int[] distancesMatrix) {
        this.distancesMatrix = distancesMatrix;
    }

    public int getIndex() {
        return index;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    public Location(int index) {
        this(index, 0);
    }

    public Location(int index, int demand) {
        this.demand = demand;
        this.index = index;
    }

    public double distanceTo(Location location) {
        return distancesMatrix[location.index];
    }
}
