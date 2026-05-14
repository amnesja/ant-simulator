package it.unibo.antsim.simulation;

import it.unibo.antsim.model.Environment;

public class TestMain {
    public static void main(String[] args) {
        Environment env = new Environment(10,10);
        System.out.println(env.getGrid().getCell(0,0).getType());
    }
}
