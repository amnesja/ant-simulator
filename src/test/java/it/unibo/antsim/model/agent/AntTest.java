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
    void searchingAntDepositsHomePheromone() {
        Environment environment = new Environment(10, 10);
        Ant ant = new Ant(5, 5);

        ant.move(environment);

        assertTrue(environment.getCell(5, 5).getHomePheromoneLevel() > 0);
    }

    @Test
    void returningAntDepositsFoodPheromone() {
        Environment environment = new Environment(10, 10);
        environment.setNestPosition(new Position(0, 0));
        Ant ant = new Ant(0, 0);
        ant.pickFood();

        ant.move(environment);

        assertTrue(environment.getCell(0, 0).getPheromoneLevel() > 0);
    }

    @Test
    void antStaysInsideWorldAndAvoidsObstacles() {
        Environment environment = new Environment(30, 30);
        environment.getCell(15, 14).setType(CellType.OBSTACLE);

        Ant ant = new Ant(15, 15);

        for (int i = 0; i < 400; i++) {
            ant.move(environment);
        }

        int x = ant.getX();
        int y = ant.getY();
        assertTrue(x >= 0 && x < 30, "x out of bounds: " + x);
        assertTrue(y >= 0 && y < 30, "y out of bounds: " + y);
        assertFalse(environment.getCell(x, y).isObstacle(), "ant ended on an obstacle");
    }
}
