package it.unibo.antsim.model.environment;

import it.unibo.antsim.config.SimulationConfig;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Class representing the environment of the ant simulation.
 * It contains a grid of cells and provides methods to update the environment.
 */
public class Environment {
    private Position nestPosition;
    private final Grid grid;
    private static final Random RANDOM = new Random();
    private static final int DEFAULT_FOOD_HP = 100;

    public Environment(int width, int height) {
        this.grid = new Grid(width, height);
        setNestPosition(new Position(0, 0));
        log("created grid=%dx%d nest=%s", width, height, nestPosition);
    }

    public void setNestPosition(Position position){
        if(!grid.isInside(position.x(), position.y())){
            throw new IllegalArgumentException("Nest position is out of bounds, it must be inside the grid!");
        }

        if(nestPosition != null && grid.isInside(nestPosition.x(), nestPosition.y())){
            grid.getCell(nestPosition.x(), nestPosition.y()).setType(CellType.EMPTY);
        }

        nestPosition = position;
        grid.getCell(position.x(), position.y()).setType(CellType.NEST);
        log("nest set at %s", position);
    }

    public Position getNestPosition(){
        return nestPosition;
    }
    public Grid getGrid() {
        return grid;
    }

    public Cell getCell(int x, int y) {
        return grid.getCell(x, y);
    }

    public List<Position> getWalkableNeighborPositions(int x, int y) {
        List<Position> positions = new ArrayList<>();

        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, -1, 0, 1};

        for (int i = 0; i < 4; i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];

            if (grid.isInside(newX, newY) && !grid.getCell(newX, newY).isObstacle()) {
                positions.add(new Position(newX, newY));
            }
        }

        return positions;
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
                grid.getCell(x, y).evaporate(SimulationConfig.PHEROMONE_EVAPORATION_RATE); // Evaporation rate
            }
        }
        emitHomeGradient();
        log("evaporated pheromones rate=%.2f foodTrailTotal=%.2f homeTrailTotal=%.2f",
                SimulationConfig.PHEROMONE_EVAPORATION_RATE,
                totalFoodPheromone(),
                totalHomePheromone());
    }

    /**
     * The nest constantly emits a "home" pheromone gradient so that ants carrying
     * food can always sense their way back, even far from any trail.
     */
    private void emitHomeGradient() {
        int nx = nestPosition.x();
        int ny = nestPosition.y();
        int r = (int) SimulationConfig.NEST_HOME_EMIT_RADIUS;

        for (int x = Math.max(0, nx - r); x <= Math.min(grid.getWidth() - 1, nx + r); x++) {
            for (int y = Math.max(0, ny - r); y <= Math.min(grid.getHeight() - 1, ny + r); y++) {
                double dist = Math.hypot(x - nx, y - ny);
                if (dist > r) continue;
                double strength = SimulationConfig.NEST_HOME_EMIT_STRENGTH * (1.0 - dist / r);
                grid.getCell(x, y).addHomePheromoneLevel(strength);
            }
        }
    }

    public void generateFood(int foodCount) {
        int attempts = 0;
        int maxAttempts = foodCount * 10;
        int generated = 0;

        for(; generated < foodCount && attempts < maxAttempts; attempts++){
            int x = RANDOM.nextInt(grid.getWidth());
            int y = RANDOM.nextInt(grid.getHeight());

            Cell cell = grid.getCell(x, y);
            if (cell.getType() == CellType.EMPTY) {
                cell.setType(CellType.FOOD);
                cell.setFoodHP(DEFAULT_FOOD_HP);
                log("food generated at (%d,%d) hp=%d attempt=%d", x, y, DEFAULT_FOOD_HP, attempts + 1);
                generated++;
            } else {
                log("food generation skipped at (%d,%d) type=%s attempt=%d", x, y, cell.getType(), attempts + 1);
            }
        }
        log("generateFood requested=%d generated=%d attempts=%d totalFoodHP=%d",
                foodCount, generated, attempts, getTotalFoodHP());
    }

    public void consumeFood(int x, int y, int amount) {
        int before = grid.getCell(x, y).getFoodHP();
        grid.getCell(x, y).consumeFood(amount);
        int after = grid.getCell(x, y).getFoodHP();
        log("food consumed at (%d,%d) amount=%d hp=%d->%d type=%s",
                x, y, amount, before, after, grid.getCell(x, y).getType());
    }

    public int countCellsOfType(CellType type) {
        int count = 0;
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                if (grid.getCell(x, y).getType() == type) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getTotalFoodHP() {
        int totalFoodHP = 0;
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Cell cell = grid.getCell(x, y);
                if (cell.hasFood()) {
                    totalFoodHP += cell.getFoodHP();
                }
            }
        }
        return totalFoodHP;
    }

    public void generateObstacle(int obstacleCount) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int totalCells = width * height;

        // non superare il 30% della griglia con ostacoli (configurabile)
        int maxObstaclesAllowed = (int) (totalCells * 0.30);
        int currentObstacles = countCellsOfType(CellType.OBSTACLE);
        int canAdd = Math.max(0, maxObstaclesAllowed - currentObstacles);
        int toGenerate = Math.min(obstacleCount, canAdd);
        if (toGenerate <= 0) {
            // niente da fare
            log("generateObstacle requested=%d added=0 current=%d maxAllowed=%d",
                    obstacleCount, currentObstacles, maxObstaclesAllowed);
            return;
        }

        int generated = 0;
        int attempts = 0;
        int maxAttempts = toGenerate * 20; // più tentativi per trovare celle libere

        while (generated < toGenerate && attempts < maxAttempts) {
            int x = RANDOM.nextInt(width);
            int y = RANDOM.nextInt(height);
            attempts++;
            Cell cell = grid.getCell(x, y);
            if (cell.getType() == CellType.EMPTY && !cell.isNest()) {
                cell.setType(CellType.OBSTACLE);
                generated++;
                log("obstacle generated at (%d,%d) attempt=%d", x, y, attempts);
            } else {
                log("obstacle skipped at (%d,%d) type=%s attempt=%d", x, y, cell.getType(), attempts);
            }
        }

        log("generateObstacle requested=%d added=%d current=%d",
                obstacleCount, generated, currentObstacles + generated);
    }

    public void resetObstacles(int obstacleCount) {
        log("resetObstacles requested=%d", obstacleCount);
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Cell cell = grid.getCell(x, y);
                if (cell.getType() == CellType.OBSTACLE) {
                    cell.setType(CellType.EMPTY);
                    log("obstacle cleared at (%d,%d)", x, y);
                }
            }
        }
        generateObstacle(obstacleCount);
    }

    /**
     * Generates a single, dense cluster of food with a large amount of resources.
     */
    public void generateFoodCluster(int cx, int cy, double radius, int hpPerCell) {
        int r = (int) Math.ceil(radius);
        int generated = 0;

        for (int x = cx - r; x <= cx + r; x++) {
            for (int y = cy - r; y <= cy + r; y++) {
                if (!grid.isInside(x, y)) continue;
                if (Math.hypot(x - cx, y - cy) > radius) continue;
                Cell cell = grid.getCell(x, y);
                if (cell.isNest() || cell.isObstacle()) continue;
                cell.setType(CellType.FOOD);
                cell.setFoodHP(hpPerCell);
                generated++;
            }
        }
        log("food cluster generated center=(%d,%d) radius=%.1f cells=%d hp=%d",
                cx, cy, radius, generated, hpPerCell);
    }

    /**
     * Places a single dense food cluster at a random location that keeps a safe
     * distance from the nest and avoids existing rock cells, so every reset
     * produces a fresh world layout (the food no longer stays in a fixed spot).
     */
    public void generateRandomFoodCluster() {
        int margin = (int) Math.ceil(SimulationConfig.FOOD_CLUSTER_RADIUS) + 2;
        int minDistFromNest = (int) SimulationConfig.OBSTACLE_CLEAR_RADIUS;

        int cx = -1;
        int cy = -1;
        for (int attempts = 0; attempts < 200; attempts++) {
            int x = margin + RANDOM.nextInt(Math.max(1, grid.getWidth() - 2 * margin));
            int y = margin + RANDOM.nextInt(Math.max(1, grid.getHeight() - 2 * margin));

            if (Math.hypot(x - nestPosition.x(), y - nestPosition.y()) < minDistFromNest) continue;
            if (grid.getCell(x, y).isObstacle()) continue;

            cx = x;
            cy = y;
            break;
        }
        if (cx < 0) {
            cx = grid.getWidth() / 2;
            cy = grid.getHeight() / 2;
        }

        generateFoodCluster(cx, cy, SimulationConfig.FOOD_CLUSTER_RADIUS, SimulationConfig.FOOD_CLUSTER_HP);
    }

    /**
     * Scatters organic rock clusters across the world. They block some routes
     * between the nest and the food so the colony must discover the best path.
     */
    public void generateRockClusters() {
        int placed = 0;
        int attempts = 0;
        int maxAttempts = SimulationConfig.OBSTACLE_CLUSTERS * 40;

        while (placed < SimulationConfig.OBSTACLE_CLUSTERS && attempts < maxAttempts) {
            attempts++;
            int cx = (int) SimulationConfig.OBSTACLE_EDGE_MARGIN
                    + RANDOM.nextInt(grid.getWidth() - (int) (2 * SimulationConfig.OBSTACLE_EDGE_MARGIN));
            int cy = (int) SimulationConfig.OBSTACLE_EDGE_MARGIN
                    + RANDOM.nextInt(grid.getHeight() - (int) (2 * SimulationConfig.OBSTACLE_EDGE_MARGIN));
            double radius = SimulationConfig.OBSTACLE_MIN_RADIUS
                    + RANDOM.nextDouble() * (SimulationConfig.OBSTACLE_MAX_RADIUS - SimulationConfig.OBSTACLE_MIN_RADIUS);

            if (Math.hypot(cx - nestPosition.x(), cy - nestPosition.y()) < SimulationConfig.OBSTACLE_CLEAR_RADIUS) continue;

            int r = (int) Math.ceil(radius);
            int cells = 0;
            for (int x = cx - r; x <= cx + r; x++) {
                for (int y = cy - r; y <= cy + r; y++) {
                    if (!grid.isInside(x, y)) continue;
                    if (Math.hypot(x - cx, y - cy) > radius) continue;
                    Cell cell = grid.getCell(x, y);
                    if (cell.isNest() || cell.hasFood()) continue;
                    cell.setType(CellType.OBSTACLE);
                    cells++;
                }
            }
            placed += (cells > 0 ? 1 : 0);
        }
        log("rock clusters generated=%d", placed);
    }

    public void resetDynamicElements(int obstacleCount, int foodCount) {
        log("resetDynamicElements obstacleCount=%d foodCount=%d", obstacleCount, foodCount);
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Cell cell = grid.getCell(x, y);
                cell.evaporate(0.0);

                if (!cell.isNest()) {
                    cell.setType(CellType.EMPTY);
                    cell.setFoodHP(0);
                }
            }
        }

        generateRockClusters();
        generateRandomFoodCluster();
    }

    public boolean isNest(int x, int y) {
        return grid.getCell(x, y).isNest();
    }

    // --- Continuous-space sampling (used by sensor-based ant movement) ---

    public boolean isObstacleAt(double x, double y) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        if (!grid.isInside(cx, cy)) return true; // out of bounds acts as a wall
        return grid.getCell(cx, cy).isObstacle();
    }

    /**
     * A position ants cannot enter: out of bounds, an obstacle, or a food cell.
     * Food blocks movement exactly like an obstacle (ants route around it and
     * pick it up from an adjacent cell instead of standing on top of it).
     */
    public boolean isBlockedAt(double x, double y) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        if (!grid.isInside(cx, cy)) return true;
        Cell cell = grid.getCell(cx, cy);
        return cell.isObstacle() || cell.hasFood();
    }

    /**
     * Returns the coordinates of a food cell the given cell touches (itself or a
     * 4-neighbor), or null when no food is adjacent. Used so ants collect food
     * from the perimeter of a food pile without ever stepping onto it.
     */
    public Position findFoodCellNear(int x, int y) {
        if (grid.isInside(x, y) && grid.getCell(x, y).hasFood()) {
            return new Position(x, y);
        }
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, -1, 0, 1};
        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (grid.isInside(nx, ny) && grid.getCell(nx, ny).hasFood()) {
                return new Position(nx, ny);
            }
        }
        return null;
    }

    public boolean isFoodAt(double x, double y) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        return grid.isInside(cx, cy) && grid.getCell(cx, cy).hasFood();
    }

    public boolean isNestAt(double x, double y) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        return grid.isInside(cx, cy) && grid.getCell(cx, cy).isNest();
    }

    public double getFoodPheromoneAt(double x, double y) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        if (!grid.isInside(cx, cy)) return 0.0;
        return grid.getCell(cx, cy).getPheromoneLevel();
    }

    public double getHomePheromoneAt(double x, double y) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        if (!grid.isInside(cx, cy)) return 0.0;
        return grid.getCell(cx, cy).getHomePheromoneLevel();
    }

    public void depositFoodPheromoneAt(double x, double y, double value) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        if (grid.isInside(cx, cy)) grid.getCell(cx, cy).addPheromoneLevel(value);
    }

    public void depositHomePheromoneAt(double x, double y, double value) {
        int cx = (int) Math.floor(x);
        int cy = (int) Math.floor(y);
        if (grid.isInside(cx, cy)) grid.getCell(cx, cy).addHomePheromoneLevel(value);
    }

    private double totalFoodPheromone() {
        double total = 0.0;
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                total += grid.getCell(x, y).getPheromoneLevel();
            }
        }
        return total;
    }

    private double totalHomePheromone() {
        double total = 0.0;
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                total += grid.getCell(x, y).getHomePheromoneLevel();
            }
        }
        return total;
    }

    private void log(String format, Object... args) {
        if (SimulationConfig.ENABLE_CLI_LOGS) {
            System.out.printf("[ENV] %s%n", String.format(format, args));
        }
    }
}
