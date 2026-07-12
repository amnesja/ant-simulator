package it.unibo.antsim.config;

public final class SimulationConfig {
    // World (continuous space sampled on a cell grid). Large enough to be explored
    // by zooming/panning the camera instead of being shown all at once.
    public static final int WORLD_WIDTH = 600;
    public static final int WORLD_HEIGHT = 600;

    // Colony (mutable: tunable from the UI at runtime). Sized for a large world.
    public static int INITIAL_AGENT_COUNT = 2000;

    // Nest and food cluster (world cell coordinates)
    public static final int NEST_X = 24;
    public static final int NEST_Y = 80;
    // Mutable: tunable from the UI (applied on "Nuovo mondo")
    public static double FOOD_CLUSTER_RADIUS = 14.0;
    public static int FOOD_CLUSTER_HP = 120;

    // Optional periodic food regeneration (set false to keep a single cluster)
    public static final boolean ENABLE_FOOD_REGEN = false;
    public static final int FOOD_GENERATION_INTERVAL = 1000;

    // Continuous movement (sensor-based steering, like Ant Simulator 2)
    // All mutable so the user can tweak them live from the control panel.
    public static double ANT_SPEED = 0.5;            // cells per simulation step
    public static double SENSOR_DISTANCE = 5.0;       // how far ahead ants sense
    public static double SENSOR_ANGLE = 0.6;          // radians from heading (left/right)
    public static double TURN_STRENGTH = 0.4;         // max turn toward sensed target
    public static double WANDER_STRENGTH = 0.22;       // random jitter each step
    public static double FOOD_SENSE_BONUS = 12.0;     // attraction when food is in sight
    public static double NEST_SENSE_BONUS = 12.0;     // attraction when nest is in sight

    // Pheromones
    public static double PHEROMONE_DEPOSIT_AMOUNT = 0.9;
    // Total pheromone a returning ant lays along its trip; divided by the trip
    // length so shorter (optimal) paths are reinforced more strongly.
    public static double PHEROMONE_DEPOSIT_BUDGET = 60.0;
    public static double PHEROMONE_EVAPORATION_RATE = 0.96;
    public static double MAX_PHEROMONE_LEVEL = 80.0;

    // The nest constantly emits a "home" gradient so returning ants can find their way back
    public static double NEST_HOME_EMIT_RADIUS = 26.0;
    public static double NEST_HOME_EMIT_STRENGTH = 1.5;

    // Natural obstacles: organic rock clusters that block some routes so ants
    // must discover the best path around them.
    // Mutable: tunable from the UI (applied on "Nuovo mondo")
    public static int OBSTACLE_CLUSTERS = 16;
    public static final double OBSTACLE_MIN_RADIUS = 6.0;
    public static final double OBSTACLE_MAX_RADIUS = 13.0;
    public static final double OBSTACLE_CLEAR_RADIUS = 18.0; // keep clear around nest & food
    public static final double OBSTACLE_EDGE_MARGIN = 8.0;

    public static final boolean ENABLE_CLI_LOGS = false;

    private SimulationConfig() {
    }
}
