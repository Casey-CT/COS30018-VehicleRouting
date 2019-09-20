import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;


public class GraphGen {
    private final int vertices;
    private int[][] twoD_array;

    public GraphGen(int v)
    {
        vertices = v;
        twoD_array = new int[vertices + 1][vertices + 1];
    }
    public void makeEdge(int to, int from, int edge)
    {
        try
        {
            twoD_array[to][from] = edge;
        }
        catch (ArrayIndexOutOfBoundsException index)
        {
            System.out.println("The vertices does not exists");
        }
    }
    public int getEdge(int to, int from)
    {
        try
        {
            return twoD_array[to][from];
        }
        catch (ArrayIndexOutOfBoundsException index)
        {
            System.out.println("The vertices does not exists");
        }
        return -1;
    }
    public static void main(String args[])
    {
        int v, vTo, vFrom, dMin, dMax, dRand, eRand, eMin, eMax, count = 1, to = 0, from = 0;
        Random r = new Random();
        Scanner sc = new Scanner(System.in);
        GraphGen graph;
        try
        {
            System.out.println("Enter the number of nodes: ");
            v = sc.nextInt();
            System.out.println("Enter the minimum number of connections: ");
            eMin = sc.nextInt();
            System.out.println("Enter the maximum number of connections: ");
            eMax = sc.nextInt();

            if (eMin > eMax) {
                System.out.println("Minimum cannot be greater than Maximum");
            }

            System.out.println("Enter the minimum distance: ");
            dMin = sc.nextInt();
            System.out.println("Enter the maximum distance: ");
            dMax = sc.nextInt();

            if (dMin > dMax) {
                System.out.println("Minimum cannot be greater than Maximum");
            }

            graph = new GraphGen(v);
            eRand = ThreadLocalRandom.current().nextInt(eMin, eMax + 1);
            System.out.println("eRand: " + eRand);
            while (count <= eRand)
            {
                vTo = r.nextInt(v) + 1;
                vFrom = r.nextInt(v) + 1;
                dRand = ThreadLocalRandom.current().nextInt(dMin, dMax + 1);
                if (vTo != vFrom) {
                    graph.makeEdge(vTo, vFrom, dRand);
                    count++;
                }
            }

            System.out.println("The two d array for the given graph is: ");
            System.out.print("  ");
            for (int i = 1; i <= v; i++)
                System.out.print(i + " ");
            System.out.println();
            for (int i = 1; i <= v; i++)
            {
                System.out.print(i + " ");
                for (int j = 1; j <= v; j++)
                    System.out.print(graph.getEdge(i, j) + " ");
                System.out.println();
            }
        }
        catch (Exception E)
        {
            System.out.println("Something went wrong");
        }
        sc.close();
    }
}
