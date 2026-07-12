package it.unibo.antsim.model.agent;

import it.unibo.antsim.config.SimulationConfig;
import it.unibo.antsim.model.environment.Environment;

import java.util.Random;

public class Ant {
    private static int nextId = 1;
    private static final Random RANDOM = new Random();

    private final int id;
    private double x;
    private double y;
    private double heading;
    private AntState state;
    private int tripSteps;

    public Ant(int cellX, int cellY) {
        this.id = nextId++;
        this.x = cellX + 0.5;
        this.y = cellY + 0.5;
        this.heading = randomAngle();
        this.state = AntState.SEARCHING_FOOD;
    }

    public void move(Environment env) {
        tripSteps++;

        // Deposit the trail that the other phase follows:
        //  - searching ants lay a "home" trail (so carriers can return)
        //  - returning ants lay a "food" trail (so searchers can find food)
        if (state == AntState.RETURNING_TO_NEST) {
            // ACO reinforcement: shorter foraging trips deposit more pheromone
            // per step, so the shortest nest->food path is strengthened most
            // and emerges as the optimal trail.
            env.depositFoodPheromoneAt(x, y, SimulationConfig.PHEROMONE_DEPOSIT_BUDGET / tripSteps);
        } else {
            env.depositHomePheromoneAt(x, y, SimulationConfig.PHEROMONE_DEPOSIT_AMOUNT);
        }

        steer(env);

        double nx = x + Math.cos(heading) * SimulationConfig.ANT_SPEED;
        double ny = y + Math.sin(heading) * SimulationConfig.ANT_SPEED;

        if (env.isBlockedAt(nx, ny)) {
            // Blocked: turn away and stay put this step.
            heading += Math.PI * (0.5 + RANDOM.nextDouble());
        } else {
            x = nx;
            y = ny;
        }
    }

    private void steer(Environment env) {
        double leftAngle = heading - SimulationConfig.SENSOR_ANGLE;
        double rightAngle = heading + SimulationConfig.SENSOR_ANGLE;

        double center = sense(env, heading);
        double left = sense(env, leftAngle);
        double right = sense(env, rightAngle);

        double best = Math.max(center, Math.max(left, right));

        if (best > 0) {
            double target;
            if (best == center) {
                target = heading;
            } else if (best == left) {
                target = leftAngle;
            } else {
                target = rightAngle;
            }
            turnToward(target);
        }

        // Random wander keeps exploration alive and breaks symmetry.
        heading += (RANDOM.nextDouble() * 2.0 - 1.0) * SimulationConfig.WANDER_STRENGTH;
    }

    private double sense(Environment env, double angle) {
        double sx = x + Math.cos(angle) * SimulationConfig.SENSOR_DISTANCE;
        double sy = y + Math.sin(angle) * SimulationConfig.SENSOR_DISTANCE;

        if (state == AntState.SEARCHING_FOOD) {
            double value = env.getFoodPheromoneAt(sx, sy);
            if (env.isFoodAt(sx, sy)) value += SimulationConfig.FOOD_SENSE_BONUS;
            return value;
        } else {
            double value = env.getHomePheromoneAt(sx, sy);
            if (env.isNestAt(sx, sy)) value += SimulationConfig.NEST_SENSE_BONUS;
            return value;
        }
    }

    private void turnToward(double targetAngle) {
        double diff = normalizeAngle(targetAngle - heading);
        diff = Math.max(-SimulationConfig.TURN_STRENGTH, Math.min(SimulationConfig.TURN_STRENGTH, diff));
        heading = normalizeAngle(heading + diff);
    }

    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    private double randomAngle() {
        return RANDOM.nextDouble() * 2 * Math.PI;
    }

    public void pickFood() {
        state = AntState.RETURNING_TO_NEST;
        tripSteps = 0;
    }

    public void dropFood() {
        state = AntState.SEARCHING_FOOD;
    }

    public boolean isCarryingFood() {
        return state == AntState.RETURNING_TO_NEST;
    }

    public AntState getState() {
        return state;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return (int) Math.floor(x);
    }

    public int getY() {
        return (int) Math.floor(y);
    }

    public double getDoubleX() {
        return x;
    }

    public double getDoubleY() {
        return y;
    }

    public double getHeading() {
        return heading;
    }
}
