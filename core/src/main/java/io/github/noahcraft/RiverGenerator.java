package io.github.noahcraft;
import com.badlogic.gdx.math.Vector2;
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

    // River data structure
    private class River {
        List<Vector2> points = new ArrayList<>();
        float width;

        public River(float width) {
            this.width = width;
        }
    }

    public RiverGenerator(int[][] heightMap, int[][]biomeMap, int width, int length) {
        this.heightMap = heightMap;
        this.biomeMap = biomeMap;
        this.width = width;
        this.length = length;
        this.random = new Random();
    }

    public void generateRivers(int riverCount) {
        List<River> rivers = new ArrayList<>();

        for (int i = 0; i < riverCount; i++) {
            // Find a suitable starting point (higher elevation)
            Vector2 source = findRiverSource();

            // Find a suitable end point (lower elevation, ideally water)
            Vector2 destination = findRiverDestination(source);

            // Generate river path
            River river = generateRiverPath(source, destination);

            if (river != null && river.points.size() > 5) { // Ensure river is substantial
                rivers.add(river);

                // Apply erosion to height map along river
                applyRiverErosion(river,biomeMap,World.OCEAN);
            }
        }


    }

    private Vector2 findRiverSource() {
        // Find a suitable high elevation point for river source
        Vector2 source = new Vector2();
        float highestElevation = 0;

        for (int attempt = 0; attempt < 100; attempt++) {
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
        // Ideally water/ocean or lowest elevation available
        Vector2 destination = new Vector2();
        float lowestElevation = 1.0f;

        for (int attempt = 0; attempt < 100; attempt++) {
            int x = random.nextInt(width);
            int y = random.nextInt(length);

            // Don't choose starting point as destination
            if (x == source.x && y == source.y) {
                continue;
            }

            // Prefer points at map borders (simulating oceans)
            float borderBonus = 0;
            if (x < width * 0.1 || x > width * 0.9 || y < length * 0.1 || y > length * 0.9) {
                borderBonus = 0.1f;
            }

            if (heightMap[x][y] - borderBonus < lowestElevation) {
                lowestElevation = heightMap[x][y] - borderBonus;
                destination.set(x, y);
            }
        }

        return destination;
    }

    private River generateRiverPath(Vector2 source, Vector2 destination) {
        // A* pathfinding algorithm with terrain-aware cost function
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<String, Node> allNodes = new HashMap<>();

        Node startNode = new Node(source);
        startNode.gScore = 0;
        startNode.fScore = heuristic(source, destination);

        openSet.add(startNode);
        allNodes.put(nodeKey(startNode), startNode);

        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            // If found river
            if ((int)current.position.x == (int)destination.x &&
                (int)current.position.y == (int)destination.y) {
                // Reconstruct path
                River river = new River(1.0f + random.nextFloat() * 2.0f); // Random width
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

                // Calculate cost based on elevation change
                // Rivers prefer to flow downhill
                float elevationDiff = heightMap[nx][ny] - heightMap[(int)current.position.x][(int)current.position.y];
                float moveCost = 1.0f;

                if (elevationDiff > 0) {
                    // Penalty for flowing uphill
                    moveCost += elevationDiff * 10.0f;
                } else {
                    // Bonus for flowing downhill
                    moveCost -= elevationDiff * 0.5f;
                }

                // Diagonal movement costs more
                if (dir[0] != 0 && dir[1] != 0) {
                    moveCost *= 1.414f; // sqrt(2)
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

        // No path found
        return null;
    }

    private void applyRiverErosion(River river, int[][] biomeMap, int oceanBiomeId) {
        // Apply erosion to height map along river path and update biome map
        for (Vector2 point : river.points) {
            int x = (int)point.x;
            int y = (int)point.y;

            // Apply river depth
            int riverDepth = 1 + (int)(river.width / 2);
            heightMap[x][y] -= riverDepth;

            // Set the river tile to ocean biome
            biomeMap[x][y] = oceanBiomeId; // World.Ocean

            // Apply erosion to nearby points (creates river banks)
            int bankWidth = (int)Math.ceil(river.width);
            for (int dx = -bankWidth; dx <= bankWidth; dx++) {
                for (int dy = -bankWidth; dy <= bankWidth; dy++) {
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

    private float heuristic(Vector2 a, Vector2 b) {
        // Manhattan distance
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
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
