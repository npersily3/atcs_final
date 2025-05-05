package main.java.io.github.noahcraft;


import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;

public class TerrainCameraController extends InputAdapter {
    private final PerspectiveCamera camera;

    public TerrainCameraController(PerspectiveCamera camera) {
        this.camera = camera;
    }

    public void update() {
        float speed = 10f;
        if (Input.Keys.W == getKeyPressed()) camera.translate(camera.direction.cpy().scl(speed));
        if (Input.Keys.S == getKeyPressed()) camera.translate(camera.direction.cpy().scl(-speed));
        if (Input.Keys.A == getKeyPressed()) camera.translate(camera.direction.cpy().crs(camera.up).nor().scl(-speed));
        if (Input.Keys.D == getKeyPressed()) camera.translate(camera.direction.cpy().crs(camera.up).nor().scl(speed));
        if (Input.Keys.SPACE == getKeyPressed()) camera.translate(camera.up.cpy().scl(speed));
        if (Input.Keys.SHIFT_LEFT == getKeyPressed()) camera.translate(camera.up.cpy().scl(-speed));

        camera.update();
    }

    private int getKeyPressed() {
        for (int key : new int[]{Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D, Input.Keys.SPACE, Input.Keys.SHIFT_LEFT}) {
            if (com.badlogic.gdx.Gdx.input.isKeyPressed(key)) return key;
        }
        return -1;
    }
}
