package it.unibo.antsim.model.environment;

import it.unibo.antsim.config.SimulationConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentTest {

    @Test
    void defaultNestPositionIsOrigin() {
        Environment environment = new Environment(10, 10);

        assertEquals(new Position(0, 0), environment.getNestPosition());
        assertTrue(environment.isNest(0, 0));
    }

    @Test
    void setNestPositionMovesNestCell() {
        Environment environment = new Environment(10, 10);

        environment.setNestPosition(new Position(3, 4));

        assertFalse(environment.isNest(0, 0));
        assertTrue(environment.isNest(3, 4));
        assertEquals(new Position(3, 4), environment.getNestPosition());
    }

    @Test
    void cannotSetNestOutsideGrid() {
        Environment environment = new Environment(10, 10);

        assertThrows(
                IllegalArgumentException.class,
                () -> environment.setNestPosition(new Position(10, 0))
        );
    }

    @Test
    void walkableNeighborsExcludeObstaclesAndOutOfBounds() {
        Environment environment = new Environment(3, 3);
        environment.getCell(1, 0).setType(CellType.OBSTACLE);

        var positions = environment.getWalkableNeighborPositions(0, 0);

        assertFalse(positions.contains(new Position(1, 0)));
        assertTrue(positions.contains(new Position(0, 1)));
        assertEquals(1, positions.size());
    }

    @Test
    void foodClusterGeneratesManyFoodCells() {
        Environment environment = new Environment(60, 60);
        environment.generateFoodCluster(30, 30, 10.0, 200);

        int foodCells = environment.countCellsOfType(CellType.FOOD);
        assertTrue(foodCells > 200, "expected a dense cluster, got " + foodCells);
        assertEquals(200 * foodCells, environment.getTotalFoodHP());
    }

    @Test
    void rockClustersSpawnObstaclesAndKeepNestClear() {
        Environment environment = new Environment(160, 160);
        environment.setNestPosition(new Position(SimulationConfig.NEST_X, SimulationConfig.NEST_Y));
        environment.generateRockClusters();

        int obstacles = environment.countCellsOfType(CellType.OBSTACLE);
        assertTrue(obstacles > 100, "expected several rock cells, got " + obstacles);

        // The nest and its surroundings are never buried by rocks.
        assertFalse(environment.getCell(SimulationConfig.NEST_X, SimulationConfig.NEST_Y).isObstacle());
    }

    @Test
    void continuousSamplingRespectsCellContents() {
        Environment environment = new Environment(20, 20);
        environment.setNestPosition(new Position(5, 5));
        environment.getCell(8, 8).setType(CellType.OBSTACLE);

        assertTrue(environment.isNestAt(5.2, 5.2));
        assertFalse(environment.isNestAt(8.2, 8.2));

        assertTrue(environment.isObstacleAt(8.4, 8.6));
        assertFalse(environment.isObstacleAt(5.2, 5.2));

        environment.depositFoodPheromoneAt(3.5, 3.5, 5.0);
        assertTrue(environment.getFoodPheromoneAt(3.1, 3.9) > 0);
    }
}
