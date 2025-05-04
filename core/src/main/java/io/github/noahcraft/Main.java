package main.java.io.github.noahcraft;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public class Main extends ApplicationAdapter {
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private ModelCache modelCache;
    private Environment environment;
    private WorldManager worldManager;
    private World world;
    private CameraController cameraController;

    @Override
    public void create() {
        // Initialize camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(880f, 50f, 1000f); // Reset to above terrain
        camera.lookAt(880f, 0f, 1000f);
        camera.near = 1f;
        camera.far = 5000f;
        camera.update();

        // Initialize rendering with DefaultShaderProvider
        modelBatch = new ModelBatch(new DefaultShaderProvider());
        modelCache = new ModelCache();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        // Initialize world
        world = new World();
        world.createWorld();
        worldManager = new WorldManager(camera, world);

        // Set up camera controller
        cameraController = new CameraController(camera);
        Gdx.input.setInputProcessor(new GestureDetector(cameraController));
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE); // Disable back-face culling

        // Update chunks and camera
        worldManager.updateChunks();
        cameraController.update();
        camera.update();

        // Log camera position
        System.out.println("Camera at: " + camera.position);

        // Update model cache
        Array<ModelInstance> instances = worldManager.getVisibleInstances();
        modelCache.begin();
        for (ModelInstance instance : instances) {
            modelCache.add(instance);
        }
        modelCache.end();

        // Render
        System.out.println("Rendering frame, FPS: " + Gdx.graphics.getFramesPerSecond() + ", Instances: " + instances.size);
        modelBatch.begin(camera);
        modelBatch.render(modelCache, environment);
        Array<com.badlogic.gdx.graphics.g3d.Renderable> renderables = new Array<>();
        modelCache.getRenderables(renderables, null);
        System.out.println("Renderables: " + renderables.size);
        if (renderables.size == 0) {
            modelBatch.render(instances, environment);
        }
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        modelCache.dispose();
        worldManager.dispose();
    }
}

class WorldManager {
    private static final int CHUNK_SIZE = 8;
    private static final int RENDER_DISTANCE = 4;
    private HashMap<Vector3, Chunk> chunks = new HashMap<>();
    private PerspectiveCamera camera;
    private World world;

    public WorldManager(PerspectiveCamera camera, World world) {
        this.camera = camera;
        this.world = world;
    }

    public void updateChunks() {
        Vector3 camPos = camera.position;
        int chunkX = Math.round(camPos.x / CHUNK_SIZE);
        int chunkZ = Math.round(camPos.z / CHUNK_SIZE);

        for (int x = chunkX - RENDER_DISTANCE; x <= chunkX + RENDER_DISTANCE; x++) {
            for (int z = chunkZ - RENDER_DISTANCE; z <= chunkZ + RENDER_DISTANCE; z++) {
                Vector3 chunkPos = new Vector3(x, 0, z);
                if (!chunks.containsKey(chunkPos)) {
                    if (x * CHUNK_SIZE >= 0 && x * CHUNK_SIZE < World.WORLD_WIDTH &&
                        z * CHUNK_SIZE >= 0 && z * CHUNK_SIZE < World.WORLD_LENGTH) {
                        chunks.put(chunkPos, new Chunk(x, z, world.getHeightMap(), world.getBiomeMap(),
                            World.WORLD_WIDTH, World.WORLD_LENGTH));
                    }
                }
            }
        }

        chunks.entrySet().removeIf(entry -> camPos.dst(entry.getKey().scl(CHUNK_SIZE)) > RENDER_DISTANCE * CHUNK_SIZE * 1.5f);
        System.out.println("Loaded " + chunks.size() + " chunks");
    }

    public Array<ModelInstance> getVisibleInstances() {
        Array<ModelInstance> instances = new Array<>();
        for (Chunk chunk : chunks.values()) {
            Vector3 pos = chunk.getPosition();
            instances.add(chunk.getModelInstance());
            System.out.println("Added chunk at: " + pos);
        }
        System.out.println("Visible instances: " + instances.size);
        return instances;
    }

    public void dispose() {
        for (Chunk chunk : chunks.values()) {
            chunk.dispose();
        }
        chunks.clear();
    }
}

class Chunk {
    private static final int CHUNK_SIZE = 8;
    private static final int MAX_VERTICES = 30000;
    private ModelInstance modelInstance;
    private Vector3 position;
    private int[][] heightMap;
    private int[][] biomeMap;
    private int worldWidth;
    private int worldLength;

    public Chunk(int chunkX, int chunkZ, int[][] heightMap, int[][] biomeMap, int worldWidth, int worldLength) {
        this.heightMap = heightMap;
        this.biomeMap = biomeMap;
        this.worldWidth = worldWidth;
        this.worldLength = worldLength;
        this.position = new Vector3(chunkX * CHUNK_SIZE, 0, chunkZ * CHUNK_SIZE);
        generateMesh(chunkX, chunkZ);
    }

    private void generateMesh(int chunkX, int chunkZ) {
        long startTime = System.nanoTime();
        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        Map<Integer, MeshPartBuilder> builders = new HashMap<>();

        // one mesh part per biome for this chunk
        for (int biome : World.ALL_BIOMES) {
            Material mat = new Material(
                ColorAttribute.createDiffuse(getBiomeColor(biome)),
                ColorAttribute.createSpecular(1,1,1,1)
            );
            builders.put(biome,
                mb.part("b" + biome, GL20.GL_TRIANGLES,
                    VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, mat));
        }

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int wx = chunkX*CHUNK_SIZE + x, wz = chunkZ*CHUNK_SIZE + z;
                if (wx >= worldWidth || wz >= worldLength) continue;
                int h = Math.max(1, heightMap[wx][wz]);
                int nh = minNeighborHeight(wx, wz);
                int biome = biomeMap[wx][wz];
                MeshPartBuilder mesh = builders.get(biome);

                // build every cube column from nh to h inclusive
                for (int y = nh; y <= h; y++) {
                    addCube(mesh, x, y, z, heightMap, wx, wz);
                }
            }
        }

        Model model = mb.end();
        modelInstance = new ModelInstance(model);
        modelInstance.transform.setToTranslation(position);
        System.out.println("Chunk generated in "
            + (System.nanoTime()-startTime)/1_000_000 + " ms");
    }


    private void addNewMeshBuilder(ModelBuilder modelBuilder, Array<MeshPartBuilder> meshBuilders,
                                   Array<MeshPartBuilder> biomeBuilders, int biome) {
        Material material = new Material(
            ColorAttribute.createDiffuse(getBiomeColor(biome)),
            ColorAttribute.createSpecular(1f, 1f, 1f, 1f) // Add specular for shader compatibility
        );
        MeshPartBuilder meshBuilder = modelBuilder.part("cube_" + biome + "_" + meshBuilders.size, GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, material);
        meshBuilders.add(meshBuilder);
        biomeBuilders.add(meshBuilder);
    }

    private int addCube(MeshPartBuilder meshBuilder, float x, float y, float z, boolean[] visibleFaces) {
        float size = 1f;
        Vector3[] vertices = {
            new Vector3(x, y, z),
            new Vector3(x + size, y, z),
            new Vector3(x, y + size, z),
            new Vector3(x + size, y + size, z),
            new Vector3(x, y, z + size),
            new Vector3(x + size, y, z + size),
            new Vector3(x, y + size, z + size),
            new Vector3(x + size, y + size, z + size)
        };

        Vector3[] normals = {
            new Vector3(0, 0, -1),  // Front
            new Vector3(0, 0, 1),   // Back
            new Vector3(-1, 0, 0),  // Left
            new Vector3(1, 0, 0),   // Right
            new Vector3(0, 1, 0),   // Top
            new Vector3(0, -1, 0)   // Bottom
        };

        short[] indices = {
            0, 1, 2, 1, 3, 2, // Front
            5, 4, 7, 4, 6, 7, // Back
            4, 0, 6, 0, 2, 6, // Left
            1, 5, 3, 5, 7, 3, // Right
            2, 3, 6, 3, 7, 6, // Top
            4, 5, 0, 5, 1, 0  // Bottom
        };

        // Add vertices with averaged normals for shared vertices
        short[] vertexIndices = new short[8];
        for (int i = 0; i < vertices.length; i++) {
            // Use top normal as default for simplicity
            vertexIndices[i] = meshBuilder.vertex(vertices[i], normals[4], null, null);
        }

        // Add indices for visible faces
        int indicesAdded = 0;
        for (int i = 0; i < 6; i++) {
            if (visibleFaces[i]) {
                int idx = i * 6;
                System.out.println("Face " + i + " indices: " +
                    indices[idx] + "," + indices[idx + 1] + "," + indices[idx + 2] + "," +
                    indices[idx + 3] + "," + indices[idx + 4] + "," + indices[idx + 5]);
                meshBuilder.setVertexTransform(null);
                meshBuilder.index(
                    vertexIndices[indices[idx]],
                    vertexIndices[indices[idx + 1]],
                    vertexIndices[indices[idx + 2]]
                );
                meshBuilder.index(
                    vertexIndices[indices[idx + 3]],
                    vertexIndices[indices[idx + 4]],
                    vertexIndices[idx + 5]
                );
                indicesAdded += 6;
            }
        }

        return indicesAdded;
    }

    private Color getBiomeColor(int biome) {
        switch (biome) {
            case World.OCEAN: return Color.BLUE;
            case World.MOUNTAIN: return Color.GRAY;
            case World.DESERT: return Color.YELLOW;
            case World.SAVANNA: return new Color(0.8f, 0.6f, 0.2f, 1f);
            case World.RAINFOREST: return Color.GREEN;
            case World.PLAINS: return new Color(0.7f, 0.7f, 0.3f, 1f);
            case World.GRASSLAND: return new Color(0.6f, 0.8f, 0.3f, 1f);
            case World.SEASONAL_FOREST: return new Color(0.4f, 0.6f, 0.2f, 1f);
            case World.TUNDRA: return Color.WHITE;
            case World.TAIGA: return new Color(0.3f, 0.5f, 0.3f, 1f);
            case World.BOREAL_FOREST: return new Color(0.2f, 0.4f, 0.2f, 1f);
            case World.RIVER: return Color.CYAN;
            default: return Color.MAGENTA;
        }
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    public Vector3 getPosition() {
        return position;
    }

    public void dispose() {
        modelInstance.model.dispose();
    }
}

class CameraController implements GestureDetector.GestureListener {
    private PerspectiveCamera camera;
    private Vector3 tmp = new Vector3();

    public CameraController(PerspectiveCamera camera) {
        this.camera = camera;
    }

    public void update() {
        float speed = 100f * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)) {
            tmp.set(camera.direction).scl(speed);
            camera.position.add(tmp);
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S)) {
            tmp.set(camera.direction).scl(-speed);
            camera.position.add(tmp);
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(-speed);
            camera.position.add(tmp);
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(speed);
            camera.position.add(tmp);
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        float sensitivity = 0.1f;
        camera.rotate(Vector3.Y, -deltaX * sensitivity);
        tmp.set(camera.direction).nor();
        camera.update();
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {
    }
}
