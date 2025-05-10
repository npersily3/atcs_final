package main.java.io.github.noahcraft;


import com.badlogic.gdx.Game;

public class Main extends Game {

    // Configuration parameters that will be set by sliders
    private WorldConfig worldConfig;



    @Override
    public void create() {
        // Initialize default configuration with reasonable defaults
        worldConfig = new WorldConfig();
        worldConfig.setSeaLevel(0.5f);
        worldConfig.setHeat(0.5f);
        worldConfig.setRainfall(0.5f);
        worldConfig.setRiverAmount(50);
        worldConfig.setElevationVariance(0.5f);
        worldConfig.setHeatVariance(0.5f);
        worldConfig.setRainVariance(0.5f);

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
