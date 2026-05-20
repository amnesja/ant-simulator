package it.unibo.antsim;

import it.unibo.antsim.controller.SimulationController;
import it.unibo.antsim.model.CellType;
import it.unibo.antsim.model.Environment;
import it.unibo.antsim.simulation.FakeAgents;
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

    @Override
    public void start(Stage primaryStage) {
        // Setup environment
        Environment environment = new Environment(10, 10);
        environment.getCell(0, 0).setType(CellType.NEST);
        environment.generateFood(1);
        environment.generateObstacle(7);

        // Setup engine
        engine = new SimulationEngine(environment);
        engine.setFoodGenerationInterval(1000);

        // Add agents
        for(int i = 0; i < 10; i++){
            engine.addAgent(new FakeAgents(0, 0));
        }

        // Setup controller
        controller = new SimulationController(engine);

        // Setup view
        view = new SimulationView(environment, 300, 300);

        // Initial render
        view.render(engine.getAgents());

        // Create UI layout
        BorderPane root = new BorderPane();

        // Center: Canvas
        root.setCenter(view);

        // Bottom: Control buttons
        HBox controlPanel = createControlPanel();
        root.setBottom(controlPanel);

        // Top: Stats
        statsLabel = new Label();
        statsLabel.setStyle("-fx-font-size: 14; -fx-padding: 10;");
        root.setTop(statsLabel);
        updateStats();

        // Create scene
        Scene scene = new Scene(root, 400, 450);

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
        panel.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");

        Button startBtn = new Button("Start");
        startBtn.setStyle("-fx-font-size: 12; -fx-padding: 8 20;");
        startBtn.setOnAction(e -> controller.start());

        Button pauseBtn = new Button("Pause");
        pauseBtn.setStyle("-fx-font-size: 12; -fx-padding: 8 20;");
        pauseBtn.setOnAction(e -> controller.pause());

        Button resumeBtn = new Button("Resume");
        resumeBtn.setStyle("-fx-font-size: 12; -fx-padding: 8 20;");
        resumeBtn.setOnAction(e -> controller.start());

        Button resetBtn = new Button("Reset");
        resetBtn.setStyle("-fx-font-size: 12; -fx-padding: 8 20;");
        resetBtn.setOnAction(e -> {
            controller.reset(10);
            Environment environment = engine.getEnvironment();

            for(int x = 0; x < environment.getGrid().getWidth(); x++){
                for(int y = 0; y < environment.getGrid().getHeight(); y++){
                    if(environment.getCell(x, y).hasFood()){
                        environment.removeFood(x, y);
                    }
                }
            }

            environment.resetObstacles(3);
            environment.generateFood(5);
            view.render(engine.getAgents());
            updateStats();
        });

        panel.getChildren().addAll(startBtn, pauseBtn, resumeBtn, resetBtn);
        return panel;
    }

    /**     * Setup rendering loop to update Canvas every frame     */
    private void setupRenderingLoop() {
        // Render loop using AnimationTimer
        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                view.render(engine.getAgents());
                updateStats();
            }
        };
        timer.start();
    }

    /**     * Update statistics label     */
    private void updateStats() {
        String stats = String.format(
                "Step: %d | Food picked: %d | Food at Nest: %d | Food HP: %d | Agents: %d",
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
