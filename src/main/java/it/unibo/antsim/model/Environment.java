package it.unibo.antsim.model;

/**
 * Class representing the environment of the ant simulation.
 * It contains a grid of cells and provides methods to update the environment.
 */
public class Environment {
    private final Grid grid;

    public Environment(int width, int height) {
        this.grid = new Grid(width, height);
    }

    public Grid getGrid() {
        return grid;
    }

    public void update() {

    }
}
