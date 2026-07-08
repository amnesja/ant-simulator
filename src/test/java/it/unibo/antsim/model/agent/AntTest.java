package it.unibo.antsim.model.agent;

import it.unibo.antsim.model.environment.CellType;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.environment.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntTest {

    @Test
    void antStartsSearchingFood() {
        Ant ant = new Ant(0, 0);

        assertEquals(AntState.SEARCHING_FOOD, ant.getState());
        assertFalse(ant.isCarryingFood());
    }

    @Test
    void pickAndDropFoodChangeState() {
        Ant ant = new Ant(0, 0);

        ant.pickFood();

        assertEquals(AntState.RETURNING_TO_NEST, ant.getState());
        assertTrue(ant.isCarryingFood());

        ant.dropFood();

        assertEquals(AntState.SEARCHING_FOOD, ant.getState());
        assertFalse(ant.isCarryingFood());
    }

    @Test
    void searchingAntMovesToAdjacentFoodWhenOnlyFoodCellIsWalkable() {
        Environment environment = new Environment(3, 3);
        Ant ant = new Ant(1, 1);

        environment.getCell(0, 1).setType(CellType.OBSTACLE);
        environment.getCell(1, 0).setType(CellType.OBSTACLE);
        environment.getCell(2, 1).setType(CellType.OBSTACLE);
        environment.getCell(1, 2).setType(CellType.FOOD);

        ant.move(environment);

        assertEquals(1, ant.getX());
        assertEquals(2, ant.getY());
    }

    @Test
    void searchingAntDepositsHomePheromoneBeforeMoving() {
        Environment environment = new Environment(3, 3);
        Ant ant = new Ant(1, 1);

        ant.move(environment);

        assertTrue(environment.getCell(1, 1).getHomePheromoneLevel() > 0);
    }

    @Test
    void returningAntMovesTowardConfiguredNest() {
        Environment environment = new Environment(4, 4);
        environment.setNestPosition(new Position(0, 0));
        Ant ant = new Ant(1, 1);
        ant.pickFood();

        environment.getCell(2, 1).setType(CellType.OBSTACLE);
        environment.getCell(1, 2).setType(CellType.OBSTACLE);

        ant.move(environment);

        Position newPosition = new Position(ant.getX(), ant.getY());
        assertTrue(
                newPosition.equals(new Position(0, 1))
                        || newPosition.equals(new Position(1, 0))
        );
    }

    @Test
    void returningAntFollowsHomePheromoneTrail() {
        Environment environment = new Environment(4, 4);
        environment.setNestPosition(new Position(0, 0));
        Ant ant = new Ant(2, 1);
        ant.pickFood();

        environment.addHomePheromone(2, 2, 5.0);

        ant.move(environment);

        assertEquals(2, ant.getX());
        assertEquals(2, ant.getY());
    }
}
