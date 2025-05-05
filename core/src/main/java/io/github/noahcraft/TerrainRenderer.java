package main.java.io.github.noahcraft;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Vector3;

import java.util.HashMap;
import java.util.Map;

import static main.java.io.github.noahcraft.World.*;

public class TerrainRenderer {
    private final ModelInstance instance;

    public TerrainRenderer(World world, int tileStep, int chunkTiles) {
        // Get world data
        int[][] hm = world.getHeightMap();
        int[][] bm = world.getBiomeMap();

        // Calculate optimal step size based on world size to avoid vertex limits
        // Increase step size if WORLD_WIDTH * WORLD_LENGTH is too large
        int adaptiveStep = Math.max(tileStep, determineOptimalStepSize(WORLD_WIDTH, WORLD_LENGTH));
        System.out.println("Using adaptive step size: " + adaptiveStep);

        // Group biomes into major categories to reduce number of mesh parts
        int[] biomeGroups = {
            OCEAN,          // 0: Water bodies
            RIVER,          // 0: Water bodies (same group as OCEAN)
            MOUNTAIN,       // 1: Mountains
            DESERT,         // 2: Arid
            SAVANNA,        // 2: Arid (same as DESERT)
            RAINFOREST,     // 3: Forests
            SEASONAL_FOREST,// 3: Forests (same as RAINFOREST)
            BOREAL_FOREST,  // 3: Forests (same as RAINFOREST)
            TAIGA,          // 3: Forests (same as RAINFOREST)
            PLAINS,         // 4: Grasslands
            GRASSLAND,      // 4: Grasslands (same as PLAINS)
            TUNDRA          // 5: Snow
        };

        // Create model builder
        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        // Create a smaller number of mesh parts (one per biome group)
        int NUM_BIOME_GROUPS = 6; // Water, Mountains, Arid, Forests, Grasslands, Snow
        MeshPartBuilder[] builders = new MeshPartBuilder[NUM_BIOME_GROUPS];

        // Create mesh parts for biome groups
        for (int i = 0; i < NUM_BIOME_GROUPS; i++) {
            Color color = getBiomeGroupColor(i);
            Material mat = new Material(
                ColorAttribute.createDiffuse(color),
                ColorAttribute.createSpecular(0.5f, 0.5f, 0.5f, 1.0f)
            );

            builders[i] = mb.part(
                "biome_group_" + i,
                GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                mat
            );
            System.out.println("Created mesh part for biome group " + i);
        }

        // Track statistics
        int[] quadCounts = new int[NUM_BIOME_GROUPS];

        // Generate terrain using adaptive step size
        for (int x = 0; x < WORLD_WIDTH - adaptiveStep; x += adaptiveStep) {
            for (int z = 0; z < WORLD_LENGTH - adaptiveStep; z += adaptiveStep) {
                // Get heights
                float y00 = hm[x][z];
                float y10 = hm[x + adaptiveStep][z];
                float y11 = hm[x + adaptiveStep][z + adaptiveStep];
                float y01 = hm[x][z + adaptiveStep];

                // Get biome and determine its group
                int biome = bm[x][z];
                int biomeGroup = getBiomeGroup(biome, biomeGroups);

                // Get builder for this biome group
                MeshPartBuilder builder = builders[biomeGroup];

                // Count quad
                quadCounts[biomeGroup]++;

                // Calculate normal
                Vector3 v1 = new Vector3(adaptiveStep, y10 - y00, 0);
                Vector3 v2 = new Vector3(0, y01 - y00, adaptiveStep);
                Vector3 normal = new Vector3(v2).crs(v1).nor();

                // Add two triangles (a quad) with correct normals
                // First triangle
                float nx = normal.x, ny = normal.y, nz = normal.z;
                builder.triangle(
                    new MeshPartBuilder.VertexInfo().setPos(x, y00, z).setNor(nx, ny, nz),
                    new MeshPartBuilder.VertexInfo().setPos(x, y01, z + adaptiveStep).setNor(nx, ny, nz),
                    new MeshPartBuilder.VertexInfo().setPos(x + adaptiveStep, y11, z + adaptiveStep).setNor(nx, ny, nz)
                );

                // Second triangle
                builder.triangle(
                    new MeshPartBuilder.VertexInfo().setPos(x, y00, z).setNor(nx, ny, nz),
                    new MeshPartBuilder.VertexInfo().setPos(x + adaptiveStep, y11, z + adaptiveStep).setNor(nx, ny, nz),
                    new MeshPartBuilder.VertexInfo().setPos(x + adaptiveStep, y10, z).setNor(nx, ny, nz)
                );
            }
        }

        // Create model and instance
        Model model = mb.end();
        instance = new ModelInstance(model);

        // Print statistics
        System.out.println("Quad count per biome group:");
        for (int i = 0; i < NUM_BIOME_GROUPS; i++) {
            System.out.println("Biome group " + i + ": " + quadCounts[i] + " quads");
        }
    }

    private int determineOptimalStepSize(int worldWidth, int worldLength) {
        // Calculate total potential vertices
        int totalVertices = (worldWidth / 40) * (worldLength / 40) * 4; // 4 vertices per quad

        // LibGDX has a limit of about 32K indices per mesh
        // Aim for less than 8K vertices per mesh to be safe (32K indices)
        int desiredVertices = 8000;

        // Calculate step size to reach desired vertices
        int optimalStep = Math.max(40, (int)Math.sqrt((worldWidth * worldLength) / desiredVertices));

        // Round to nearest multiple of 10 for cleaner numbers
        return ((optimalStep + 9) / 10) * 10;
    }

    private int getBiomeGroup(int biome, int[] biomeGroups) {
        // Map biome to its group
        if (biome == OCEAN || biome == RIVER) return 0; // Water
        if (biome == MOUNTAIN) return 1; // Mountains
        if (biome == DESERT || biome == SAVANNA) return 2; // Arid
        if (biome == RAINFOREST || biome == SEASONAL_FOREST ||
            biome == BOREAL_FOREST || biome == TAIGA) return 3; // Forests
        if (biome == PLAINS || biome == GRASSLAND) return 4; // Grasslands
        if (biome == TUNDRA) return 5; // Snow
        return 0; // Default to water if unknown
    }

    private Color getBiomeGroupColor(int biomeGroup) {
        switch (biomeGroup) {
            case 0: return new Color(0.0f, 0.4f, 0.8f, 1.0f); // Water - blue
            case 1: return new Color(0.5f, 0.5f, 0.5f, 1.0f); // Mountains - gray
            case 2: return new Color(0.9f, 0.8f, 0.4f, 1.0f); // Arid - tan/yellow
            case 3: return new Color(0.1f, 0.6f, 0.2f, 1.0f); // Forests - green
            case 4: return new Color(0.4f, 0.8f, 0.3f, 1.0f); // Grasslands - light green
            case 5: return new Color(0.9f, 0.9f, 0.9f, 1.0f); // Snow - white
            default: return new Color(1.0f, 0.0f, 1.0f, 1.0f); // Unknown - magenta
        }
    }

    public void render(com.badlogic.gdx.graphics.g3d.ModelBatch batch,
                       com.badlogic.gdx.graphics.g3d.Environment env) {
        batch.render(instance, env);
    }

    public void dispose() {
        instance.model.dispose();
    }
}
