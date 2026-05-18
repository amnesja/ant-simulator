package it.unibo.antsim;

import it.unibo.antsim.controller.SimulationController;
import it.unibo.antsim.model.CellType;
import it.unibo.antsim.model.Environment;
import it.unibo.antsim.simulation.AsciiRenderer;
import it.unibo.antsim.simulation.FakeAgents;
import it.unibo.antsim.simulation.SimulationEngine;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    private SimulationController controller;
    private SimulationEngine engine;
    private int stepCount = 0;

    @Override
    public void start(Stage primaryStage) {
        // Setup environment
        Environment environment = new Environment(10, 10);

        // Setup nest at (0,0)
        environment.getCell(0, 0).setType(CellType.NEST);

        // Generate initial resources
        environment.generateFood(5);
        environment.generateObstacle(3);

        // Setup engine
        engine = new SimulationEngine(environment);
        engine.setFoodGenerationInterval(5);

        // Agents
        engine.addAgent(new FakeAgents(0, 0));
        engine.addAgent(new FakeAgents(0, 0));
        engine.addAgent(new FakeAgents(0, 0));

        // Setup controller
        controller = new SimulationController(engine);

        // Print initial state
        System.out.println("Starting simulation...\n");

        // Start simulation
        controller.start();

        // Rendering loop
        new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                try {
                    Thread.sleep(1000); // 1 secondo per step
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (i % 2 == 0) { // Renderizza ogni 2 step
                    System.out.println("\n========== STEP " + engine.getStepCount() + " ==========");
                    AsciiRenderer.render(environment, engine.getAgents());
                    System.out.println(engine.getState());
                }
            }

            controller.stop();
            System.out.println("\nSimulation finished!");
        }).start();

        primaryStage.setTitle("Ant Simulator");
        primaryStage.show();

        // Set up window close handler
        primaryStage.setOnCloseRequest(e -> {
            controller.stop();
            System.out.println("\nSimulation stopped!");
            System.out.println("Final state: " + engine.getState());
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}