package it.unibo.antsim.model.agent;

import it.unibo.antsim.model.environment.Cell;
import it.unibo.antsim.model.environment.Environment;
import it.unibo.antsim.model.environment.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AcoTest {

    @Test
    public void testAntPrefersPheromonePath() {
        // Create a 3x3 environment
        Environment env = new Environment(3, 3);
        
        // Position the ant at (1,1)
        Ant ant = new Ant(1, 1);
        
        // Neighbors of (1,1) are (0,1), (2,1), (1,0), (1,2)
        // Let's put high pheromone on (0,1)
        env.addPheromone(0, 1, 10.0);
        
        int pheromonePathCount = 0;
        int iterations = 100;
        
        for (int i = 0; i < iterations; i++) {
            // We need to reset the ant position for each iteration to make it controlled
            // But Ant doesn't have a setPosition method. 
            // I will use reflection or just assume I can't easily reset it without a change.
            // Let's modify Ant to allow setting position for testing purposes, or just use a new Ant each time.
            
            Ant testAnt = new Ant(1, 1);
            testAnt.move(env);
            
            if (testAnt.getX() == 0 && testAnt.getY() == 1) {
                pheromonePathCount++;
            }
        }
        
        // With epsilon 0.1 and pheromone 10.0, the probability of picking (0,1) 
        // should be roughly 10 / (10 + 0.1 + 0.1 + 0.1 + 0.1) = 10 / 10.4 approx 96%
        // Even with random movement, it would be 25% (if there are 4 neighbors)
        
        assertTrue(pheromonePathCount > (iterations / 2), 
            "Ant should prefer the pheromone path. Got: " + pheromonePathCount + "/" + iterations);
    }
}
