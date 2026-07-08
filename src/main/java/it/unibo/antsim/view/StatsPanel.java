package it.unibo.antsim.view;

import it.unibo.antsim.simulation.SimulationEngine;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class StatsPanel extends HBox {
    private final Label statusLabel = new Label();
    private final Label stepLabel = new Label();
    private final Label foodPickedLabel = new Label();
    private final Label foodAtNestLabel = new Label();
    private final Label foodHpLabel = new Label();
    private final Label agentsLabel = new Label();

    public StatsPanel() {
        super(16);
        setPadding(new Insets(10));
        getStyleClass().add("stats-panel");

        getChildren().addAll(
                statusLabel,
                stepLabel,
                foodPickedLabel,
                foodAtNestLabel,
                foodHpLabel,
                agentsLabel
        );
    }

    public void update(SimulationEngine engine) {
        statusLabel.setText("Status: " + engine.getStatus());
        stepLabel.setText("Step: " + engine.getState().getStepCount());
        foodPickedLabel.setText("Food picked: " + engine.getState().getFoodPicked());
        foodAtNestLabel.setText("Food at nest: " + engine.getState().getFoodAtNest());
        foodHpLabel.setText("Food HP: " + engine.getEnvironment().getTotalFoodHP());
        agentsLabel.setText("Agents: " + engine.getState().getAgentCount());
    }
}
