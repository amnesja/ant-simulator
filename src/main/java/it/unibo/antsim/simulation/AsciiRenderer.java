package it.unibo.antsim.simulation;

import it.unibo.antsim.model.CellType;
import it.unibo.antsim.model.Environment;

import java.util.List;

public class AsciiRenderer {

    public static void render(Environment env, List<FakeAgents> agents) {

        int width = env.getGrid().getWidth();
        int height = env.getGrid().getHeight();

        char[][] map = new char[height][width];

        // 1. riempi base
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                CellType type = env.getCell(x, y).getType();

                switch (type) {
                    case FOOD -> map[y][x] = 'F';
                    case NEST -> map[y][x] = 'N';
                    case OBSTACLE -> map[y][x] = '#';
                    default -> map[y][x] = '.';
                }
            }
        }

        // 2. sovrascrivi agenti
        for (FakeAgents agent : agents) {
            map[agent.getY()][agent.getX()] = 'A';
        }

        // 3. stampa
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(map[y][x] + " ");
            }
            System.out.println();
        }

        System.out.println();
    }
}