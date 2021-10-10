package br.ucsal;

import java.util.Random;

public class Util {
    public static int generateRandomTime() {
        return (new Random().nextInt(16) + 5) * 1000;
    }
}
