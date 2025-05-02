package io.github.noahcraft;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.Array;

public class Main extends ApplicationAdapter {
    private static final int VIEWPORT_WIDTH = 800;
    private static final int VIEWPORT_HEIGHT = 600;

    // Visible portion of the world
    private static final int DISPLAY_WIDTH = 512;
    private static final int DISPLAY_HEIGHT = 512;
    private float cameraSpeed = 5f; // Adjust for panning speed
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Texture mapTexture;
    private Pixmap pixmap;
    private World world;
    private int[][] biomeMap;

    // For the biome color table
    private Stage stage;
    private Table biomeTable;
    private BitmapFont font;

    // Biome colors - using a clear color scheme
    private static final Color[] BIOME_COLORS = {
            new Color(0.0f, 0.3f, 0.8f, 1.0f),      // OCEAN - Deep Blue
            new Color(0.5f, 0.5f, 0.5f, 1.0f),      // MOUNTAIN - Gray
            new Color(0.9f, 0.8f, 0.2f, 1.0f),      // DESERT - Sandy Yellow
            new Color(0.8f, 0.7f, 0.2f, 1.0f),      // SAVANNA - Yellowish Green
            new Color(0.0f, 0.6f, 0.0f, 1.0f),      // RAINFOREST - Deep Green
            new Color(0.6f, 0.8f, 0.2f, 1.0f),      // PLAINS - Light Green
            new Color(0.4f, 0.8f, 0.3f, 1.0f),      // GRASSLAND - Medium Green
            new Color(0.2f, 0.6f, 0.1f, 1.0f),      // SEASONAL_FOREST - Forest Green
            new Color(0.9f, 0.9f, 0.9f, 1.0f),      // TUNDRA - Snow White
            new Color(0.7f, 0.8f, 0.7f, 1.0f),      // TAIGA - Light Gray Green
            new Color(0.1f, 0.4f, 0.2f, 1.0f),      // BOREAL_FOREST - Dark Green
            new Color(Color.CYAN)
    };

    private static final String[] BIOME_NAMES = {
            "Ocean", "Mountain", "Desert", "Savanna", "Rainforest",
            "Plains", "Grassland", "Seasonal Forest", "Tundra", "Taiga", "Boreal Forest", "River"
    };

    @Override
    public void create() {
        // Initialize camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);

        // Initialize batch
        batch = new SpriteBatch();

        // Create and initialize the world
        world = new World();

        world.createWorld();

        // Get the biome map
        biomeMap = world.getBiomeMap();

        // Create pixmap and render the biome map
        // Create pixmap with the size of the entire world
        pixmap = new Pixmap(World.WORLD_WIDTH, World.WORLD_LENGTH, Pixmap.Format.RGBA8888);
        renderBiomeMap();

        // Create texture from the full-sized pixmap
        mapTexture = new Texture(pixmap);

        // Initialize camera to view a portion of the world
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        camera.position.set(World.WORLD_WIDTH / 2f, World.WORLD_LENGTH / 2f, 0); // Center the camera
        camera.update();

        // Initialize stage for UI
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Create biome color table
        createBiomeTable();
    }

    private void renderBiomeMap() {
        // Select a portion of the world to display
        int startX = 0;
        int startY = 0;

        for (int x = 0; x < World.WORLD_WIDTH; x++) {
            for (int y = 0; y < World.WORLD_LENGTH; y++) {


                int biomeType = biomeMap[x][y];
                Color color = BIOME_COLORS[biomeType];

                pixmap.setColor(color);
                pixmap.drawPixel(x, y);
            }
        }
    }

    private void createBiomeTable() {
        font = new BitmapFont();
        font.getData().setScale(1.2f);

        LabelStyle headerStyle = new LabelStyle(font, Color.WHITE);
        LabelStyle labelStyle = new LabelStyle(font, Color.WHITE);

        biomeTable = new Table();
        biomeTable.top().right();
        biomeTable.setFillParent(true);
        biomeTable.pad(10);

        // Add header
        biomeTable.add(new Label("Biome Color Table", headerStyle)).colspan(2).pad(5).row();

        // Add biome colors and names
        for (int i = 0; i < BIOME_COLORS.length; i++) {
            // Create color swatch
            Pixmap swatchPixmap = new Pixmap(20, 20, Pixmap.Format.RGBA8888);
            swatchPixmap.setColor(BIOME_COLORS[i]);
            swatchPixmap.fill();
            Texture swatchTexture = new Texture(swatchPixmap);

            // Add color swatch and biome name
            biomeTable.add(new com.badlogic.gdx.scenes.scene2d.ui.Image(swatchTexture)).pad(2);
            biomeTable.add(new Label(BIOME_NAMES[i], labelStyle)).pad(2).left().row();

            swatchPixmap.dispose();
        }

        stage.addActor(biomeTable);
    }
    private void handleInput() {
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
            camera.translate(-cameraSpeed, 0);
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
            camera.translate(cameraSpeed, 0);
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.UP)) {
            camera.translate(0, cameraSpeed);
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
            camera.translate(0, -cameraSpeed);
        }

        // Mouse panning (drag with left button)
        if (Gdx.input.isButtonPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            camera.translate(-Gdx.input.getDeltaX(), Gdx.input.getDeltaY());
        }

        // Keep camera within world bounds (optional but recommended)
        float halfViewportWidth = VIEWPORT_WIDTH / 2f;
        float halfViewportHeight = VIEWPORT_HEIGHT / 2f;
        camera.position.x = Math.max(halfViewportWidth, Math.min(camera.position.x, World.WORLD_WIDTH - halfViewportWidth));
        camera.position.y = Math.max(halfViewportHeight, Math.min(camera.position.y, World.WORLD_LENGTH - halfViewportHeight));
    }
    @Override
    public void render() {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera based on input (see next step)
        handleInput();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Draw the entire world texture
        batch.begin();
        batch.draw(mapTexture, 0, 0); // Draw at (0,0) with its full size
        batch.end();

        // Draw UI
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        mapTexture.dispose();
        pixmap.dispose();
        stage.dispose();
        font.dispose();
    }
}
