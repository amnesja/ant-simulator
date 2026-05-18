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
        System.out.println("\n--- ENGINE TEST ---");

        Environment environment = new Environment(5, 5);

        // metto cibo in una posizione
        environment.getCell(2, 2).setType(CellType.FOOD);

        // crea engine
        SimulationEngine engine = new SimulationEngine(environment);

        // avvia
        engine.start();

        // esegui step
        for (int i = 0; i < 10; i++) {
            engine.step();

            System.out.println("Step: " + engine.getStepCount());

            // stampa posizione agenti (fake)
            for (FakeAgents agent : engine.getAgents()) {
                System.out.println("Agent at: (" + agent.getX() + ", " + agent.getY() + ")");
            }

            Thread.sleep(300);
        }
    }
}
