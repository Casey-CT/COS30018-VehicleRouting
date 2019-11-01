package GA;

import org.jgap.IChromosome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class
 FitnessFunction extends org.jgap.FitnessFunction {

    private int numberOfVehicles;
    private int numberOfLocations;
    private int vehicleCapacity;
    private ArrayList<Location> locations;

    private int truckOverloadPenalty;
    private int incompleteTruckPenalty;
    private int distancePenalty;

    public int getNumberOfVehicles() {
        return numberOfVehicles;
    }

    public void setNumberOfVehicles(int numberOfVehicles) {
        this.numberOfVehicles = numberOfVehicles;
    }

    public int getNumberOfLocations() {
        return numberOfLocations;
    }

    public void setNumberOfLocations(int numberOfLocations) {
        this.numberOfLocations = numberOfLocations;
    }

    public int getVehicleCapacity() {
        return vehicleCapacity;
    }

    public void setVehicleCapacity(int vehicleCapacity) {
        this.vehicleCapacity = vehicleCapacity;
    }

    public Location getLocation(int pos) {
        return locations.get(pos);
    }

    public void setLocations(ArrayList<Location> locations) {
        this.locations = locations;
    }

    public FitnessFunction(int numberOfVehicles, int numberOfLocations, int vehicleCapacity, int truckOverloadPenalty, int incompleteTruckPenalty, int distancePenalty) {
        this.numberOfVehicles = numberOfVehicles;
        this.numberOfLocations = numberOfLocations;
        this.vehicleCapacity = vehicleCapacity;
        this.truckOverloadPenalty = truckOverloadPenalty;
        this.incompleteTruckPenalty = incompleteTruckPenalty;
        this.distancePenalty = distancePenalty;
    }

    @Override
    protected double evaluate(IChromosome iChromosome) {
        double fitness = 0;

        for(int i = 1; i <= numberOfVehicles; i++) {
            fitness += computeTotalDistance(i, iChromosome, this) * distancePenalty;
            fitness += computeTruckCapacityOptimization(i, iChromosome, this);
        }

        if(fitness < 0) {
            return 0;
        }
        return  Math.max(1, 100000 - fitness);
    }

    public double computeUsedCapacity(int vehicleNumber, IChromosome iChromosome, FitnessFunction f) {
        final List<Integer> positions = getPositions(vehicleNumber, iChromosome, f, false);
        double usedCapacity = 0.0;

        for(int pos: positions) {
            final Location location = f.getLocation(pos);
            usedCapacity += location.getDemand();
        }
        return usedCapacity;
    }

    private  double computeTruckCapacityOptimization(int vehicleNumber, IChromosome iChromosome, FitnessFunction f) {
        final double vehicleCapacity = f.getVehicleCapacity();

        final double usedCapacity = computeUsedCapacity(vehicleNumber, iChromosome, f);

        if(usedCapacity >= vehicleCapacity) {
            //return (usedCapacity - vehicleCapacity) * incompleteDeliveryPenalty;
            return 100000;
        }
        return (vehicleCapacity - usedCapacity) * incompleteTruckPenalty;
    }

    public double computeTotalDistance(int vehicleNumber, IChromosome iChromosome, FitnessFunction f) {
        double totalDistance = 0.0;
        final List<Integer> positions = getPositions(vehicleNumber, iChromosome, f, true);
        final Location depot = locations.get(0);
        Location lastVisited = depot;

        for (int pos : positions) {
            final Location location = this.getLocation(pos);
            totalDistance += lastVisited.distanceTo(location);
            lastVisited = location;
        }
        totalDistance += lastVisited.distanceTo(depot);
        return totalDistance;
    }

    public List<Integer> getPositions(final int vehicleNumber, final IChromosome chromosome, final FitnessFunction f, final boolean order) {
        final List<Integer> route = new ArrayList<>();
        final List<Double> positions = new ArrayList<>();
        for (int i = 0; i < f.numberOfLocations; ++i) {
            int chromosomeValue = (Integer) chromosome.getGene(i).getAllele();
            if (chromosomeValue == vehicleNumber) {
                route.add(i);
                positions.add((Double) chromosome.getGene(i + f.numberOfLocations).getAllele());
            }
        }
        if (order) {
            order(positions, route);
        }
        return route;
    }

    private void order(List<Double> positions, List<Integer> route) {
        for (int i = 0; i < positions.size(); i++) {
            for (int j = i + 1; j < positions.size(); j++) {
                if (positions.get(i).compareTo(positions.get(j)) < 0) {
                    Collections.swap(positions, i, j);
                    Collections.swap(route, i, j);
                }
            }
        }
    }
}
