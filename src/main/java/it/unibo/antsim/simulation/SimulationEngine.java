package it.unibo.antsim.simulation;

import it.unibo.antsim.config.SimulationConfig;
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
        log("created");
    }

    public void start() {
        this.status = SimulationStatus.RUNNING;
        log("start");
    }

    public void pause() {
        if (status == SimulationStatus.RUNNING) {
            this.status = SimulationStatus.PAUSED;
            log("pause at step=%d", stepCount);
        }
    }

    public void resume() {
        if (status == SimulationStatus.PAUSED) {
            this.status = SimulationStatus.RUNNING;
            log("resume at step=%d", stepCount);
        }
    }

    public void stop() {
        this.status = SimulationStatus.STOPPED;
        log("stop at step=%d", stepCount);
    }

    public void reset(int agentCount) {
        this.stepCount = 0;
        this.status = SimulationStatus.STOPPED;
        log("reset agentCount=%d", agentCount);

        state.reset();
        ants.clear();

        for(int i = 0;i < agentCount; i++){
            addAnt(new Ant(0,0));
        }

        state.setAgentCount(ants.size());
    }

    public void step() {
        if (status != SimulationStatus.RUNNING) return;

        log("step=%d begin ants=%d foodHP=%d", stepCount, ants.size(), environment.getTotalFoodHP());

        if(stepCount % foodGenerationInterval == 0 && stepCount > 0) {
            log("step=%d periodic food generation", stepCount);
            environment.generateFood(1);
        }

        updateAgents();
        updateEnvironment();
        stepCount++;
        handleInteractions();
        log("step=%d end foodPicked=%d foodAtNest=%d foodHP=%d",
                stepCount,
                state.getFoodPicked(),
                state.getFoodAtNest(),
                environment.getTotalFoodHP());
    }

    public void setFoodGenerationInterval(int foodGenerationInterval) {
        this.foodGenerationInterval = foodGenerationInterval;
        log("foodGenerationInterval=%d", foodGenerationInterval);
    }

    public void updateEnvironment() {
        environment.update();
    }

    public void addAnt(Ant ant) {
        ants.add(ant);
        log("ant added id=%02d position=(%d,%d)", ant.getId(), ant.getX(), ant.getY());
    }

    public void updateAgents() {
        log("updateAgents count=%d", ants.size());
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
                log("ant=%02d picked food at (%d,%d) nearbyAnts=%d consumeAmount=%d",
                        ant.getId(), ant.getX(), ant.getY(), nearbyAnts, consumeAmount);
            }

            if (ant.isCarryingFood() && environment.isNest(ant.getX(), ant.getY())) {
                ant.dropFood();
                state.incrementFoodAtNest();
                log("ant=%02d delivered food to nest at (%d,%d)",
                        ant.getId(), ant.getX(), ant.getY());
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

    private void log(String format, Object... args) {
        if (SimulationConfig.ENABLE_CLI_LOGS) {
            System.out.printf("[ENGINE] %s%n", String.format(format, args));
        }
    }
}
