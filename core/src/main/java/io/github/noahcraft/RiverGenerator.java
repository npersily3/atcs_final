package io.github.noahcraft;
import com.badlogic.gdx.math.Vector2;

import java.awt.*;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RiverGenerator {
    private int[][] heightMap, biomeMap;
    private int width, length;
    private Random random;

    // Maximum iterations for pathfinding to prevent infinite loops
    private static final int MAX_ITERATIONS = Integer.MAX_VALUE;

    // Parameters to control river generation
    private static final float UPHILL_PENALTY = 10.0f;
    private static final float DOWNHILL_BONUS = 0.5f;
    private static final float DIAGONAL_COST = 1.414f;

    // River data structure
    private class River {
        List<Vector2> points = new ArrayList<>();
        int width;

        public River(int width) {
            this.width = width;
        }
    }

    public RiverGenerator(int[][] heightMap, int[][] biomeMap, int width, int length) {
        this.heightMap = heightMap;
        this.biomeMap = biomeMap;
        this.width = width;
        this.length = length;
        this.random = new Random();
    }

    public void generateRivers(int riverCount) {
        List<River> rivers = new ArrayList<>();
        int successfulRivers = 0;

        // Limit attempts to prevent excessive processing
        int maxAttempts = riverCount * 3;
        int attempts = 0;

        while (successfulRivers < riverCount && attempts < maxAttempts) {
            attempts++;

            // Find a suitable starting point (higher elevation)
            Vector2 source = findRiverSource();
       //     System.out.print(source.x + "   " + source.y + "  Destination  ");
            // Find a suitable end point (lower elevation, ideally water)
            Vector2 destination = findRiverDestination(source);
         //   System.out.println(destination.x + "   " + destination.y);
            // Generate river path with maximum iteration limit
            River river = generateRiverPath(source, destination);

            if (river != null && river.points.size() > 5) { // Ensure river is substantial
                rivers.add(river);
                successfulRivers++;

                // Apply erosion to height map along river
               // applyRiverErosion(river, biomeMap, World.RIVER);

                updateMap(river, World.RIVER);
                // Log progress for large maps
                if (width * length > 1000000 && successfulRivers % 10 == 0) {
                //    System.out.println("Generated " + successfulRivers + " rivers out of " + riverCount);
                }
            }
        }

     //   System.out.println("Successfully generated " + successfulRivers + " rivers out of " + riverCount + " requested");
    }

    private Vector2 findRiverSource() {
        // Find a suitable high elevation point for river source
        Vector2 source = new Vector2();
        float highestElevation = 0;

        // For very large maps, sample fewer points to improve performance
        int sampleCount = Math.min(100, width * length / 2500);

        for (int attempt = 0; attempt < sampleCount; attempt++) {
            int x = random.nextInt(width);
            int y = random.nextInt(length);

            // Prefer points away from edges
            if (x < width * 0.1 || x > width * 0.9 || y < length * 0.1 || y > length * 0.9) {
                continue;
            }

            if (heightMap[x][y] > highestElevation) {
                highestElevation = heightMap[x][y];
                source.set(x, y);
            }
        }

        return source;
    }

    private Vector2 findRiverDestination(Vector2 source) {
        // Find a suitable low elevation point for river destination
        Vector2 destination = new Vector2();
        float lowestElevation = Float.MAX_VALUE;

        // For very large maps, sample fewer points to improve performance
        int sampleCount = Math.min(100, width * length / 2500);

        for (int attempt = 0; attempt < sampleCount; attempt++) {
            int x = random.nextInt(width);
            int y = random.nextInt(length);

            // Don't choose starting point as destination
            if (x == (int)source.x && y == (int)source.y) {
                continue;
            }

            // Prefer points at map borders (simulating oceans)
            float borderBonus = 0;
            if (x < width * 0.1 || x > width * 0.9 || y < length * 0.1 || y > length * 0.9) {
                borderBonus = 0.1f;
            }

            // Make sure we're actually looking at the height value
            int elevation = heightMap[x][y];
            if (elevation - borderBonus < lowestElevation) {
                lowestElevation = elevation - borderBonus;
                destination.set(x, y);
            }
        }

        return destination;
    }

    private River generateRiverPath(Vector2 source, Vector2 destination) {
        // A* pathfinding algorithm with terrain-aware cost function
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<String, Node> allNodes = new HashMap<>();

        // For large maps, initialize with a larger capacity
        if (width * length > 1000000) {
            allNodes = new HashMap<>(10000);
        }

        Node startNode = new Node(source);
        startNode.gScore = 0;
        startNode.fScore = heuristic(source, destination);

        openSet.add(startNode);
        allNodes.put(nodeKey(startNode), startNode);

        // Directions: cardinal + diagonal
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};

        // Counter to prevent infinite loops
        int iterations = 0;

        while (!openSet.isEmpty() && iterations < MAX_ITERATIONS) {
            iterations++;
            Node current = openSet.poll();

            // If we found destination or are close enough
            if (isDestinationReached(current.position, destination)) {
                // Reconstruct path
                River river = new River(10); // Random width
                Node node = current;
                while (node != null) {
                    river.points.add(0, new Vector2(node.position));
                    node = node.cameFrom;
                }
                return river;
            }

            // Explore neighbors
            for (int[] dir : directions) {
                int nx = (int)current.position.x + dir[0];
                int ny = (int)current.position.y + dir[1];

                // Check bounds
                if (nx < 0 || nx >= width || ny < 0 || ny >= length) {
                    continue;
                }

                // Calculate cost based on elevation change - rivers prefer to flow downhill
                float currentElevation = heightMap[(int)current.position.x][(int)current.position.y];
                float neighborElevation = heightMap[nx][ny];
                float elevationDiff = neighborElevation - currentElevation;
                float moveCost = 1.0f;

                if (elevationDiff > 0) {
                    // Penalty for flowing uphill
                    moveCost += elevationDiff * UPHILL_PENALTY;
                } else {
                    // Bonus for flowing downhill
                    moveCost -= elevationDiff * DOWNHILL_BONUS;
                }

                // Diagonal movement costs more
                if (dir[0] != 0 && dir[1] != 0) {
                    moveCost *= DIAGONAL_COST;
                }

                // Large penalty for tiles that are already rivers to avoid overlapping
                if (biomeMap[nx][ny] == World.OCEAN) {
                    moveCost += 50.0f;
                }

                Vector2 neighborPos = new Vector2(nx, ny);
                String neighborKey = nodeKey(neighborPos);

                Node neighborNode = allNodes.getOrDefault(neighborKey, new Node(neighborPos));

                float tentativeGScore = current.gScore + moveCost;

                if (tentativeGScore < neighborNode.gScore) {
                    neighborNode.cameFrom = current;
                    neighborNode.gScore = tentativeGScore;
                    neighborNode.fScore = tentativeGScore + heuristic(neighborPos, destination);

                    allNodes.put(neighborKey, neighborNode);

                    if (!openSet.contains(neighborNode)) {
                        openSet.add(neighborNode);
                    }
                }
            }
        }

        // No path found or max iterations reached
        if (iterations >= MAX_ITERATIONS) {
            System.out.println("Warning: Max iterations reached while generating river path");
        }
        return null;
    }

    private boolean isDestinationReached(Vector2 current, Vector2 destination) {
        // Consider destination reached if exact match or very close
        int currentX = (int)current.x;
        int currentY = (int)current.y;
        int destX = (int)destination.x;
        int destY = (int)destination.y;

        // Check exact match
        if (currentX == destX && currentY == destY) {
            return true;
        }

        // For large maps, accept being close to destination or reaching any water
        if (width * length > 1000000) {
            // Accept water tiles
            if (biomeMap[currentX][currentY] == World.OCEAN) {
                return true;
            }

            // Accept being within reasonable distance
            int manhattanDistance = Math.abs(currentX - destX) + Math.abs(currentY - destY);
            if (manhattanDistance <= 100) {
                return true;
            }
        }

        return false;


    }
    private void updateMap(River river, int id) {
        List<Vector2> points = river.points;
        int width = river.width;

        // Process each point in the river
        for (Vector2 point : points) {
            int x = (int) point.x;
            int y = (int) point.y;

            // Apply the width by updating surrounding cells
            for (int i = -width/2; i <= width/2; i++) {
                for (int j = -width/2; j <= width/2; j++) {
                    int newX = x + i;
                    int newY = y + j;

                    // Check if the point is within bounds of the biomeMap
                    if (newX >= 0 && newX < biomeMap.length && newY >= 0 && newY < biomeMap[0].length) {
                        // Check if the point is within the radius (for circular/rounded rivers)
                        if (i*i + j*j <= (width/2)*(width/2)) {
                            biomeMap[newX][newY] = id;
                        }
                    }
                }
            }
        }
    }
/*

    private void applyRiverErosion(River river, int[][] biomeMap, int oceanBiomeId) {
        // Apply erosion to height map along river path and update biome map
        for (Vector2 point : river.points) {
            int x = (int)point.x;
            int y = (int)point.y;

            // Skip if out of bounds
            if (x < 0 || x >= width || y < 0 || y >= length) {
                continue;
            }

            // Apply river depth
            int riverDepth = 1 + (int)(river.width / 2);
            heightMap[x][y] -= riverDepth;

            // Set the river tile to ocean biome
            biomeMap[x][y] = oceanBiomeId;

            // Apply erosion to nearby points (creates river banks)
            int bankWidth = (int)Math.ceil(river.width);

            // For large rivers or maps, consider optimizing by only applying to certain points
            boolean sparseErosion = (width * length > 1000000) && (bankWidth > 3);

            for (int dx = -bankWidth; dx <= bankWidth; dx++) {
                for (int dy = -bankWidth; dy <= bankWidth; dy++) {
                    // When dealing with large maps, apply sparse erosion for performance
                    if (sparseErosion && (dx % 2 != 0 || dy % 2 != 0)) {
                        continue;
                    }

                    int nx = x + dx;
                    int ny = y + dy;

                    if (nx >= 0 && nx < width && ny >= 0 && ny < length) {
                        float distance = (float)Math.sqrt(dx*dx + dy*dy);
                        if (distance <= river.width) {
                            // Inside river - full depth and ocean biome
                            heightMap[nx][ny] -= riverDepth;
                            biomeMap[nx][ny] = oceanBiomeId;
                        } else if (distance <= river.width + 1) {
                            // Immediate bank - one block higher
                            heightMap[nx][ny] -= Math.max(1, riverDepth - 1);
                            // You could optionally set bank tiles to a different biome
                            // biomeMap[nx][ny] = riverBankBiomeId;
                        } else if (distance <= river.width + 2) {
                            // Outer bank - slight depression
                            heightMap[nx][ny] -= 1;
                        }
                    }
                }
            }
        }
    }
*/

    private float heuristic(Vector2 a, Vector2 b) {
        // Use Euclidean distance for better pathfinding
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        return (float)Math.sqrt(dx*dx + dy*dy);
    }

    private String nodeKey(Node node) {
        return nodeKey(node.position);
    }

    private String nodeKey(Vector2 position) {
        return (int)position.x + "," + (int)position.y;
    }

    // Helper class for A* pathfinding
    private class Node {
        Vector2 position;
        Node cameFrom;
        float gScore = Float.MAX_VALUE;
        float fScore = Float.MAX_VALUE;

        public Node(Vector2 position) {
            this.position = new Vector2(position);
        }
    }
}
