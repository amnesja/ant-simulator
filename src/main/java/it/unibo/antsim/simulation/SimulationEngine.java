package it.unibo.antsim.simulation;

import it.unibo.antsim.model.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * SimulationEngine is responsible for managing the simulation loop, updating the environment
 * and handling interactions between entities.
 */
public class SimulationEngine {
    private final Environment environment;
    private List<FakeAgents> agents = new ArrayList<>(); // Placeholder for actual Ant agents
    private boolean running = false;
    private int foodGenerationInterval = 10;
    private long stepCount = 0;

    public SimulationEngine(Environment environment) {
        this.environment = environment;
    }

    public void start() {
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }

    public void reset() {
        this.stepCount = 0;
    }

    public void step() {
        if (!running) return;

        if(stepCount % foodGenerationInterval == 0 && stepCount > 0) {
            environment.generateFood(2);
        }

        updateAgents();
        updateEnvironment();
        handleInteractions();
        stepCount++;
    }

    public void setFoodGenerationInterval(int foodGenerationInterval) {
        this.foodGenerationInterval = foodGenerationInterval;
    }

    public void updateEnvironment() {
        environment.update();
    }

    public void addAgent(FakeAgents agent) {
        agents.add(agent);
    }
    public void updateAgents() {
        for (FakeAgents agent : agents) {
            agent.move(environment);
        }
    }

    public void handleInteractions() {
        // food consumption (consumazione cibo)
        // global states update (aggiornamento stati globali)
        // interaction between ants (interazione tra formiche)
        for (FakeAgents agent : agents) {
            if (!agent.isCarryingFood() && environment.isFood(agent.getX(), agent.getY())) {
                agent.pickFood();
                environment.removeFood(agent.getX(), agent.getY());
                System.out.println("Food collected by agent at (" + agent.getX() + ", " + agent.getY() + ")");
            }

            if (agent.isCarryingFood() && environment.isNest(agent.getX(), agent.getY())) {
                agent.dropFood();
                System.out.println("Food delivered to nest!");
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public long getStepCount() {
        return stepCount;
    }

    public List<FakeAgents> getAgents() {
        return agents;
    }
}
