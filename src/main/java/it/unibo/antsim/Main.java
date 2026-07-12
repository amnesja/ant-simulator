package it.unibo.antsim;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.config.ViewConfig;
import it.unibo.antsim.controller.SimulationController;
import it.unibo.antsim.model.agent.Ant;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.environment.Position;
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
    private ControlPanel controlPanel;

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

        bindViewSize(scene);
        setupRenderingLoop();
        primaryStage.setOnCloseRequest(e -> {
            controller.stop();
            System.out.println("Simulation stopped!");
        });
    }

    private Environment createEnvironment() {
        Environment environment = new Environment(SimulationConfig.WORLD_WIDTH, SimulationConfig.WORLD_HEIGHT);
        environment.setNestPosition(new Position(SimulationConfig.NEST_X, SimulationConfig.NEST_Y));
        environment.generateRockClusters();
        environment.generateRandomFoodCluster();
        return environment;
    }

    private SimulationEngine createEngine(Environment environment) {
        SimulationEngine simulationEngine = new SimulationEngine(environment);
        simulationEngine.setFoodGenerationInterval(SimulationConfig.FOOD_GENERATION_INTERVAL);

        Position nest = environment.getNestPosition();
        for (int i = 0; i < SimulationConfig.INITIAL_AGENT_COUNT; i++) {
            simulationEngine.addAnt(new Ant(nest.x(), nest.y()));
        }

        return simulationEngine;
    }

    private BorderPane createLayout() {
        BorderPane root = new BorderPane();

        controlPanel = new ControlPanel(controller, engine, view, statsPanel);

        root.setCenter(view);
        root.setRight(controlPanel);
        root.setBottom(statsPanel);

        return root;
    }

    /**
     * Binds the simulation canvas to fill the center area (right panel and
     * bottom stats bar excluded). The camera follows the canvas size via a
     * listener in SimulationView, so the world always occupies the whole view
     * instead of being letterboxed into a square.
     */
    private void bindViewSize(Scene scene) {
        view.widthProperty().bind(scene.widthProperty().subtract(controlPanel.widthProperty()));
        view.heightProperty().bind(scene.heightProperty().subtract(statsPanel.heightProperty()));
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
