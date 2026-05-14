package it.unibo.antsim.simulation;

import it.unibo.antsim.model.Environment;

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
        environment.update();
        stepCount++;
    }

    public boolean isRunning() {
        return running;
    }

    public long getStepCount() {
        return stepCount;
    }
}
