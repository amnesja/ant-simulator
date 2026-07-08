package it.unibo.antsim.config;

import it.unibo.antsim.view.PheromoneViewMode;

public final class ViewConfig {
    public static final int SCENE_WIDTH = 560;
    public static final int SCENE_HEIGHT = 620;
    public static final int CELL_SIZE = 48;
    public static final boolean SHOW_GRID = false;
    public static final PheromoneViewMode PHEROMONE_VIEW_MODE = PheromoneViewMode.BOTH;
    public static final double PHEROMONE_SOFT_RADIUS_MULTIPLIER = 0.95;

    private ViewConfig() {
    }
}
