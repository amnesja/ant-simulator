package it.unibo.antsim.simulation;

import it.unibo.antsim.model.CellType;
import it.unibo.antsim.model.Environment;

public class SimulationTest {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("\n--- ENGINE + INTERACTION TEST ---");

        Environment env = new Environment(5, 5);

        env.getCell(0, 0).setType(CellType.NEST);
        env.getCell(2, 2).setType(CellType.FOOD);

        SimulationEngine engine = new SimulationEngine(env);
        engine.start();

        for (int i = 0; i < 40; i++) {

            engine.step();

            System.out.println("STEP: " + engine.getStepCount());

            AsciiRenderer.render(env, engine.getAgents());

            Thread.sleep(300);
        }
    }
}