package it.unibo.antsim.simulation;

import it.unibo.antsim.model.Environment;
import it.unibo.antsim.model.SimulationState;

import javax.print.attribute.standard.PrinterState;
import java.util.ArrayList;
import java.util.List;

/**
 * SimulationEngine is responsible for managing the simulation loop, updating the environment
 * and handling interactions between entities.
 */
public class SimulationEngine {
    private final Environment environment;
    private List<FakeAgents> agents = new ArrayList<>(); // Placeholder for actual Ant agents
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
        agents.clear();

        for(int i = 0;i < agentCount; i++){
            agents.add(new FakeAgents(0,0));
        }

        state.setAgentCount(agents.size());
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

                int nearbyAgents = environment.countAgentsNearFood(agent.getX(), agent.getY(), agents);
                int consumeAmount = FOOD_CONSUMPTION_PER_AGENT * nearbyAgents;

                environment.consumeFood(agent.getX(), agent.getY(), consumeAmount);
                state.incrementFoodPicked();
                System.out.println("Food picked by agent at (" + agent.getX() + ", " + agent.getY() + ")");
            }

            if (agent.isCarryingFood() && environment.isNest(agent.getX(), agent.getY())) {
                agent.dropFood();
                state.incrementFoodAtNest();
                System.out.println("Food delivered to nest!");
            }
        }
        state.setStepCount(stepCount);
        state.setAgentCount(agents.size());
    }

    public List<FakeAgents> getAgents() {
        return agents;
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
