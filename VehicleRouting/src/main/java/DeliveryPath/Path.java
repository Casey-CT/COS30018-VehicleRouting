package DeliveryPath;

//TODO: Add in some try/catch blocks to handle any exceptions
//-locationPointer being an errant value

import com.google.gson.Gson;

//Class for containing data about a delivery agents path
public class Path {
    //Constants
    public final int NOT_STARTED = -1;
    public final int COMPLETE = -1;

    //Fields
    //int[] locations - Array of locations node ids
    //int[] distances - Array of same length as locations, contains the distances location and next location
    //int pathLength - length of location and distance arrays
    //int locationPointer - Points to the next location in the path. Must be between 0 and pathLength - 1
    //                    - Set at -1 to signal that the path has not started to be traversed
    private int[] locations;
    private int[] distances;
    private int pathLength;
    private int locationPointer;

    //Normal Constructor
    public Path(int[] locations, int[] distances) {
        if(locations == null || distances == null) {
            throw new IllegalArgumentException("ERROR: Location and Distance Arrays Cannot Be NULL");
        }

        this.locations = locations;
        this.distances = distances;
        if(locations.length != distances.length) {
            throw new IllegalArgumentException("ERROR: Location and Distance Arrays must have the same length");
        }
        else if(locations.length == 0 || distances.length == 0) {
            throw new IllegalArgumentException("ERROR: Location and Distance Arrays must have a length greater than zero");
        }
        else pathLength = locations.length;
        locationPointer = NOT_STARTED;
    }

    //Constructor for All Values
    public Path(int[] locations, int[] distances, int pathLength, int locationPointer) {
        //Should probably do some validation of data in here, but meh, not sure if this will be used at all
        this.locations = locations;
        this.distances = distances;
        this.pathLength = pathLength;
        this.locationPointer = locationPointer;
    }

    //Serialization Methods
    //Returns JSON representation of this object
    public String serialize() {
        Gson gson = new Gson();
        String json = gson.toJson(this);

        return json;
    }

    //Static method which creates a path object from a JSON representation
    public static Path deserialize(String json) {
        Gson gson = new Gson();
        Path p = gson.fromJson(json, Path.class);

        return p;
    }

    //Traversal Methods
    //Iterate LocationPointer to the Next Location
    public void traversePath() {
        if(locationPointer < pathLength) {
            locationPointer++;
        }
    }

    //Returns the ID of the next location, or -1 if path is complete
    public int getNextLocation() {
        if(!isPathComplete() && isPathStarted()) {
            return locations[locationPointer];
        }
        else return COMPLETE;
    }

    //Returns the distance to the next location, or -1 if the path is complete
    public int getNextDistance() {
        if(!isPathComplete() && isPathStarted()) {
            return distances[locationPointer];
        }
        else return COMPLETE;
    }

    public boolean isPathStarted() {
        return !(locationPointer == NOT_STARTED);
    }

    public boolean isPathComplete() {
        return locationPointer == pathLength;
    }

    //Field Getters
    public int[] getLocations() {
        return locations;
    }

    public int[] getDistances() {
        return distances;
    }

    public int getPathLength() {
        return pathLength;
    }

    public int getLocationPointer() {
        return locationPointer;
    }
}
