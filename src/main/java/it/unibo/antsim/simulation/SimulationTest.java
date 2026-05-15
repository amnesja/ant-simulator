package it.unibo.antsim.simulation;

import it.unibo.antsim.model.Environment;

/**
 * SimulationTest is a simple test class to verify the functionality
 * of the SimulationEngine and Environment classes.
 */
public class SimulationTest {
    public static void main(String[] args) throws InterruptedException {
        Environment environment = new Environment(10,10);
        SimulationEngine engine = new SimulationEngine(environment);

        engine.start();

        for (int i = 0; i < 10; i++) {
            engine.step();
            System.out.println("Step: " + engine.getStepCount());
            Thread.sleep(500);
        }
    }
}
