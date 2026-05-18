package it.unibo.antsim.simulation;

import it.unibo.antsim.model.CellType;
import it.unibo.antsim.model.Environment;

public class SimulationTest {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("\n--- ENGINE + INTERACTION TEST ---");

        Environment env = new Environment(5, 5);

        env.getCell(0, 0).setType(CellType.NEST);
        env.getCell(2, 2).setType(CellType.FOOD);

        //Cibo iniziale
        env.generateFood(3);
        SimulationEngine engine = new SimulationEngine(env);
        //Aggiunta agenti
        engine.addAgent(new FakeAgents(0, 0));
        engine.addAgent(new FakeAgents(0, 0));
        engine.addAgent(new FakeAgents(0, 0));
        //Cibo
        engine.setFoodGenerationInterval(10);

        engine.start();

        for (int i = 0; i < 50; i++) {

            engine.step();

            System.out.println("STEP: " + engine.getStepCount());

            AsciiRenderer.render(env, engine.getAgents());

            Thread.sleep(300);
        }
    }
}