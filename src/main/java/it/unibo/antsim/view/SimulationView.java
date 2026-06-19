package it.unibo.antsim.view;

import it.unibo.antsim.config.ViewConfig;
import it.unibo.antsim.model.CellType;
import it.unibo.antsim.model.Environment;
import it.unibo.antsim.simulation.FakeAgents;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationView extends Canvas {
    private final Environment environment;
    private final int cellSize = ViewConfig.CELL_SIZE;
    private static final Color SAND = Color.web("#ead7a2");
    private static final Color SAND_ALT = Color.web("#e4cd91");
    private static final Color GRID_LINE = Color.rgb(120, 93, 54, 0.18);
    private static final Color ANT_BODY = Color.web("#2b1a12");
    private static final Color ANT_DETAIL = Color.web("#140c08");

    public SimulationView(Environment environment) {
        this.environment = environment;
        this.setWidth(environment.getGrid().getWidth() * cellSize);
        this.setHeight(environment.getGrid().getHeight() * cellSize);
    }

    public void render(List<FakeAgents> agents) {
        GraphicsContext gc = this.getGraphicsContext2D();

        gc.setFill(SAND);
        gc.fillRect(0, 0, this.getWidth(), this.getHeight());

        drawTerrain(gc);
        drawGrid(gc);
        drawCells(gc);
        drawAgents(gc, agents);
    }

    private void drawTerrain(GraphicsContext gc) {
        int gridWidth = environment.getGrid().getWidth();
        int gridHeight = environment.getGrid().getHeight();

        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                gc.setFill((x + y) % 2 == 0 ? SAND : SAND_ALT);
                gc.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(GRID_LINE);
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
        Map<String, Integer> agentsPerCell = new HashMap<>();
        Map<String, Integer> drawnPerCell = new HashMap<>();

        for (FakeAgents agent : agents) {
            String key = cellKey(agent);
            agentsPerCell.put(key, agentsPerCell.getOrDefault(key, 0) + 1);
        }

        for(FakeAgents agent: agents){
            String key = cellKey(agent);
            int totalInCell = agentsPerCell.get(key);
            int indexInCell = drawnPerCell.getOrDefault(key, 0);
            drawnPerCell.put(key, indexInCell + 1);

            drawAnt(gc, agent, totalInCell, indexInCell);
        }
    }

    private String cellKey(FakeAgents agent) {
        return agent.getX() + ":" + agent.getY();
    }

    private void drawNest(GraphicsContext gc, int x, int y) {
        double baseX = x * cellSize;
        double baseY = y * cellSize;

        gc.setFill(Color.web("#7a4b24"));
        gc.fillOval(baseX + 2, baseY + 6, cellSize - 4, cellSize - 10);

        gc.setFill(Color.web("#9b6735"));
        gc.fillOval(baseX + 5, baseY + 4, cellSize - 10, cellSize - 12);

        gc.setFill(Color.web("#24150d"));
        gc.fillOval(baseX + 9, baseY + 11, cellSize - 18, cellSize - 17);

        gc.setStroke(Color.rgb(48, 30, 18, 0.45));
        gc.setLineWidth(1.2);
        gc.strokeOval(baseX + 2, baseY + 6, cellSize - 4, cellSize - 10);
    }

    private void drawFood(GraphicsContext gc, int x, int y) {
        double baseX = x * cellSize;
        double baseY = y * cellSize;

        gc.setFill(Color.web("#1f8f3a"));
        gc.fillOval(baseX + cellSize * 0.20, baseY + cellSize * 0.42, cellSize * 0.34, cellSize * 0.20);
        gc.fillOval(baseX + cellSize * 0.42, baseY + cellSize * 0.23, cellSize * 0.32, cellSize * 0.20);
        gc.fillOval(baseX + cellSize * 0.48, baseY + cellSize * 0.55, cellSize * 0.30, cellSize * 0.18);

        gc.setStroke(Color.web("#0d5f23"));
        gc.setLineWidth(1.5);
        gc.strokeLine(baseX + cellSize * 0.28, baseY + cellSize * 0.50, baseX + cellSize * 0.75, baseY + cellSize * 0.34);
        gc.strokeLine(baseX + cellSize * 0.34, baseY + cellSize * 0.64, baseX + cellSize * 0.80, baseY + cellSize * 0.66);
    }

    private void drawObstacle(GraphicsContext gc, int x, int y) {
        double baseX = x * cellSize;
        double baseY = y * cellSize;

        gc.setFill(Color.web("#4f4a43"));
        gc.fillOval(baseX + 4, baseY + 8, cellSize - 8, cellSize - 11);

        gc.setFill(Color.web("#6b645b"));
        gc.fillOval(baseX + 7, baseY + 6, cellSize - 15, cellSize - 17);

        gc.setStroke(Color.web("#2f2b27"));
        gc.setLineWidth(1.2);
        gc.strokeOval(baseX + 4, baseY + 8, cellSize - 8, cellSize - 11);
    }

    private void drawAnt(GraphicsContext gc, FakeAgents agent, int totalInCell, int indexInCell){
        double cx = agent.getX() * cellSize + cellSize / 2.0;
        double cy = agent.getY() * cellSize + cellSize / 2.0;

        if (totalInCell > 1) {
            double angle = 2 * Math.PI * indexInCell / totalInCell;
            double radius = Math.min(cellSize * 0.22, 12);
            cx += Math.cos(angle) * radius;
            cy += Math.sin(angle) * radius;
        }

        gc.save();
        gc.translate(cx,cy);
        gc.scale(cellSize / 30.0, cellSize / 30.0);

        gc.setFill(ANT_BODY);
        gc.fillOval(-9, -4, 8, 8); // abs
        gc.fillOval(-2, -5, 8, 10); // chest
        gc.fillOval(5, -4, 8, 8); // head

        gc.setStroke(ANT_DETAIL);
        gc.setLineWidth(1.5);

        // legs
        gc.strokeLine(-2, -3, -9, -10);
        gc.strokeLine(1, -3, -5, -11);
        gc.strokeLine(4, -3, 10, -10);

        gc.strokeLine(-2, 3, -9, 10);
        gc.strokeLine(1, 3, -5, 11);
        gc.strokeLine(4, 3, 10, 10);

        // antenne
        gc.strokeLine(11, -2, 16, -7);
        gc.strokeLine(11, 2, 16, 7);

        if (agent.isCarryingFood()) {
            gc.setFill(Color.LIMEGREEN);
            gc.fillOval(-3, -13, 6, 6);
        }

        gc.restore();
    }
}
