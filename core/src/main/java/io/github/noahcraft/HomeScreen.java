package main.java.io.github.noahcraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class HomeScreen implements Screen {
    private final Main game;
    private Stage stage;
    private Skin skin;

    public HomeScreen(final Main game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Create a programmatic skin for the UI
        createSkin();

        // Create a table to layout the UI elements
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Add a title
        Label titleLabel = new Label("World Generator Settings", skin, "title");
        table.add(titleLabel).colspan(2).pad(20f);
        table.row();

        // Sea Level slider
        addSlider(table, "Sea Level:", 0f, 1f, 0.01f, game.getWorldConfig().getSeaLevel(),
            value -> game.getWorldConfig().setSeaLevel(value));

        // Heat slider
        addSlider(table, "Global Heat:", 0f, 1f, 0.01f, game.getWorldConfig().getHeat(),
            value -> game.getWorldConfig().setHeat(value));

        // Rainfall slider
        addSlider(table, "Global Rainfall:", 0f, 1f, 0.01f, game.getWorldConfig().getRainfall(),
            value -> game.getWorldConfig().setRainfall(value));

        // River Amount slider
        addSlider(table, "River Amount:", 0, 100, 1, game.getWorldConfig().getRiverAmount(),
            value -> game.getWorldConfig().setRiverAmount((int)value));

        // Elevation Variance slider
        addSlider(table, "Elevation Variance:", 0f, 1f, 0.01f, game.getWorldConfig().getElevationVariance(),
            value -> game.getWorldConfig().setElevationVariance(value));

        // Heat Variance slider
        addSlider(table, "Heat Variance:", 0f, 1f, 0.01f, game.getWorldConfig().getHeatVariance(),
            value -> game.getWorldConfig().setHeatVariance(value));

        // Rain Variance slider
        addSlider(table, "Rain Variance:", 0f, 1f, 0.01f, game.getWorldConfig().getRainVariance(),
            value -> game.getWorldConfig().setRainVariance(value));

        // Add a separator
        table.add(new Label("", skin)).colspan(2).padTop(20f);
        table.row();

        // Generate World button
        TextButton generateButton = new TextButton("Generate World", skin);
        table.add(generateButton).colspan(2).pad(30f).width(250f).height(60f);

        // Add listener for Generate button
        generateButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.startGame();
            }
        });
    }

    /**
     * Helper method to create and add a slider with a label to the table
     */
    private void addSlider(Table table, String labelText, float min, float max, float step,
                           float initialValue, SliderCallback callback) {
        // Add the label
        Label label = new Label(labelText, skin);
        table.add(label).padRight(10f).width(150f).right();

        // Create a container for the slider and value label
        Table sliderContainer = new Table();

        // Create the slider
        final Slider slider = new Slider(min, max, step, false, skin);
        slider.setValue(initialValue);
        sliderContainer.add(slider).width(300f);

        // Create value label to show the current value
        final Label valueLabel = new Label(String.format("%.2f", initialValue), skin);
        sliderContainer.add(valueLabel).width(80f).padLeft(10f);

        // Add the container to the main table
        table.add(sliderContainer).padBottom(15f).left();
        table.row();

        // Add listener to update the config and value label when slider changes
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = slider.getValue();
                callback.setValue(value);

                // Update the value label (format depends on whether it's an integer or float)
                if (step >= 1f) {
                    valueLabel.setText(String.format("%d", (int)value));
                } else {
                    valueLabel.setText(String.format("%.2f", value));
                }
            }
        });
    }

    /**
     * Functional interface for slider value change callbacks
     */
    private interface SliderCallback {
        void setValue(float value);
    }

    /**
     * Creates a simple programmatic skin for the UI
     /**
     * Creates a simple programmatic skin for the UI
     */
    private void createSkin() {
        skin = new Skin();

        // Add a bitmap font to the skin
        skin.add("default", new BitmapFont());

        // Create a 1x1 white texture for our UI elements
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        // Add texture to skin
        skin.add("white", texture);

        // Create drawables for UI elements
        TextureRegion region = new TextureRegion(texture);
        TextureRegionDrawable drawable = new TextureRegionDrawable(region);

        // Button styles
        TextureRegionDrawable buttonUp = new TextureRegionDrawable(region);
        buttonUp.setMinWidth(30);
        buttonUp.setMinHeight(30);
        buttonUp.setLeftWidth(10);
        buttonUp.setRightWidth(10);
        buttonUp.setTopHeight(10);
        buttonUp.setBottomHeight(10);

        TextureRegionDrawable buttonDown = new TextureRegionDrawable(region);
        buttonDown.setMinWidth(30);
        buttonDown.setMinHeight(30);

        TextureRegionDrawable sliderBg = new TextureRegionDrawable(region);
        sliderBg.setMinWidth(10);
        sliderBg.setMinHeight(4);

        TextureRegionDrawable sliderKnob = new TextureRegionDrawable(region);
        sliderKnob.setMinWidth(20);
        sliderKnob.setMinHeight(20);

        // Store the drawables directly first
        skin.add("button-up", buttonUp); // Default to Drawable

        skin.add("button-down", buttonDown);
        skin.add("slider-bg", sliderBg);
        skin.add("slider-knob", sliderKnob);


        // Create button style
        TextButtonStyle textButtonStyle = new TextButtonStyle();
        textButtonStyle.up = skin.getDrawable("button-up");
        textButtonStyle.down = skin.getDrawable("button-down");
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle);

        // Create label style
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = skin.getFont("default");
        skin.add("default", labelStyle);

        // Create title style (exactly the same for now, could be different)
        skin.add("title", labelStyle);

        // Create slider style
        SliderStyle sliderStyle = new SliderStyle();
        sliderStyle.background = skin.getDrawable("slider-bg");
        sliderStyle.knob = skin.getDrawable("slider-knob");
        skin.add("default-horizontal", sliderStyle);
    }
    @Override
    public void show() {
        // Called when this screen becomes the current screen
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Clear the screen with a dark background
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update and draw the stage
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
