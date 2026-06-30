package it.unibo.antsim.simulation;

import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.SimulationState;
import it.unibo.antsim.model.agent.Ant;
import it.unibo.antsim.model.environment.Cell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SimulationEngine is responsible for managing the simulation loop, updating the environment
 * and handling interactions between entities.
 */
public class SimulationEngine {
    private final Environment environment;
    private final List<Ant> ants = new ArrayList<>();
    private final SimulationState state =  new SimulationState();
    private SimulationStatus status = SimulationStatus.STOPPED;
    private int foodGenerationInterval = 100;
    private static final int FOOD_CONSUMPTION_PER_AGENT = 5;
    private long stepCount = 0;

    public SimulationEngine(Environment environment) {
        this.environment = environment;
    }

    public void start() {
        this.status = SimulationStatus.RUNNING;
    }

    public void pause() {
        if (status == SimulationStatus.RUNNING) {
            this.status = SimulationStatus.PAUSED;
        }
    }

    public void resume() {
        if (status == SimulationStatus.PAUSED) {
            this.status = SimulationStatus.RUNNING;
        }
    }

    public void stop() {
        this.status = SimulationStatus.STOPPED;
    }

    public void reset(int agentCount) {
        this.stepCount = 0;
        this.status = SimulationStatus.STOPPED;

        state.reset();
        ants.clear();

        for(int i = 0;i < agentCount; i++){
            ants.add(new Ant(0,0));
        }

        state.setAgentCount(ants.size());
    }

    public void step() {
        if (status != SimulationStatus.RUNNING) return;

        if(stepCount % foodGenerationInterval == 0 && stepCount > 0) {
            environment.generateFood(1);
        }

        updateAgents();
        updateEnvironment();
        stepCount++;
        handleInteractions();
    }

    public void setFoodGenerationInterval(int foodGenerationInterval) {
        this.foodGenerationInterval = foodGenerationInterval;
    }

    public void updateEnvironment() {
        environment.update();
    }

    public void addAnt(Ant ant) {
        ants.add(ant);
    }

    public void updateAgents() {
        for (Ant ant : ants) {
            ant.move(environment);
        }
    }

    public void handleInteractions() {
        // food consumption (consumazione cibo)
        // global states update (aggiornamento stati globali)
        // interaction between ants (interazione tra formiche)
        for (Ant ant : ants) {
            if (!ant.isCarryingFood() && environment.isFood(ant.getX(), ant.getY())) {
                ant.pickFood();

                int nearbyAnts = countAntsNearFood(ant.getX(), ant.getY());
                int consumeAmount = FOOD_CONSUMPTION_PER_AGENT * nearbyAnts;

                environment.consumeFood(ant.getX(), ant.getY(), consumeAmount);
                state.incrementFoodPicked();
                System.out.println("Food picked by ant at (" + ant.getX() + ", " + ant.getY() + ")");
            }

            if (ant.isCarryingFood() && environment.isNest(ant.getX(), ant.getY())) {
                ant.dropFood();
                state.incrementFoodAtNest();
                System.out.println("Food delivered to nest!");
            }
        }
        state.setStepCount(stepCount);
        state.setAgentCount(ants.size());
    }

    private int countAntsNearFood(int x, int y) {
        int count = 0;
        List<Cell> neighbors = environment.getNeighbors(x, y);

        for (Ant ant : ants) {
            if ((ant.getX() == x && ant.getY() == y)
                    || neighbors.contains(environment.getCell(ant.getX(), ant.getY()))) {
                count++;
            }
        }
        return count;
    }

    public List<Ant> getAnts() {
        return Collections.unmodifiableList(ants);
    }

    public SimulationState getState() {
        return state;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public SimulationStatus getStatus() {
        return status;
    }
}
