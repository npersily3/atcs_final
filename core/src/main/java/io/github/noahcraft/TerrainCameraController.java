package main.java.io.github.noahcraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

public class TerrainCameraController extends InputAdapter {
    private final PerspectiveCamera camera;
    private float lastX;
    private float lastY;
    private boolean dragging;
    private final float rotationSpeed = 0.2f;

    public TerrainCameraController(PerspectiveCamera camera) {
        this.camera = camera;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            lastX = screenX;
            lastY = screenY;
            dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            dragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (dragging) {
            // Calculate how much the mouse has moved
            float deltaX = (screenX - lastX) * rotationSpeed;
            float deltaY = (screenY - lastY) * rotationSpeed;

            // Rotate around Y axis (left/right)
            camera.rotate(Vector3.Y, deltaX);

            // Rotate around X axis (up/down) - using the camera's right vector
            Vector3 rightAxis = new Vector3();
            camera.direction.cpy().crs(camera.up).nor().scl(1, 0, 1).nor();
            camera.rotate(rightAxis, -deltaY);

            lastX = screenX;
            lastY = screenY;
            return true;
        }
        return false;
    }

    public void update() {
        float speed = 10f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.translate(camera.direction.cpy().scl(speed));
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.translate(camera.direction.cpy().scl(-speed));
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.translate(camera.direction.cpy().crs(camera.up).nor().scl(-speed));
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.translate(camera.direction.cpy().crs(camera.up).nor().scl(speed));
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) camera.translate(camera.up.cpy().scl(speed));
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) camera.translate(camera.up.cpy().scl(-speed));

        camera.update();
    }

    private int getKeyPressed() {
        for (int key : new int[]{Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D, Input.Keys.SPACE, Input.Keys.SHIFT_LEFT}) {
            if (Gdx.input.isKeyPressed(key)) return key;
        }
        return -1;
    }
}
