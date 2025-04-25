package io.github.noahcraft;

import java.util.HashMap;
import java.util.Map;

public class BiomeManager {
    private final Map<Integer, BiomeProfile> biomes = new HashMap<>();

    public BiomeManager() {
        // Initialize with your biome profiles
        // Example (you can customize these values):
        biomes.put(World.DESERT, new BiomeProfile(World.DESERT, "Desert", 0xF4A460,
            0.8, 1.5, 0.3));
        biomes.put(World.SAVANNA, new BiomeProfile(World.SAVANNA, "Savanna", 0xD2B48C,
            1.0, 1.2, 0.7));
        // Add the rest of your biomes
    }

    public BiomeProfile getBiome(int id) {
        return biomes.get(id);
    }

    // Add this method for biome blending
    public int[][] blendBiomes(int[][] biomeMap, int[][] heightMap, int[][] tempMap, int[][] moistMap) {
        int width = biomeMap.length;
        int height = biomeMap[0].length;
        int[][] blendedBiomeMap = new int[width][height];

        // Radius for neighborhood sampling (larger = more blending)
        int radius = 3;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Original biome at this location
                int currentBiome = biomeMap[x][y];

                // Skip oceans and mountains
                if (currentBiome == World.OCEAN || currentBiome == World.MOUNTAIN) {
                    blendedBiomeMap[x][y] = currentBiome;
                    continue;
                }

                // Create a weighted list of neighboring biomes
                Map<Integer, Integer> neighborCounts = new HashMap<>();

                // Sample the neighborhood
                for (int nx = Math.max(0, x-radius); nx <= Math.min(width-1, x+radius); nx++) {
                    for (int ny = Math.max(0, y-radius); ny <= Math.min(height-1, y+radius); ny++) {
                        int neighbor = biomeMap[nx][ny];

                        // Skip oceans and mountains in blending
                        if (neighbor == World.OCEAN || neighbor == World.MOUNTAIN) {
                            continue;
                        }

                        // Weight by distance (closer = more influence)
                        int distance = Math.abs(nx-x) + Math.abs(ny-y);
                        int weight = radius + 1 - distance;

                        neighborCounts.put(neighbor,
                            neighborCounts.getOrDefault(neighbor, 0) + weight);
                    }
                }

                // Find the dominant biome
                int maxCount = 0;
                int dominantBiome = currentBiome;

                for (Map.Entry<Integer, Integer> entry : neighborCounts.entrySet()) {
                    if (entry.getValue() > maxCount) {
                        maxCount = entry.getValue();
                        dominantBiome = entry.getKey();
                    }
                }

                blendedBiomeMap[x][y] = dominantBiome;
            }
        }

        return blendedBiomeMap;
    }
}
