package main.java.io.github.noahcraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class HomeScreen implements Screen {
    private final Main game;
    private Stage stage;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GlyphLayout glyphLayout; // For measuring text

    private final float WIDTH = 1280f;
    private final float HEIGHT = 720f;

    // UI Colors
    private final Color BACKGROUND_COLOR = new Color(0.1f, 0.1f, 0.2f, 1f);
    private final Color SLIDER_BG_COLOR = new Color(0.3f, 0.3f, 0.4f, 1f);
    private final Color SLIDER_FILL_COLOR = new Color(0.4f, 0.65f, 0.9f, 1f);
    private final Color SLIDER_KNOB_COLOR = new Color(0.9f, 0.9f, 0.9f, 1f);
    private final Color BUTTON_COLOR = new Color(0.25f, 0.6f, 0.3f, 1f);
    private final Color TEXT_COLOR = Color.WHITE;

    // Layout constants
    private final float TITLE_Y_BASELINE;
    private final float SLIDER_START_Y = HEIGHT - 180; // Adjusted for more space from title
    private final float SLIDER_SPACING = 50f;         // Adjusted spacing
    private final float SLIDER_WIDTH = 450f;          // Slightly reduced width
    private final float SLIDER_HEIGHT = 25f;          // Slightly reduced height

    private final float SLIDER_AREA_CENTER_X = WIDTH / 2;
    private final float LABEL_SLIDER_GAP = 20f; // Gap between label and slider
    private final float SLIDER_VALUE_GAP = 20f; // Gap between slider and value text

    // Calculate X positions based on SLIDER_AREA_CENTER_X
    // Labels will be to the left of the sliders, values to the right.
    // We'll calculate these dynamically based on text width for better alignment if needed,
    // but for now, let's use fixed offsets from the slider itself.
    private final float SLIDER_X; // Calculated in constructor based on SLIDER_WIDTH and SLIDER_AREA_CENTER_X
    private final float LABEL_X; // Labels will be right-aligned before the slider
    private final float VALUE_X;  // Values will be left-aligned after the slider


    private final float BUTTON_Y = 70; // Adjusted Y
    private final float BUTTON_WIDTH = 300; // Adjusted width
    private final float BUTTON_HEIGHT = 50; // Adjusted height

    // Slider state
    private int activeSlider = -1;
    private final Slider[] sliders = new Slider[7];

    public HomeScreen(final Main game) {
        this.game = game;
        stage = new Stage(new FitViewport(WIDTH, HEIGHT));
        Gdx.input.setInputProcessor(stage);

        font = new BitmapFont();
        shapeRenderer = new ShapeRenderer();
        glyphLayout = new GlyphLayout();

        TITLE_Y_BASELINE = HEIGHT - 70; // Adjusted for more space

        // Calculate slider X position to center it
        SLIDER_X = SLIDER_AREA_CENTER_X - SLIDER_WIDTH / 2;
        LABEL_X = SLIDER_X - 170f;
        VALUE_X = SLIDER_X + SLIDER_WIDTH + 20f;


        setupSliders();
        setupGenerateButton();
    }

    private void setupSliders() {
        sliders[0] = new Slider("Sea Level", 0f, 1f, 0.01f, game.getWorldConfig().getSeaLevel());
        sliders[1] = new Slider("Global Heat", 0f, 1f, 0.01f, game.getWorldConfig().getHeat());
        sliders[2] = new Slider("Global Rainfall", 0f, 1f, 0.01f, game.getWorldConfig().getRainfall());
        sliders[3] = new Slider("River Amount", 0f, 100f, 1f, game.getWorldConfig().getRiverAmount()); // Assuming int
        sliders[4] = new Slider("Elevation Variance", 0f, 1f, 0.01f, game.getWorldConfig().getElevationVariance());
        sliders[5] = new Slider("Heat Variance", 0f, 1f, 0.01f, game.getWorldConfig().getHeatVariance());
        sliders[6] = new Slider("Rain Variance", 0f, 1f, 0.01f, game.getWorldConfig().getRainVariance());

        for (int i = 0; i < sliders.length; i++) {
            sliders[i].x = SLIDER_X;
            sliders[i].y = SLIDER_START_Y - (i * SLIDER_SPACING);
            sliders[i].width = SLIDER_WIDTH;
            sliders[i].height = SLIDER_HEIGHT;
        }

        stage.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                for (int i = 0; i < sliders.length; i++) {
                    if (isInSlider(sliders[i], x, y)) {
                        activeSlider = i;
                        updateSliderValue(activeSlider, x);
                        return true;
                    }
                }
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                activeSlider = -1;
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (activeSlider >= 0) {
                    updateSliderValue(activeSlider, x);
                }
                super.touchDragged(event, x, y, pointer);
            }
        });
    }

    private void setupGenerateButton() {
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                float buttonRectX = WIDTH / 2 - BUTTON_WIDTH / 2;
                if (x >= buttonRectX && x <= buttonRectX + BUTTON_WIDTH &&
                    y >= BUTTON_Y && y <= BUTTON_Y + BUTTON_HEIGHT) {
                    game.startGame();
                }
            }
        });
    }

    private boolean isInSlider(Slider slider, float x, float y) {
        // Increased padding for easier touch interaction
        float padding = 15f;
        return x >= slider.x - padding && x <= slider.x + slider.width + padding &&
            y >= slider.y - padding && y <= slider.y + slider.height + padding;
    }

    private void updateSliderValue(int sliderIndex, float touchX) {
        if (sliderIndex < 0 || sliderIndex >= sliders.length) return;
        Slider slider = sliders[sliderIndex];
        float effectiveX = MathUtils.clamp(touchX, slider.x, slider.x + slider.width);
        float percentage = (effectiveX - slider.x) / slider.width;
        float newValue = slider.min + percentage * (slider.max - slider.min);
        if (slider.step > 0) {
            newValue = Math.round(newValue / slider.step) * slider.step;
        }
        slider.value = MathUtils.clamp(newValue, slider.min, slider.max);

        switch (sliderIndex) {
            case 0: game.getWorldConfig().setSeaLevel(slider.value); break;
            case 1: game.getWorldConfig().setHeat(slider.value); break;
            case 2: game.getWorldConfig().setRainfall(slider.value); break;
            case 3: game.getWorldConfig().setRiverAmount((int)slider.value); break;
            case 4: game.getWorldConfig().setElevationVariance(slider.value); break;
            case 5: game.getWorldConfig().setHeatVariance(slider.value); break;
            case 6: game.getWorldConfig().setRainVariance(slider.value); break;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(BACKGROUND_COLOR.r, BACKGROUND_COLOR.g, BACKGROUND_COLOR.b, BACKGROUND_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);

        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Slider slider : sliders) {
            shapeRenderer.setColor(SLIDER_BG_COLOR);
            shapeRenderer.rect(slider.x, slider.y, slider.width, slider.height);
            float fillPercentage = (slider.value - slider.min) / (slider.max - slider.min);
            float fillWidth = MathUtils.clamp(fillPercentage * slider.width, 0, slider.width);
            shapeRenderer.setColor(SLIDER_FILL_COLOR);
            shapeRenderer.rect(slider.x, slider.y, fillWidth, slider.height);
            shapeRenderer.setColor(SLIDER_KNOB_COLOR);
            float knobVisualWidth = 10f;
            float knobVisualHeightPadding = 5f; // How much taller the knob is than the track
            float knobX = MathUtils.clamp(slider.x + fillWidth - (knobVisualWidth / 2), slider.x, slider.x + slider.width - knobVisualWidth);
            shapeRenderer.rect(knobX, slider.y - knobVisualHeightPadding, knobVisualWidth, slider.height + (knobVisualHeightPadding * 2));
        }

        float buttonRectX = WIDTH / 2 - BUTTON_WIDTH / 2;
        shapeRenderer.setColor(BUTTON_COLOR);
        shapeRenderer.rect(buttonRectX, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        shapeRenderer.end();

        stage.getBatch().begin();
        font.setColor(TEXT_COLOR);

        // Title
        font.getData().setScale(1f); // Slightly larger title
        String title = "WORLD GENERATOR";
        glyphLayout.setText(font, title);
        font.draw(stage.getBatch(), glyphLayout, WIDTH / 2 - glyphLayout.width / 2, TITLE_Y_BASELINE);

        // Slider labels and values
        font.getData().setScale(1.1f); // Slightly smaller for more compact look

        // ...

        for (int i = 0; i < sliders.length; i++) {
            Slider slider = sliders[i];


            // --- Label ---
            glyphLayout.setText(font, slider.label);
            // Vertical center of the slider track
            float trackCenterY = slider.y + (slider.height / 2);
            // Baseline Y: center text vertically on the track
            float labelBaselineY = trackCenterY + (glyphLayout.height / 2);
            // Right-align label to LABEL_X
            font.draw(stage.getBatch(), glyphLayout, LABEL_X - glyphLayout.width,  470-43*i);

            // --- Value ---
            String valueText = slider.step >= 1f && (slider.step % 1 == 0) ?
                String.format("%d", (int)slider.value) :
                String.format("%.2f", slider.value);
            glyphLayout.setText(font, valueText);
            // Same baseline Y as label for vertical alignment
            float valueBaselineY = trackCenterY + (glyphLayout.height / 2);
            // Left-align value to VALUE_X
            font.draw(stage.getBatch(), glyphLayout, 550, 470-43*i);
        }

// ...


        // Generate button text (Centered in button)
        font.getData().setScale(1.2f); // Adjusted scale
        String buttonText = "GENERATE WORLD";
        glyphLayout.setText(font, buttonText);
        float buttonContentCenterX = buttonRectX + BUTTON_WIDTH / 2;
        float buttonContentCenterY = BUTTON_Y + BUTTON_HEIGHT / 2;
        float finalButtonTextX = buttonContentCenterX - glyphLayout.width / 2;
        float finalButtonTextY_Baseline = buttonContentCenterY + glyphLayout.height / 2; // Adjusted for baseline
        font.draw(stage.getBatch(), glyphLayout, 325, 85);

        stage.getBatch().end();
    }

    private static class Slider {
        String label;
        float x, y, width, height;
        float min, max, step, value;
        public Slider(String label, float min, float max, float step, float initialValue) {
            this.label = label; this.min = min; this.max = max; this.step = step; this.value = initialValue;
        }
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        if (stage != null) stage.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
    }
}
