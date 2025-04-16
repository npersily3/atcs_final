package io.github.noahcraft;

public class World {


    public static final int WORLD_LENGTH = (int) Math.pow(2,16);
    public static final int WORLD_WIDTH = (int) Math.pow(2,16);
    public static final int WORLD_HEIGHT = 512;
    public static final int EXTREME_BIOME_CONSTANT = 75;

    private int[][] heightMap;

    public World() {

        heightMap = new int[WORLD_LENGTH][WORLD_WIDTH];
    }

    private void initNoiseMap(double spread, double heightVariance, double[][] map, int elevationConstant) {

        PerlinNoise perlinNoise = new PerlinNoise();


        for (int i = 0; i < WORLD_LENGTH; i++) {
            for (int j = 0; j < WORLD_WIDTH; j++) {
                int height = (int) (PerlinNoise.noise(i * spread, j * spread) * heightVariance + + elevationConstant);
                heightMap[i][j] = Math.max(0,Math.min(WORLD_HEIGHT,height));
            }
        }
    }
    public void initMaps() {



    }

}
