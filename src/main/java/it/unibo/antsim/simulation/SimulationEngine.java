package it.unibo.antsim.simulation;

import it.unibo.antsim.model.Environment;

/**
 * SimulationEngine is responsible for managing the simulation loop, updating the environment
 * and handling interactions between entities.
 */
public class SimulationEngine {
    private final Environment environment;
    private boolean running = false;
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
        updateEnvironvemt();
        handleInteractions();
        stepCount++;
    }

    public void updateEnvironvemt() {
        environment.update();
    }

    public void handleInteractions() {
        // food consumption (consumazione cibo)
        // global states update (aggiornamento stati globali)
        // interaction between ants (interazione tra formiche)
    }

    public boolean isRunning() {
        return running;
    }

    public long getStepCount() {
        return stepCount;
    }
}
