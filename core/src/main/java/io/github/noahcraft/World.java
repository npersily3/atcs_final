package main.java.io.github.noahcraft;

import java.util.LinkedList;
import java.util.Queue;

public class World {


    public static final int WORLD_LENGTH = 5000;
    public static final int WORLD_WIDTH = 5000;
    public static final int WORLD_HEIGHT = 512, MAX_HEAT = 100;
    // Biome types
    public static final int OCEAN = 0;
    public static final int MOUNTAIN = 1;
    public static final int DESERT = 2;          // High heat, Low moisture
    public static final int SAVANNA = 3;         // High heat, Medium moisture
    public static final int RAINFOREST = 4;      // High heat, High moisture
    public static final int PLAINS = 5;          // Medium heat, Low moisture
    public static final int GRASSLAND = 6;       // Medium heat, Medium moisture
    public static final int SEASONAL_FOREST = 7; // Medium heat, High moisture
    public static final int TUNDRA = 8;          // Low heat, Low moisture
    public static final int TAIGA = 9;           // Low heat, Medium moisture
    public static final int BOREAL_FOREST = 10;
    public static final int RIVER = 11;// Low heat, High moisture
    public static final int[] ALL_BIOMES = {
        OCEAN, MOUNTAIN, DESERT, SAVANNA, RAINFOREST,
        PLAINS, GRASSLAND, SEASONAL_FOREST, TUNDRA, TAIGA,
        BOREAL_FOREST, RIVER
    };

    // Thresholds
    public static final int OCEAN_THRESHOLD = 185;       // Below this is ocean
    public static final int MOUNTAIN_THRESHOLD = WORLD_HEIGHT - 150;   // Above this is mountain
    public static final int LOW_TEMP_THRESHOLD = 33;    // Below this is "low temperature"
    public static final int HIGH_TEMP_THRESHOLD = 66;  // Above this is "high temperature"
    public static final int LOW_MOISTURE_THRESHOLD = 33; // Below this is "low moisture"
    public static final int HIGH_MOISTURE_THRESHOLD = 66; // Above this is "high moisture"


    public static final int DEFAULT_HEIGHT_VARIANCE = WORLD_HEIGHT/2;
    public static final int DEFAULT_HEIGHT_INCREASE = WORLD_HEIGHT/2;
    public static final int DEFAULT_TEMP_VARIANCE = 50, DEFAULT_TEMP_INCREASE = 50;
    public static final int DEFAULT_MOIST_VARIANCE = 50, DEFAULT_MOIST_INCREASE = 50;

    // Add these variables to your World class
    private boolean[][] visited; // For flood fill algorithm

    // Lake expansion parameters
    public static final int LAKE_EXPANSION_THRESHOLD = 300; // Height below this can become lake during expansion
    public static final int MAX_LAKE_EXPANSIONS = 200;       // Maximum number of expansion iterations
    public static final int SEED_POINTS = 50;// Number of random seed points for lake expansion
    public static final int RIVER_AMOUNT = 50;


    private float seaLevel;
    private float heat;
    private float rainfall;
    private int riverAmount;
    private float elevationVariance;
    private float heatVariance;
    private float rainVariance;

    public float getSeaLevel() {
        return seaLevel;
    }

    public void setSeaLevel(float seaLevel) {
        this.seaLevel = seaLevel;
    }

    public float getHeat() {
        return heat;
    }

    public void setHeat(float heat) {
        this.heat = heat;
    }

    public float getRainfall() {
        return rainfall;
    }

    public void setRainfall(float rainfall) {
        this.rainfall = rainfall;
    }

    public int getRiverAmount() {
        return riverAmount;
    }

    public void setRiverAmount(int riverAmount) {
        this.riverAmount = riverAmount;
    }

    public float getElevationVariance() {
        return elevationVariance;
    }

    public void setElevationVariance(float elevationVariance) {
        this.elevationVariance = elevationVariance;
    }

    public float getHeatVariance() {
        return heatVariance;
    }

    public void setHeatVariance(float heatVariance) {
        this.heatVariance = heatVariance;
    }

    public float getRainVariance() {
        return rainVariance;
    }

    public void setRainVariance(float rainVariance) {
        this.rainVariance = rainVariance;
    }

    private int[][] heightMap;
    private int[][] tempMap;
    private int[][] biomeMap;
    private int[][] moistMap;

    public World() {
        heightMap = new int[WORLD_WIDTH][WORLD_LENGTH];
        tempMap = new int[WORLD_WIDTH][WORLD_LENGTH];
        moistMap = new int[WORLD_WIDTH][WORLD_LENGTH];
    }
    public void createWorld() {
        initMaps();
       makeLakes();
        biomeMap = setBiomes();
        RiverGenerator riverGenerator = new RiverGenerator(heightMap,biomeMap,WORLD_WIDTH, WORLD_LENGTH);
        riverGenerator.generateRivers(RIVER_AMOUNT);

    }

    public int[][] getBiomeMap() {
        return biomeMap;
    }

    private void initHeightMap(double spread, double heightVariance, int elevationConstant) {


        for (int i = 0; i < WORLD_LENGTH; i++) {
            for (int j = 0; j < WORLD_WIDTH; j++) {
                int height = (int) (PerlinNoise.noise(i * spread, j * spread) * heightVariance + elevationConstant);
                heightMap[i][j] = Math.max(0, Math.min(WORLD_HEIGHT, height));
            }
        }

    }

    private void initHeatMap(double spread, double heatAmplitude, double heatIncrease) {
        for (int i = 0; i < WORLD_LENGTH; i++) {
            for (int j = 0; j < WORLD_WIDTH; j++) {
                int heat = (int) (PerlinNoise.noise(i * spread + 200, j * spread + 200) * heatAmplitude + heatIncrease);
                tempMap[i][j] = Math.max(0, Math.min(MAX_HEAT, heat));
            }
        }

    }

    private void initMoistMap(double spread, double moistAmplitude, double moistIncrease) {
        for (int i = 0; i < WORLD_LENGTH; i++) {
            for (int j = 0; j < WORLD_WIDTH; j++) {
                int moisture = (int) (PerlinNoise.noise(i * spread + 100,100 + j * spread) * moistAmplitude + moistIncrease);
                moistMap[i][j] = Math.max(0, Math.min(MAX_HEAT, moisture));
            }
        }

    }

    public void initMaps() {
        initHeightMap(.001, DEFAULT_HEIGHT_VARIANCE, DEFAULT_HEIGHT_INCREASE);
        initHeatMap(.001, DEFAULT_TEMP_VARIANCE, DEFAULT_TEMP_INCREASE);
        initMoistMap(.001,DEFAULT_MOIST_VARIANCE,DEFAULT_MOIST_INCREASE);

    }


    public int[][] setBiomes() {
        int width = heightMap.length;
        int height = heightMap[0].length;
        int[][] biomeMap = new int[width][height];

        // Assign biomes based on the rules
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int elevation = heightMap[x][y];

                // Check extreme elevation conditions first
                if (elevation < OCEAN_THRESHOLD) {

                    biomeMap[x][y] = OCEAN;
                    heightMap[x][y] = OCEAN_THRESHOLD;
                    continue;
                }

                if (elevation > MOUNTAIN_THRESHOLD) {
                    biomeMap[x][y] = MOUNTAIN;
                    continue;
                }

                // For non-extreme elevations, determine biome based on temperature and moisture
                int temperature = tempMap[x][y];
                int moisture = moistMap[x][y];

                // Determine temperature category
                int tempCategory;
                if (temperature < LOW_TEMP_THRESHOLD) {
                    tempCategory = 0; // Low
                } else if (temperature > HIGH_TEMP_THRESHOLD) {
                    tempCategory = 2; // High
                } else {
                    tempCategory = 1; // Medium
                }

                // Determine moisture category
                int moistureCategory;
                if (moisture < LOW_MOISTURE_THRESHOLD) {
                    moistureCategory = 0; // Low
                } else if (moisture > HIGH_MOISTURE_THRESHOLD) {
                    moistureCategory = 2; // High
                } else {
                    moistureCategory = 1; // Medium
                }

                // Assign biome based on combination of temperature and moisture categories
                if (tempCategory == 2) { // High temperature
                    if (moistureCategory == 0) biomeMap[x][y] = DESERT;
                    else if (moistureCategory == 1) biomeMap[x][y] = SAVANNA;
                    else biomeMap[x][y] = RAINFOREST;
                } else if (tempCategory == 1) { // Medium temperature
                    if (moistureCategory == 0) biomeMap[x][y] = PLAINS;
                    else if (moistureCategory == 1) biomeMap[x][y] = GRASSLAND;
                    else biomeMap[x][y] = SEASONAL_FOREST;
                } else { // Low temperature
                    if (moistureCategory == 0) biomeMap[x][y] = TUNDRA;
                    else if (moistureCategory == 1) biomeMap[x][y] = TAIGA;
                    else biomeMap[x][y] = BOREAL_FOREST;
                }
            }
        }

        return biomeMap;
    }
    // Add these methods to your World class
    /**
     * Uses a flood fill algorithm to expand ocean areas and create lakes.
     * This method modifies the heightMap to convert eligible land to water.
     */
    public void makeLakes() {
        // Create a smaller subset of the world to process for performance
        int subsetWidth = WORLD_WIDTH/4; // Use a subset of the world for reasonable processing time
        int subsetLength = WORLD_LENGTH/4;

        // Initialize visited array for flood fill
        visited = new boolean[subsetWidth][subsetLength];

        // First, identify existing ocean cells as starting points for flood fill
        Queue<Point> queue = new LinkedList<>();

        // Add existing ocean cells as starting points
        for (int x = 0; x < subsetWidth; x++) {
            for (int y = 0; y < subsetLength; y++) {
                if (heightMap[x][y] < OCEAN_THRESHOLD) {
                    queue.add(new Point(x, y));
                    visited[x][y] = true;
                }
            }
        }

        // Add random seed points for inland lakes
        for (int i = 0; i < SEED_POINTS; i++) {
            int x = (int) (Math.random() * subsetWidth);
            int y = (int) (Math.random() * subsetLength);

            // Only create a lake seed if the area is not too high
            if (heightMap[x][y] < LAKE_EXPANSION_THRESHOLD && heightMap[x][y] >= OCEAN_THRESHOLD) {
                heightMap[x][y] = OCEAN_THRESHOLD - 1; // Make it an ocean cell
                queue.add(new Point(x, y));
                visited[x][y] = true;
            }
        }

        // Perform flood fill for several iterations to gradually expand lakes
        for (int iteration = 0; iteration < MAX_LAKE_EXPANSIONS; iteration++) {
            int size = queue.size();

            for (int i = 0; i < size; i++) {
                Point current = queue.poll();

                // Process each of the 4 adjacent cells
                processAdjacentCell(current.x + 1, current.y, queue, subsetWidth, subsetLength);
                processAdjacentCell(current.x - 1, current.y, queue, subsetWidth, subsetLength);
                processAdjacentCell(current.x, current.y + 1, queue, subsetWidth, subsetLength);
                processAdjacentCell(current.x, current.y - 1, queue, subsetWidth, subsetLength);
            }
        }

        // Clean up after flood fill
        visited = null;
    }

    /**
     * Helper method to process an adjacent cell during flood fill
     */
    private void processAdjacentCell(int x, int y, Queue<Point> queue, int width, int length) {
        // Check if the coordinates are valid and the cell hasn't been visited
        if (x >= 0 && x < width && y >= 0 && y < length && !visited[x][y]) {
            // Check if this cell can become a lake (not too high and not a mountain)
            if (heightMap[x][y] < LAKE_EXPANSION_THRESHOLD && heightMap[x][y] < MOUNTAIN_THRESHOLD) {
                // Probability of expansion decreases as height increases
                double probability = 1.0 - ((double) heightMap[x][y] / LAKE_EXPANSION_THRESHOLD);

                if (Math.random() < probability) {
                    // Convert to water
                    heightMap[x][y] = OCEAN_THRESHOLD - 1;
                    queue.add(new Point(x, y));
                }
            }

            // Mark as visited regardless of whether it became water
            visited[x][y] = true;
        }
    }
    public int[][] getHeightMap() {
        return heightMap;
    }
    private static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }


}
