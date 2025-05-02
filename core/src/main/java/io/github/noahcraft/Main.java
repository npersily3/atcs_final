package io.github.noahcraft;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;

public class Main extends ApplicationAdapter {
    private World world;
    private int[][] biomeMap;
    private int[][] heightMap;
    private WorldRenderer3D worldRenderer;
    private CameraInputController cameraController;

    @Override
    public void create() {
        world = new World();
        world.createWorld();
        biomeMap = world.getBiomeMap();
        heightMap = world.getHeightMap(); // Make sure to add a getHeightMap() method to your World class

        worldRenderer = new WorldRenderer3D(biomeMap, heightMap);
        PerspectiveCamera camera = worldRenderer.getCamera();
        cameraController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(cameraController);
    }

    @Override
    public void render() {
        cameraController.update();
        worldRenderer.render();
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
    }
}
