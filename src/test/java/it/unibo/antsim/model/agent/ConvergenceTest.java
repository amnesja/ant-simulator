package it.unibo.antsim.model.agent;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.environment.Grid;
import it.unibo.antsim.model.environment.Position;
import it.unibo.antsim.simulation.SimulationEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConvergenceTest {

    @Test
    public void convergenceShowsEmergentTrail() {
        Environment env = new Environment(80, 80);
        env.setNestPosition(new Position(6, 40));
        env.generateFoodCluster(70, 40, 10.0, SimulationConfig.FOOD_CLUSTER_HP);

        SimulationEngine engine = new SimulationEngine(env);
        for (int i = 0; i < 300; i++) {
            engine.addAnt(new Ant(6, 40));
        }

        engine.start();

        int totalSteps = 6000;
        for (int step = 0; step < totalSteps; step++) {
            engine.step();

            if (step % 500 == 0) {
                double maxPheromone = computeMaxFoodPheromone(env);
                System.out.printf("step=%d foodPicked=%d foodAtNest=%d maxFoodPheromone=%.2f%n",
                        step,
                        engine.getState().getFoodPicked(),
                        engine.getState().getFoodAtNest(),
                        maxPheromone);
            }
        }

        double finalMax = computeMaxFoodPheromone(env);
        System.out.printf("FINAL step=%d foodPicked=%d foodAtNest=%d maxFoodPheromone=%.2f%n",
                totalSteps,
                engine.getState().getFoodPicked(),
                engine.getState().getFoodAtNest(),
                finalMax);

        assertTrue(engine.getState().getFoodAtNest() > 0,
                "no food delivered; convergence failed");
    }

    private double computeMaxFoodPheromone(Environment env) {
        double max = 0;
        Grid grid = env.getGrid();
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                double val = grid.getCell(x, y).getPheromoneLevel();
                if (val > max) max = val;
            }
        }
        return max;
    }
}
