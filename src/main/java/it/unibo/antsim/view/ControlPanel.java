package it.unibo.antsim.view;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.controller.SimulationController;
import it.unibo.antsim.simulation.SimulationEngine;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

public class ControlPanel extends VBox {
    private final SimulationController controller;
    private final SimulationEngine engine;
    private final SimulationView view;
    private final StatsPanel statsPanel;
    private final Button startBtn;
    private final Button pauseBtn;
    private final Button resumeBtn;
    private final Button resetBtn;

    public ControlPanel(
            SimulationController controller,
            SimulationEngine engine,
            SimulationView view,
            StatsPanel statsPanel
    ) {
        super(10);
        this.controller = controller;
        this.engine = engine;
        this.view = view;
        this.statsPanel = statsPanel;

        setPadding(new Insets(12));
        setPrefWidth(160);
        getStyleClass().add("control-panel");

        Label title = new Label("CONTROLS");
        title.getStyleClass().add("panel-title");

        startBtn = createButton("Start");
        pauseBtn = createButton("Pause");
        resumeBtn = createButton("Resume");
        resetBtn = createButton("Reset");

        startBtn.setOnAction(e -> {
            controller.start();
            updateControls();
        });

        pauseBtn.setOnAction(e -> {
            controller.pause();
            updateControls();
        });

        resumeBtn.setOnAction(e -> {
            controller.resume();
            updateControls();
        });

        resetBtn.setOnAction(e -> {
            controller.reset(SimulationConfig.INITIAL_AGENT_COUNT);
            engine.getEnvironment().resetDynamicElements(
                    SimulationConfig.INITIAL_OBSTACLE_COUNT,
                    SimulationConfig.INITIAL_FOOD_COUNT
            );
            view.render(engine.getAnts());
            statsPanel.update(engine);
            updateControls();
        });

        getChildren().addAll(
                title,
                new Separator(),
                startBtn,
                pauseBtn,
                resumeBtn,
                resetBtn
        );

        updateControls();
    }

    public void updateControls() {
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

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("control-button");
        return button;
    }
}
