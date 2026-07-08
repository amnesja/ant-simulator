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
        log("evaporated pheromones rate=%.2f foodTrailTotal=%.2f homeTrailTotal=%.2f",
                SimulationConfig.PHEROMONE_EVAPORATION_RATE,
                totalFoodPheromone(),
                totalHomePheromone());
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

    public boolean isFood(int x, int y) {
        return grid.getCell(x, y).hasFood();
    }

    public void consumeFood(int x, int y, int amount) {
        int before = grid.getCell(x, y).getFoodHP();
        grid.getCell(x, y).consumeFood(amount);
        int after = grid.getCell(x, y).getFoodHP();
        log("food consumed at (%d,%d) amount=%d hp=%d->%d type=%s",
                x, y, amount, before, after, grid.getCell(x, y).getType());
    }
    public void removeFood(int x, int y) {
        Cell cell  = grid.getCell(x, y);
        cell.setFoodHP(0);
        log("food removed at (%d,%d)", x, y);

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

        generateObstacle(obstacleCount);
        generateFood(foodCount);
    }

    public boolean isNest(int x, int y) {
        return grid.getCell(x, y).isNest();
    }

    public void addPheromone(int x, int y, double value) {
        double before = grid.getCell(x, y).getPheromoneLevel();
        grid.getCell(x, y).addPheromoneLevel(value);
        log("food-pheromone added at (%d,%d) amount=%.2f level=%.2f->%.2f",
                x, y, value, before, grid.getCell(x, y).getPheromoneLevel());
    }

    public void addHomePheromone(int x, int y, double value) {
        double before = grid.getCell(x, y).getHomePheromoneLevel();
        grid.getCell(x, y).addHomePheromoneLevel(value);
        log("home-pheromone added at (%d,%d) amount=%.2f level=%.2f->%.2f",
                x, y, value, before, grid.getCell(x, y).getHomePheromoneLevel());
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
