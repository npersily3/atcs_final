package io.github.noahcraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.VertexAttributes;

public class WorldRenderer3D {
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private Model worldModel;
    private ModelInstance worldInstance;
    private int[][] biomeMap;
    private int[][] heightMap;
    private static final float BLOCK_SIZE = 1f;
    private static final float VERTICAL_SCALE = 0.1f; // Adjust for exaggerated height

    // Biome colors - consistent with your 2D map
    private static final Color[] BIOME_COLORS = {
        new Color(0.0f, 0.3f, 0.8f, 1.0f),      // OCEAN
        new Color(0.5f, 0.5f, 0.5f, 1.0f),      // MOUNTAIN
        new Color(0.9f, 0.8f, 0.2f, 1.0f),      // DESERT
        new Color(0.8f, 0.7f, 0.2f, 1.0f),      // SAVANNA
        new Color(0.0f, 0.6f, 0.0f, 1.0f),      // RAINFOREST
        new Color(0.6f, 0.8f, 0.2f, 1.0f),      // PLAINS
        new Color(0.4f, 0.8f, 0.3f, 1.0f),      // GRASSLAND
        new Color(0.2f, 0.6f, 0.1f, 1.0f),      // SEASONAL_FOREST
        new Color(0.9f, 0.9f, 0.9f, 1.0f),      // TUNDRA
        new Color(0.7f, 0.8f, 0.7f, 1.0f),      // TAIGA
        new Color(0.1f, 0.4f, 0.2f, 1.0f),      // BOREAL_FOREST
        Color.CYAN                               // RIVER
    };

    public WorldRenderer3D(int[][] biomeMap, int[][] heightMap) {
        this.biomeMap = biomeMap;
        this.heightMap = heightMap;
        createEnvironment();
        createCamera();
        createWorldModel();
    }

    private void createEnvironment() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    }

    private void createCamera() {
        camera = new PerspectiveCamera(75, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(World.WORLD_WIDTH * BLOCK_SIZE / 2f, 100f, World.WORLD_LENGTH * BLOCK_SIZE / 2f);
        camera.lookAt(World.WORLD_WIDTH * BLOCK_SIZE / 2f, 0f, World.WORLD_LENGTH * BLOCK_SIZE / 2f);
        camera.near = 1f;
        camera.far = 1000f;
        camera.update();
    }

    private void createWorldModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        for (int x = 0; x < World.WORLD_WIDTH; x++) {
            for (int z = 0; z < World.WORLD_LENGTH; z++) {
                int biomeType = biomeMap[x][z];
                int height = heightMap[x][z];
                Color color = BIOME_COLORS[biomeType];

                // Create the top block
                modelBuilder.part("top_" + x + "_" + z, GL20.GL_TRIANGLES,
                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked,
                        new com.badlogic.gdx.graphics.g3d.utils.shape.Box(BLOCK_SIZE, BLOCK_SIZE * VERTICAL_SCALE, BLOCK_SIZE))
                    .setColor(color)
                    .transform.translate(x * BLOCK_SIZE, height * BLOCK_SIZE * VERTICAL_SCALE, z * BLOCK_SIZE);

                // Calculate the lowest neighbor's height
                int lowestNeighborHeight = height;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (Math.abs(dx) + Math.abs(dz) == 1) { // Only consider direct neighbors
                            int nx = x + dx;
                            int nz = z + dz;
                            if (nx >= 0 && nx < World.WORLD_WIDTH && nz >= 0 && nz < World.WORLD_LENGTH) {
                                lowestNeighborHeight = Math.min(lowestNeighborHeight, heightMap[nx][nz]);
                            }
                        }
                    }
                }

                // Create the vertical connection if there's a height difference
                float heightDifference = (height - lowestNeighborHeight) * BLOCK_SIZE * VERTICAL_SCALE;
                if (heightDifference > 0) {
                    modelBuilder.part("vertical_" + x + "_" + z, GL20.GL_TRIANGLES,
                            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked,
                            new com.badlogic.gdx.graphics.g3d.utils.shape.Box(BLOCK_SIZE * 0.8f, heightDifference, BLOCK_SIZE * 0.8f)) // Slightly thinner
                        .setColor(color)
                        .transform.translate(x * BLOCK_SIZE, (height + lowestNeighborHeight) * BLOCK_SIZE * VERTICAL_SCALE / 2f, z * BLOCK_SIZE);
                }
            }
        }

        worldModel = modelBuilder.end();
        worldInstance = new ModelInstance(worldModel);
        modelBatch = new ModelBatch();
    }

    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        modelBatch.render(worldInstance, environment);
        modelBatch.end();
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    public void dispose() {
        modelBatch.dispose();
        worldModel.dispose();
    }
}
