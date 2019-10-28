package Agent.AgentInfo;

import org.jgap.IChromosome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FitnessFunction extends org.jgap.FitnessFunction {
    private static final int INCOMPLETE_DELIVERY_PENALTY = 125;
    private static final int INCOMPLETE_TRUCK_PENALTY = 2;
    private static final int DISTANCE_PENALTY = 25;

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

    public Node getNodes(int pos) {
        return nodes.get(pos);
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    private int numberOfVehicles;
    private int numberOfLocations;
    private int vehicleCapacity;
    private ArrayList<Node> nodes;

    public FitnessFunction(int numberOfVehicles, int numberOfLocations, int vehicleCapacity) {
        this.numberOfVehicles = numberOfVehicles;
        this.numberOfLocations = numberOfLocations;
        this.vehicleCapacity = vehicleCapacity;
    }

    @Override
    protected double evaluate(IChromosome iChromosome) {
        double fitness = 0;

        for(int i = 1; i < numberOfVehicles; i++) {
            fitness += computeTotalDistance(i, iChromosome, this) * DISTANCE_PENALTY;
        }

        if(fitness < 0) {
            return 0;
        }
        return Math.max(1, 100000 - fitness);
    }

    public double computeTotalDistance(int vehicleNumber, IChromosome iChromosome, FitnessFunction f) {
        double totalDistance = 0.0;

        final List<Integer> positions = this.getPositions(vehicleNumber, iChromosome, f, true);

        final Node store = nodes.get(0);

        Node lastVisited = store;

        for (int pos : positions) {
            final Node node = this.getNodes(pos);
            totalDistance += lastVisited.distanceTo(node);
            lastVisited = node;
        }

        totalDistance += lastVisited.distanceTo(store);//distance back to the store

        return totalDistance;
    }

    public List<Integer> getPositions(final int vehicleNumber, final IChromosome chromosome, final FitnessFunction f, final boolean order) {
        final List<Integer> route = new ArrayList<>();
        final List<Double> positions = new ArrayList<>();
        for (int i = 1; i < numberOfLocations; ++i) {
            int chromosomeValue = (Integer) chromosome.getGene(i).getAllele();
            if (chromosomeValue == vehicleNumber) {
                route.add(i);
                positions.add((Double) chromosome.getGene(i + numberOfLocations).getAllele());
            }
        }

        if (order) {
            order(positions, route);
        }
        return route;
    }

    private static void order(List<Double> positions, List<Integer> route) {
        for (int i = 0; i < positions.size(); ++i) {//todo improve sorting
            for (int j = i + 1; j < positions.size(); ++j) {
                if (positions.get(i).compareTo(positions.get(j)) < 0) {
                    Collections.swap(positions, i, j);
                    Collections.swap(route, i, j);
                }
            }
        }
    }
}


