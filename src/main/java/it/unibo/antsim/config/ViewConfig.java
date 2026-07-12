package it.unibo.antsim.config;

import it.unibo.antsim.view.PheromoneViewMode;

public final class ViewConfig {
    public static final int SCENE_WIDTH = 900;
    public static final int SCENE_HEIGHT = 740;
    public static final int VIEW_WIDTH = 760;
    public static final int VIEW_HEIGHT = 700;
    public static final boolean SHOW_GRID = false;
    public static final PheromoneViewMode PHEROMONE_VIEW_MODE = PheromoneViewMode.BOTH;
    public static final double PHEROMONE_SOFT_RADIUS_MULTIPLIER = 0.95;

    private ViewConfig() {
    }
}
