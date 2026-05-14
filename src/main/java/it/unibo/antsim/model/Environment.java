package it.unibo.antsim.model;

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
