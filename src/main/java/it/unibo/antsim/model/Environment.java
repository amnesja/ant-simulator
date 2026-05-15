package it.unibo.antsim.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Class representing the environment of the ant simulation.
 * It contains a grid of cells and provides methods to update the environment.
 */
public class Environment {
    private final Grid grid;

    public Environment(int width, int height) {
        this.grid = new Grid(width, height);
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

    public boolean isFood(int x, int y) {
        return grid.getCell(x, y).hasFood();
    }

    public void removeFood(int x, int y) {
        grid.getCell(x, y).setType(CellType.EMPTY);
    }

    public boolean isNest(int x, int y) {
        return grid.getCell(x, y).isNest();
    }

    public void addPheromone(int x, int y, double value) {
        grid.getCell(x, y).addPheromoneLevel(value);
    }
}
