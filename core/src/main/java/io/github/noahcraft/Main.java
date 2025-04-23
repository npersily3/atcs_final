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
            new Color(0.1f, 0.4f, 0.2f, 1.0f)       // BOREAL_FOREST - Dark Green
    };

    private static final String[] BIOME_NAMES = {
            "Ocean", "Mountain", "Desert", "Savanna", "Rainforest",
            "Plains", "Grassland", "Seasonal Forest", "Tundra", "Taiga", "Boreal Forest"
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
        pixmap = new Pixmap(DISPLAY_WIDTH, DISPLAY_HEIGHT, Pixmap.Format.RGBA8888);
        renderBiomeMap();

        // Create texture from pixmap
        mapTexture = new Texture(pixmap);

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

        for (int x = 0; x < DISPLAY_WIDTH; x++) {
            for (int y = 0; y < DISPLAY_HEIGHT; y++) {
                int worldX = (startX + x) % World.WORLD_WIDTH;
                int worldY = (startY + y) % World.WORLD_LENGTH;

                int biomeType = biomeMap[worldX][worldY];
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

    @Override
    public void render() {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Draw terrain texture
        batch.begin();
        batch.draw(mapTexture, 0, 0, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
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
