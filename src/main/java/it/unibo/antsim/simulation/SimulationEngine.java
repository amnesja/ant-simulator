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
    private long stepCount = 0;

    public SimulationEngine(Environment environment) {
        this.environment = environment;
        agents.add(new FakeAgents(2, 2)); // Adding a fake agent for testing
        agents.add(new FakeAgents(3, 3));
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
        updateAgents();
        updateEnvironvemt();
        handleInteractions();
        stepCount++;
    }

    public void updateEnvironvemt() {
        environment.update();
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
            if(environment.isFood(agent.getX(), agent.getY())){
                environment.removeFood(agent.getX(), agent.getY());
                System.out.println("Food collected by agent at ("
                        + agent.getX() + ", " + agent.getY() + ")");            }
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
