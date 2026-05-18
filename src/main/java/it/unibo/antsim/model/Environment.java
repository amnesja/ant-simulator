package it.unibo.antsim.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Class representing the environment of the ant simulation.
 * It contains a grid of cells and provides methods to update the environment.
 */
public class Environment {
    private final Grid grid;
    private static final Random RANDOM = new Random();

    public Environment(int width, int height) {
        this.grid = new Grid(width, height);
    }

    public Grid getGrid() {
        return grid;
    }

    public Cell getCell(int x, int y) {
        return grid.getCell(x, y);
    }

    /**
        * Returns a list of neighboring cells (up, down, left, right) for the given coordinates.
     */
    public List<Cell> getNeighbors(int x, int y) {
        List<Cell> neighbors = new ArrayList<>();

        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, -1, 0, 1};

        for (int i = 0; i < 4; i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];

            if (grid.isInside(newX, newY)) {
                neighbors.add(grid.getCell(newX, newY));
            }
        }
        return neighbors;
    }

    public void update() {
        // Evaporate pheromones in all cells
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                grid.getCell(x, y).evaporate(0.95); // Evaporation rate
            }
        }
    }

    public void generateFood(int foodCount) {
        int attempts = 0;
        int maxAttempts = foodCount * 10;

        for(int generated = 0; generated < foodCount && attempts < maxAttempts; attempts++){
            int x = RANDOM.nextInt(grid.getWidth());
            int y = RANDOM.nextInt(grid.getHeight());

            Cell cell = grid.getCell(x, y);
            if (cell.getType() == CellType.EMPTY && !(x == 0 && y == 0)) {
                cell.setType(CellType.FOOD);
                generated++;
            }
        }
    }

    public boolean isFood(int x, int y) {
        return grid.getCell(x, y).hasFood();
    }

    public void removeFood(int x, int y) {
        grid.getCell(x, y).setType(CellType.EMPTY);
    }

    public void generateObstacle(int obstacleCount) {
        int attempts = 0;
        int maxAttempts = obstacleCount * 10;

        for(int generated = 0; generated < obstacleCount && attempts < maxAttempts; attempts++){
            int  x = RANDOM.nextInt(grid.getWidth());
            int y = RANDOM.nextInt(grid.getHeight());

            Cell cell = grid.getCell(x, y);
            if (cell.getType() == CellType.EMPTY && !(x == 0 && y == 0)) {
                cell.setType(CellType.OBSTACLE);
                generated++;
            }
        }
    }

    public boolean isNest(int x, int y) {
        return grid.getCell(x, y).isNest();
    }

    public void addPheromone(int x, int y, double value) {
        grid.getCell(x, y).addPheromoneLevel(value);
    }
}
