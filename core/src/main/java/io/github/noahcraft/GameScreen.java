package main.java.io.github.noahcraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

public class GameScreen implements Screen {
    private final Main game;

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private TerrainRenderer terrainRenderer;
    private TerrainCameraController cameraController;
    private World world;

    public GameScreen(final Main game) {
        this.game = game;

        // Initialize components
        modelBatch = new ModelBatch();

        // Set up environment with strong lighting
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.6f, 1f));
        environment.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, -1f, -0.8f, -0.2f));

        // Configure camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(World.WORLD_WIDTH/2, 2000f, World.WORLD_LENGTH/2 + 800f);
        camera.lookAt(World.WORLD_WIDTH/2, 0, World.WORLD_LENGTH/2);
        camera.near = 1f;
        camera.far = 100000f;
        camera.update();

        // Configure OpenGL
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);

        // Create camera controller
        cameraController = new TerrainCameraController(camera);
        Gdx.input.setInputProcessor(cameraController);

        // Create world with configuration from sliders
        // Get the WorldConfig from the main game
        WorldConfig config = game.getWorldConfig();
        // Alternative World creation without Builder pattern
        world = new World();
        world.setSeaLevel(config.getSeaLevel());
        world.setHeat(config.getHeat());
        world.setRainfall(config.getRainfall());
        world.setRiverAmount(config.getRiverAmount());
        world.setElevationVariance(config.getElevationVariance());
        world.setHeatVariance(config.getHeatVariance());
        world.setRainVariance(config.getRainVariance());
        world.createWorld();

        // Create terrain renderer with adaptive step size based on config
        terrainRenderer = new TerrainRenderer(world,
            20,
            500);
    }

    @Override
    public void show() {
        // Set the input processor to the camera controller when this screen becomes active
        Gdx.input.setInputProcessor(cameraController);
    }

    @Override
    public void render(float delta) {
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
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        modelBatch.dispose();
        terrainRenderer.dispose();
    }
}
