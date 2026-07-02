package it.unibo.antsim.model.environment;

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
}
