package it.unibo.antsim.simulation;

import it.unibo.antsim.model.Cell;
import it.unibo.antsim.model.CellType;
import it.unibo.antsim.model.Environment;

/**
 * SimulationTest is a simple test class to verify the functionality
 * of the SimulationEngine and Environment classes.
 */
public class SimulationTest {
    public static void main(String[] args) throws InterruptedException {
        /*Environment environment = new Environment(10,10);
        SimulationEngine engine = new SimulationEngine(environment);

        engine.start();

        for (int i = 0; i < 10; i++) {
            engine.step();
            System.out.println("Step: " + engine.getStepCount());
            Thread.sleep(500);
        }*/

        Environment env = new Environment(5, 5);

        // Metto cibo
        env.getCell(2, 2).setType(CellType.FOOD);

        System.out.println("Is food at (2,2): " + env.isFood(2, 2));

        // Test neighbors
        System.out.println("\nNeighbors of (2,2):");
        for (Cell c : env.getNeighbors(2, 2)) {
            System.out.println(c.getType());
        }

        // Test rimozione cibo
        env.removeFood(2, 2);
        System.out.println("\nAfter removal:");
        System.out.println("Is food at (2,2): " + env.isFood(2, 2));

        // Test feromoni
        env.addPheromone(1, 1, 10.0);
        System.out.println("\nPheromone before update: " +
                env.getCell(1,1).getPheromoneLevel());

        env.update();

        System.out.println("Pheromone after update: " +
                env.getCell(1,1).getPheromoneLevel());
    }
}
