package it.unibo.antsim.view;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.controller.SimulationController;
import it.unibo.antsim.simulation.SimulationEngine;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.DoubleConsumer;

public class ControlPanel extends ScrollPane {
    private final SimulationController controller;
    private final SimulationEngine engine;
    private final SimulationView view;
    private final StatsPanel statsPanel;
    private final Button startBtn;
    private final Button pauseBtn;
    private final Button resumeBtn;
    private final Button resetBtn;
    private final Button newWorldBtn;
    private final VBox content = new VBox(10);

    public ControlPanel(
            SimulationController controller,
            SimulationEngine engine,
            SimulationView view,
            StatsPanel statsPanel
    ) {
        super();
        this.controller = controller;
        this.engine = engine;
        this.view = view;
        this.statsPanel = statsPanel;

        setContent(content);
        setFitToWidth(true);
        setPadding(new Insets(12));
        setPrefWidth(220);
        getStyleClass().add("control-panel");

        Label title = new Label("CONTROLS");
        title.getStyleClass().add("panel-title");

        startBtn = createButton("Start");
        pauseBtn = createButton("Pause");
        resumeBtn = createButton("Resume");
        resetBtn = createButton("Reset");
        newWorldBtn = createButton("Nuovo mondo");

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
            view.resetCamera();
            view.render(engine.getAnts());
            statsPanel.update(engine);
            updateControls();
        });

        newWorldBtn.setOnAction(e -> {
            engine.regenerateWorld();
            view.resetCamera();
            view.render(engine.getAnts());
            statsPanel.update(engine);
        });


        content.getChildren().addAll(
                title,
                new Separator(),
                startBtn,
                pauseBtn,
                resumeBtn,
                resetBtn,
                newWorldBtn,
                new Separator()
        );

        addParamSliders();
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

    private void addParamSliders() {
        addSection("MOVIDENTO");
        addParamSlider("Velocità", 0.1, 3.0, SimulationConfig.ANT_SPEED,
                v -> SimulationConfig.ANT_SPEED = v, false);
        addParamSlider("Distanza sensori", 1, 15, SimulationConfig.SENSOR_DISTANCE,
                v -> SimulationConfig.SENSOR_DISTANCE = v, false);
        addParamSlider("Angolo sensori", 0.1, 1.5, SimulationConfig.SENSOR_ANGLE,
                v -> SimulationConfig.SENSOR_ANGLE = v, false);
        addParamSlider("Rotazione max", 0.05, 1.0, SimulationConfig.TURN_STRENGTH,
                v -> SimulationConfig.TURN_STRENGTH = v, false);
        addParamSlider("Erraticità", 0.0, 0.6, SimulationConfig.WANDER_STRENGTH,
                v -> SimulationConfig.WANDER_STRENGTH = v, false);

        addSection("FEROMONI");
        addParamSlider("Attraz. cibo", 0, 30, SimulationConfig.FOOD_SENSE_BONUS,
                v -> SimulationConfig.FOOD_SENSE_BONUS = v, false);
        addParamSlider("Attraz. nido", 0, 30, SimulationConfig.NEST_SENSE_BONUS,
                v -> SimulationConfig.NEST_SENSE_BONUS = v, false);
        addParamSlider("Deposito fero.", 0.1, 3.0, SimulationConfig.PHEROMONE_DEPOSIT_AMOUNT,
                v -> SimulationConfig.PHEROMONE_DEPOSIT_AMOUNT = v, false);
        addParamSlider("Evaporazione", 0.90, 0.999, SimulationConfig.PHEROMONE_EVAPORATION_RATE,
                v -> SimulationConfig.PHEROMONE_EVAPORATION_RATE = v, false);
        addParamSlider("Max fero.", 10, 200, SimulationConfig.MAX_PHEROMONE_LEVEL,
                v -> SimulationConfig.MAX_PHEROMONE_LEVEL = v, true);

        addSection("NIDO");
        addParamSlider("Raggio nido", 5, 50, SimulationConfig.NEST_HOME_EMIT_RADIUS,
                v -> SimulationConfig.NEST_HOME_EMIT_RADIUS = v, true);
        addParamSlider("Forza nido", 0, 5, SimulationConfig.NEST_HOME_EMIT_STRENGTH,
                v -> SimulationConfig.NEST_HOME_EMIT_STRENGTH = v, false);

        addSection("MONDO");
        addParamSlider("Ostacoli", 0, 40, SimulationConfig.OBSTACLE_CLUSTERS,
                v -> SimulationConfig.OBSTACLE_CLUSTERS = (int) v, true);
        addParamSlider("Raggio cibo", 4, 30, SimulationConfig.FOOD_CLUSTER_RADIUS,
                v -> SimulationConfig.FOOD_CLUSTER_RADIUS = v, false);
        addParamSlider("HP cibo", 20, 400, SimulationConfig.FOOD_CLUSTER_HP,
                v -> SimulationConfig.FOOD_CLUSTER_HP = (int) v, true);
        addParamSlider("Formiche", 10, 5000, SimulationConfig.INITIAL_AGENT_COUNT, v -> {
            SimulationConfig.INITIAL_AGENT_COUNT = (int) v;
            engine.setAgentCount((int) v);
        }, true);
    }

    private void addSection(String title) {
        Label header = new Label(title);
        header.getStyleClass().add("param-section");
        content.getChildren().add(header);
    }

    private void addParamSlider(String label, double min, double max, double value,
                               DoubleConsumer setter, boolean integer) {
        Label name = new Label(label);
        Label valueLabel = new Label(formatValue(value, integer));
        valueLabel.getStyleClass().add("param-value");

        HBox header = new HBox(4, name, valueLabel);
        HBox.setHgrow(name, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        Slider slider = new Slider(min, max, value);
        slider.setMaxWidth(Double.MAX_VALUE);
        slider.valueProperty().addListener((obs, oldV, newV) -> {
            double v = newV.doubleValue();
            setter.accept(v);
            valueLabel.setText(formatValue(v, integer));
        });

        VBox row = new VBox(2, header, slider);
        row.setPadding(new Insets(0, 0, 6, 0));
        content.getChildren().add(row);
    }

    private static String formatValue(double v, boolean integer) {
        if (integer) return String.valueOf((int) Math.round(v));
        if (v >= 100) return String.format("%.0f", v);
        if (v >= 10) return String.format("%.1f", v);
        if (v >= 1) return String.format("%.2f", v);
        return String.format("%.3f", v);
    }
}
