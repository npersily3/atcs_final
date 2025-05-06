package main.java.io.github.noahcraft;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

public class Main extends Game {

    // Configuration parameters that will be set by sliders
    private WorldConfig worldConfig;

    @Override
    public void create() {
        // Initialize default configuration
        worldConfig = new WorldConfig();

        // Start with the home screen
        setScreen(new HomeScreen(this));
    }

    public WorldConfig getWorldConfig() {
        return worldConfig;
    }

    public void startGame() {
        setScreen(new GameScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (screen != null) screen.dispose();
    }
}
