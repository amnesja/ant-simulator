package it.unibo.antsim.config;

public final class SimulationConfig {
    public static final int GRID_WIDTH = 15;
    public static final int GRID_HEIGHT = 15;
    public static final int INITIAL_AGENT_COUNT = 10;
    public static final int INITIAL_FOOD_COUNT = 1;
    public static final int INITIAL_OBSTACLE_COUNT = 7;
    public static final int FOOD_GENERATION_INTERVAL = 1000;
    public static final double PHEROMONE_DEPOSIT_AMOUNT = 1.0;
    public static final double ACO_EPSILON = 0.1;
    public static final double PHEROMONE_EVAPORATION_RATE = 0.95;
    public static final double MAX_PHEROMONE_LEVEL = 10.0;
    public static final double RETURN_DISTANCE_WEIGHT = 2.0;
    public static final double RETURN_HOME_PHEROMONE_WEIGHT = 1.0;
    public static final double RETURN_BACKTRACK_PENALTY = 3.0;
    public static final boolean ENABLE_CLI_LOGS = true;

    private SimulationConfig() {
    }
}
