package it.unibo.antsim.controller;

import it.unibo.antsim.simulation.SimulationEngine;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * SimulationController is responsible for controlling the simulation flow, including starting, stopping,
 */
public class SimulationController {
    private final SimulationEngine engine;
    private Timeline timeline;

    public SimulationController(SimulationEngine engine) {
        this.engine = engine;
        setupTimeline();
    }

    private void setupTimeline() {
        timeline = new Timeline(
                new KeyFrame(Duration.millis(100), e -> engine.step())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void start() {
        engine.start();
        timeline.play();
    }

    public void stop() {
        engine.stop();
        timeline.stop();
    }

    public void pause() {
        timeline.pause();
    }

    public void reset(int agentCount) {
        stop();
        engine.reset(agentCount);
    }

}
