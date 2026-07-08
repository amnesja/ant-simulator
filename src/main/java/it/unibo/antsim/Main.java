package it.unibo.antsim;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.config.ViewConfig;
import it.unibo.antsim.controller.SimulationController;
import it.unibo.antsim.model.agent.Ant;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.simulation.SimulationEngine;
import it.unibo.antsim.view.ControlPanel;
import it.unibo.antsim.view.SimulationView;
import it.unibo.antsim.view.StatsPanel;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
    private SimulationController controller;
    private SimulationEngine engine;
    private SimulationView view;
    private StatsPanel statsPanel;

    @Override
    public void start(Stage primaryStage) {
        Environment environment = createEnvironment();
        engine = createEngine(environment);
        controller = new SimulationController(engine);
        view = new SimulationView(environment);
        statsPanel = new StatsPanel();

        BorderPane root = createLayout();
        Scene scene = new Scene(root, ViewConfig.SCENE_WIDTH, ViewConfig.SCENE_HEIGHT);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        view.render(engine.getAnts());
        statsPanel.update(engine);

        primaryStage.setTitle("Ant Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        setupRenderingLoop();
        primaryStage.setOnCloseRequest(e -> {
            controller.stop();
            System.out.println("Simulation stopped!");
        });
    }

    private Environment createEnvironment() {
        Environment environment = new Environment(SimulationConfig.GRID_WIDTH, SimulationConfig.GRID_HEIGHT);
        environment.generateFood(SimulationConfig.INITIAL_FOOD_COUNT);
        environment.generateObstacle(SimulationConfig.INITIAL_OBSTACLE_COUNT);
        return environment;
    }

    private SimulationEngine createEngine(Environment environment) {
        SimulationEngine simulationEngine = new SimulationEngine(environment);
        simulationEngine.setFoodGenerationInterval(SimulationConfig.FOOD_GENERATION_INTERVAL);

        for (int i = 0; i < SimulationConfig.INITIAL_AGENT_COUNT; i++) {
            simulationEngine.addAnt(new Ant(0, 0));
        }

        return simulationEngine;
    }

    private BorderPane createLayout() {
        BorderPane root = new BorderPane();

        ControlPanel controlPanel = new ControlPanel(controller, engine, view, statsPanel);

        root.setCenter(view);
        root.setRight(controlPanel);
        root.setBottom(statsPanel);

        return root;
    }

    private void setupRenderingLoop() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                view.render(engine.getAnts());
                statsPanel.update(engine);
            }
        };
        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
