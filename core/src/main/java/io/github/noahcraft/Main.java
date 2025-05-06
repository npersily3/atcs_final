package main.java.io.github.noahcraft;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

public class Main extends ApplicationAdapter {

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private TerrainRenderer terrainRenderer;
    private TerrainCameraController cameraController;

    @Override
    public void create() {
        // Initialize components
        modelBatch = new ModelBatch();

        // Set up environment with strong lighting
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.6f, 1f));
        environment.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, -1f, -0.8f, -0.2f));

        // Configure camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(World.WORLD_WIDTH/2, 800f, World.WORLD_LENGTH/2 + 800f);
        camera.lookAt(World.WORLD_WIDTH/2, 0, World.WORLD_LENGTH/2);
        camera.near = 1f;
        camera.far = 10000f;
        camera.update();

        // Configure OpenGL
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);

        // Create camera controller
        cameraController = new TerrainCameraController(camera);
        Gdx.input.setInputProcessor(cameraController);

        // Create world
        World world = new World();
        world.createWorld();

        // Create terrain renderer with adaptive step size
        terrainRenderer = new TerrainRenderer(world, 20, 500);
    }

    @Override
    public void render() {
        // Update camera
        cameraController.update();

        // Clear screen with blue sky
        Gdx.gl.glClearColor(0.5f, 0.7f, 0.9f, 1.0f);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Render terrain
        modelBatch.begin(camera);
        terrainRenderer.render(modelBatch, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        terrainRenderer.dispose();
    }
}
