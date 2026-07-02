package it.unibo.antsim.model.environment;

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
            if (cell.getType() == CellType.EMPTY) {
                cell.setType(CellType.FOOD);
                cell.setFoodHP(DEFAULT_FOOD_HP);
                generated++;
            }
        }
    }

    public boolean isFood(int x, int y) {
        return grid.getCell(x, y).hasFood();
    }

    public void consumeFood(int x, int y, int amount) {
        grid.getCell(x, y).consumeFood(amount);
    }
    public void removeFood(int x, int y) {
        Cell cell  = grid.getCell(x, y);
        cell.setFoodHP(0);

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
            }
        }

        System.out.println("generateObstacle: requested=" + obstacleCount +
                " added=" + generated + " current=" + (currentObstacles + generated));
    }

    public void resetObstacles(int obstacleCount) {
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Cell cell = grid.getCell(x, y);
                if (cell.getType() == CellType.OBSTACLE) {
                    cell.setType(CellType.EMPTY);
                }
            }
        }
        generateObstacle(obstacleCount);
    }

    public void resetDynamicElements(int obstacleCount, int foodCount) {
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
        grid.getCell(x, y).addPheromoneLevel(value);
    }
}
