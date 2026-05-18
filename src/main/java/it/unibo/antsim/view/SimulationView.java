package it.unibo.antsim.view;

import it.unibo.antsim.model.CellType;
import it.unibo.antsim.model.Environment;
import it.unibo.antsim.simulation.FakeAgents;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class SimulationView extends Canvas {
    private final Environment environment;
    private final int cellSize = 30;

    public SimulationView(Environment environment, int width, int height) {
        this.environment = environment;
        this.setWidth(width);
        this.setHeight(height);
    }

    public void render(List<FakeAgents> agents) {
        GraphicsContext gc = this.getGraphicsContext2D();

        // Clear canvas
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, this.getWidth(), this.getHeight());

        // Draw grid
        drawGrid(gc);

        // Draw cells
        drawCells(gc);

        // Draw agents
        drawAgents(gc, agents);
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        int gridWidth = environment.getGrid().getWidth();
        int gridHeight = environment.getGrid().getHeight();

        // Vertical lines
        for(int x = 0; x <= gridWidth; x++){
            gc.strokeLine(x * cellSize, 0, x * cellSize, gridHeight * cellSize);
        }

        // Horizontal lines
        for(int y = 0; y <= gridHeight; y++){
            gc.strokeLine(0, y * cellSize, gridWidth * cellSize, y * cellSize);
        }
    }

    private void drawCells(GraphicsContext gc) {
        int gridWith = environment.getGrid().getWidth();
        int gridHeight = environment.getGrid().getHeight();

        for(int x = 0; x < gridWith; x++){
            for(int y = 0; y < gridHeight; y++){
                CellType type = environment.getCell(x, y).getType();
                switch (type){
                    case NEST:
                        drawNest(gc, x, y);
                        break;
                    case FOOD:
                        drawFood(gc, x, y);
                        break;
                    case OBSTACLE:
                        drawObstacle(gc, x, y);
                        break;
                    case EMPTY:
                        // No need to draw empty cells
                }
            }
        }
    }

    private void drawAgents(GraphicsContext gc, List<FakeAgents> agents) {
        gc.setFill(Color.RED);

        for(FakeAgents agent: agents){
            int x = agent.getX() * cellSize + cellSize/2;
            int y = agent.getY() * cellSize + cellSize/2;
            int radius = cellSize/3;

            // Agent -> circle
            gc.fillOval(x, y, radius, radius);

            // Outline
            gc.setStroke(Color.DARKRED);
            gc.setLineWidth(2);
            gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }

    public void drawNest(GraphicsContext gc, int x, int y) {
        gc.setFill(Color.BROWN);
        gc.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

        // Draw 'N' text
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(14));
        gc.fillText("N", x * cellSize + cellSize / 3, y * cellSize + cellSize / 1.5);
    }

    public void drawFood(GraphicsContext gc, int x, int y) {
        gc.setFill(Color.GREEN);
        int padding = 5;
        gc.fillOval(
                x * cellSize + padding,
                y * cellSize + padding,
                cellSize - 2 * padding,
                cellSize - 2 * padding
        );
    }

    private void drawObstacle(GraphicsContext gc, int x, int y) {
        gc.setFill(Color.BLACK);
        gc.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
    }
}
