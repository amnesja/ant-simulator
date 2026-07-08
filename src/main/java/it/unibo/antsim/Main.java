package it.unibo.antsim;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.config.ViewConfig;
import it.unibo.antsim.controller.SimulationController;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.agent.Ant;
import it.unibo.antsim.simulation.SimulationEngine;
import it.unibo.antsim.view.SimulationView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {
    private SimulationController controller;
    private SimulationEngine engine;
    private SimulationView view;
    private Label statsLabel;
    private Button startBtn;
    private Button pauseBtn;
    private Button resumeBtn;
    private Button resetBtn;

    @Override
    public void start(Stage primaryStage) {
        // Setup environment
        Environment environment = new Environment(SimulationConfig.GRID_WIDTH, SimulationConfig.GRID_HEIGHT);
        environment.generateFood(SimulationConfig.INITIAL_FOOD_COUNT);
        environment.generateObstacle(SimulationConfig.INITIAL_OBSTACLE_COUNT);

        // Setup engine
        engine = new SimulationEngine(environment);
        engine.setFoodGenerationInterval(SimulationConfig.FOOD_GENERATION_INTERVAL);

        // Add ants
        for(int i = 0; i < SimulationConfig.INITIAL_AGENT_COUNT; i++){
            engine.addAnt(new Ant(0, 0));
        }

        // Setup controller
        controller = new SimulationController(engine);

        // Setup view
        view = new SimulationView(environment);

        // Initial render
        view.render(engine.getAnts());

        // Create UI layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #d7bd7d;");

        // Center: Canvas
        root.setCenter(view);

        // Bottom: Control buttons
        HBox controlPanel = createControlPanel();
        root.setBottom(controlPanel);

        // Top: Stats
        statsLabel = new Label();
        statsLabel.setStyle("-fx-font-size: 14; -fx-padding: 10; -fx-background-color: #f3dfad;");
        root.setTop(statsLabel);
        updateStats();
        updateControls();

        // Create scene
        Scene scene = new Scene(root, ViewConfig.SCENE_WIDTH, ViewConfig.SCENE_HEIGHT);

        // Setup window
        primaryStage.setTitle("Ant Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Setup rendering loop
        setupRenderingLoop();

        // Handle window close
        primaryStage.setOnCloseRequest(e -> {
            controller.stop();
            System.out.println("Simulation stopped!");
        });
    }

    /**     * Creates the control panel with buttons     */
    private HBox createControlPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #f3dfad; -fx-border-color: #b8934e; -fx-border-width: 1 0 0 0;");

        startBtn = new Button("Start");
        startBtn.setStyle("-fx-font-size: 12; -fx-padding: 8 20;");
        startBtn.setOnAction(e -> {
            controller.start();
            updateControls();
        });

        pauseBtn = new Button("Pause");
        pauseBtn.setStyle("-fx-font-size: 12; -fx-padding: 8 20;");
        pauseBtn.setOnAction(e -> {
            controller.pause();
            updateControls();
        });

        resumeBtn = new Button("Resume");
        resumeBtn.setStyle("-fx-font-size: 12; -fx-padding: 8 20;");
        resumeBtn.setOnAction(e -> {
            controller.resume();
            updateControls();
        });

        resetBtn = new Button("Reset");
        resetBtn.setStyle("-fx-font-size: 12; -fx-padding: 8 20;");
        resetBtn.setOnAction(e -> {
            controller.reset(SimulationConfig.INITIAL_AGENT_COUNT);
            engine.getEnvironment().resetDynamicElements(
                    SimulationConfig.INITIAL_OBSTACLE_COUNT,
                    SimulationConfig.INITIAL_FOOD_COUNT
            );
            view.render(engine.getAnts());
            updateStats();
            updateControls();
        });

        panel.getChildren().addAll(startBtn, pauseBtn, resumeBtn, resetBtn);
        return panel;
    }

    private void updateControls() {
        switch (engine.getStatus()) {
            case STOPPED:
                startBtn.setDisable(false);
                pauseBtn.setDisable(true);
                resumeBtn.setDisable(true);
                resetBtn.setDisable(false);
                break;
            case RUNNING:
                startBtn.setDisable(true);
                pauseBtn.setDisable(false);
                resumeBtn.setDisable(true);
                resetBtn.setDisable(false);
                break;
            case PAUSED:
                startBtn.setDisable(true);
                pauseBtn.setDisable(true);
                resumeBtn.setDisable(false);
                resetBtn.setDisable(false);
                break;
        }
    }

    /**     * Setup rendering loop to update Canvas every frame     */
    private void setupRenderingLoop() {
        // Render loop using AnimationTimer
        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                view.render(engine.getAnts());
                updateStats();
            }
        };
        timer.start();
    }

    /**     * Update statistics label     */
    private void updateStats() {
        String stats = String.format(
                "Status: %s | Step: %d | Food picked: %d | Food at Nest: %d | Food HP: %d | Agents: %d",
                engine.getStatus(),
                engine.getState().getStepCount(),
                engine.getState().getFoodPicked(),
                engine.getState().getFoodAtNest(),
                engine.getEnvironment().getTotalFoodHP(),
                engine.getState().getAgentCount()
        );
        statsLabel.setText(stats);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
