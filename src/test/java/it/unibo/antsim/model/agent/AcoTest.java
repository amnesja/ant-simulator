package it.unibo.antsim.model.agent;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.environment.Position;
import it.unibo.antsim.simulation.SimulationEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AcoTest {

    @Test
    public void searchingAntsEventuallyFindTheFoodCluster() {
        Environment env = new Environment(80, 80);
        env.setNestPosition(new Position(6, 40));
        env.generateFoodCluster(70, 40, 10.0, SimulationConfig.FOOD_CLUSTER_HP);

        SimulationEngine engine = new SimulationEngine(env);
        for (int i = 0; i < 300; i++) {
            engine.addAnt(new Ant(6, 40));
        }

        engine.start();
        for (int step = 0; step < 3000; step++) {
            engine.step();
        }

        // With hundreds of ants and a large food source, some must reach it.
        assertTrue(engine.getState().getFoodPicked() > 0,
                "no ant ever reached the food cluster");
    }
}
