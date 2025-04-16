package io.github.noahcraft;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.UBJsonReader;

import java.util.ArrayList;
import java.util.List;

public class Main extends ApplicationAdapter implements InputProcessor {
    // Core 3D rendering components
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private CameraInputController cameraController;

    // UI components
    private SpriteBatch spriteBatch;
    private BitmapFont font;

    // Game objects
    private Array<GameObject> gameObjects = new Array<>();
    private Array<Disposable> disposables = new Array<>();
    private PlayerShip playerShip;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();

    // Game state
    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private float enemySpawnTimer = 0;
    private float enemySpawnInterval = 3f;

    @Override
    public void create() {
        // Initialize camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 6f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 300f;
        camera.update();

        // Initialize model batch and environment
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        // Add some dynamic lighting
        environment.add(new PointLight().set(Color.WHITE, new Vector3(2, 5, 0), 10f));
        environment.add(new PointLight().set(Color.BLUE, new Vector3(-2, 5, 2), 10f));

        // Initialize UI components
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2);

        // Create game floor
        createGameFloor();

        // Create player ship
        createPlayerShip();

        // Set up input processing
        cameraController = new CameraInputController(camera);
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(this);
        inputMultiplexer.addProcessor(cameraController);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    private void createGameFloor() {
        ModelBuilder modelBuilder = new ModelBuilder();

        // Create a textured floor
        Texture floorTexture = new Texture(Gdx.files.internal("badlogic.jpg")); // Replace with your own texture
        disposables.add(floorTexture);

        Material floorMaterial = new Material(
            TextureAttribute.createDiffuse(floorTexture),
            ColorAttribute.createSpecular(0.5f, 0.5f, 0.5f, 1f));

        Model floorModel = modelBuilder.createBox(
            40f, 0.5f, 40f,
            floorMaterial,
            Usage.Position | Usage.Normal | Usage.TextureCoordinates);

        disposables.add(floorModel);

        GameObject floor = new GameObject(new ModelInstance(floorModel));
        floor.transform.translate(0, -3f, 0);
        gameObjects.add(floor);
    }

    private void createPlayerShip() {
        ModelBuilder modelBuilder = new ModelBuilder();

        // Create a simple ship model
        // In a real game, you'd load a model from a file
        Model shipModel = modelBuilder.createCone(
            1f, 2f, 1f, 8,
            new Material(ColorAttribute.createDiffuse(Color.BLUE)),
            Usage.Position | Usage.Normal);

        disposables.add(shipModel);

        playerShip = new PlayerShip(new ModelInstance(shipModel));
        playerShip.transform.translate(0, 0, 0);
        // Rotate to point forward
        playerShip.transform.rotate(Vector3.X, 90);
        gameObjects.add(playerShip);
    }

    private void spawnEnemy() {
        ModelBuilder modelBuilder = new ModelBuilder();

        // Create a simple enemy model
        Model enemyModel = modelBuilder.createBox(
            1f, 1f, 1f,
            new Material(ColorAttribute.createDiffuse(Color.RED)),
            Usage.Position | Usage.Normal);

        disposables.add(enemyModel);

        Enemy enemy = new Enemy(new ModelInstance(enemyModel));

        // Spawn at random position at top of screen
        float x = MathUtils.random(-15f, 15f);
        enemy.transform.translate(x, 0, -20f);

        enemies.add(enemy);
        gameObjects.add(enemy);
    }

    private void fireProjectile() {
        ModelBuilder modelBuilder = new ModelBuilder();

        // Create a simple projectile model
        Model projectileModel = modelBuilder.createSphere(
            0.3f, 0.3f, 0.3f, 8, 8,
            new Material(ColorAttribute.createDiffuse(Color.YELLOW)),
            Usage.Position | Usage.Normal);

        disposables.add(projectileModel);

        // Create projectile at player's position
        Vector3 position = new Vector3();
        playerShip.transform.getTranslation(position);

        Projectile projectile = new Projectile(new ModelInstance(projectileModel));
        projectile.transform.setTranslation(position);
        projectile.speed = 20f;

        projectiles.add(projectile);
        gameObjects.add(projectile);
    }

    @Override
    public void render() {
        // Clear screen
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);

        // Update game state
        float deltaTime = Gdx.graphics.getDeltaTime();

        if (!gameOver) {
            updateGame(deltaTime);
        }

        // Update camera
        cameraController.update();

        // Render 3D objects
        modelBatch.begin(camera);
        for (GameObject gameObject : gameObjects) {
            modelBatch.render(gameObject.instance, environment);
        }
        modelBatch.end();

        // Render UI
        renderUI();
    }

    private void updateGame(float deltaTime) {
        // Handle player input
        handlePlayerInput(deltaTime);

        // Update enemies
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(deltaTime);

            // Remove enemies that go off screen
            Vector3 position = new Vector3();
            enemy.transform.getTranslation(position);

            if (position.z > 10f) {
                enemies.remove(i);
                gameObjects.removeValue(enemy, true);
                lives--;

                if (lives <= 0) {
                    gameOver = true;
                }
            }
        }

        // Update projectiles
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(deltaTime);

            // Remove projectiles that go off screen
            Vector3 position = new Vector3();
            projectile.transform.getTranslation(position);

            if (position.z < -30f) {
                projectiles.remove(i);
                gameObjects.removeValue(projectile, true);
            }

            // Check for collisions with enemies
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);

                if (checkCollision(projectile, enemy)) {
                    // Remove both projectile and enemy
                    projectiles.remove(i);
                    gameObjects.removeValue(projectile, true);

                    enemies.remove(j);
                    gameObjects.removeValue(enemy, true);

                    score += 100;
                    break;
                }
            }
        }

        // Spawn new enemies
        enemySpawnTimer += deltaTime;
        if (enemySpawnTimer >= enemySpawnInterval) {
            spawnEnemy();
            enemySpawnTimer = 0;

            // Make game progressively harder
            enemySpawnInterval = Math.max(0.5f, enemySpawnInterval * 0.99f);
        }
    }

    private boolean checkCollision(GameObject a, GameObject b) {
        Vector3 posA = new Vector3();
        Vector3 posB = new Vector3();

        a.transform.getTranslation(posA);
        b.transform.getTranslation(posB);

        // Simple distance-based collision
        return posA.dst(posB) < 1f;
    }

    private void handlePlayerInput(float deltaTime) {
        float speed = 10f * deltaTime;

        // Handle player movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerShip.transform.translate(-speed, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerShip.transform.translate(speed, 0, 0);
        }

        // Clamp player to screen bounds
        Vector3 position = new Vector3();
        playerShip.transform.getTranslation(position);
        position.x = MathUtils.clamp(position.x, -15f, 15f);
        playerShip.transform.setTranslation(position);
    }

    private void renderUI() {
        spriteBatch.begin();
        font.draw(spriteBatch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
        font.draw(spriteBatch, "Lives: " + lives, 20, Gdx.graphics.getHeight() - 60);

        if (gameOver) {
            String gameOverText = "GAME OVER";
            font.draw(spriteBatch, gameOverText,
                Gdx.graphics.getWidth() / 2f - 100,
                Gdx.graphics.getHeight() / 2f);

            String restartText = "Press ENTER to restart";
            font.draw(spriteBatch, restartText,
                Gdx.graphics.getWidth() / 2f - 150,
                Gdx.graphics.getHeight() / 2f - 40);
        }

        spriteBatch.end();
    }

    private void restartGame() {
        // Clear game objects
        for (GameObject obj : gameObjects) {
            if (!(obj instanceof PlayerShip) && !(obj == gameObjects.first())) {
                gameObjects.removeValue(obj, true);
            }
        }

        enemies.clear();
        projectiles.clear();

        // Reset game state
        score = 0;
        lives = 3;
        gameOver = false;
        enemySpawnTimer = 0;
        enemySpawnInterval = 3f;

        // Reset player position
        playerShip.transform.setToTranslation(0, 0, 0);
        playerShip.transform.rotate(Vector3.X, 90);
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        spriteBatch.dispose();
        font.dispose();

        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.SPACE && !gameOver) {
            fireProjectile();
            return true;
        }

        if (keycode == Input.Keys.ENTER && gameOver) {
            restartGame();
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    // Main entry point

    // Game object classes

    class GameObject {
        public ModelInstance instance;
        public Matrix4 transform;

        public GameObject(ModelInstance instance) {
            this.instance = instance;
            this.transform = instance.transform;
        }

        public void update(float deltaTime) {
            // Base update method
        }
    }

    class PlayerShip extends GameObject {
        public float speed = 15f;

        public PlayerShip(ModelInstance instance) {
            super(instance);
        }

        @Override
        public void update(float deltaTime) {
            // Player-specific update logic
        }
    }

    class Enemy extends GameObject {
        public float speed = 5f;

        public Enemy(ModelInstance instance) {
            super(instance);
        }

        @Override
        public void update(float deltaTime) {
            // Move enemy toward player
            transform.translate(0, 0, speed * deltaTime);
        }
    }

    class Projectile extends GameObject {
        public float speed = 15f;
        public float lifeTime = 3f;
        private float timeAlive = 0f;

        public Projectile(ModelInstance instance) {
            super(instance);
        }

        @Override
        public void update(float deltaTime) {
            // Move projectile forward
            transform.translate(0, 0, -speed * deltaTime);

            // Update lifetime
            timeAlive += deltaTime;
        }

        public boolean isExpired() {
            return timeAlive > lifeTime;
        }
    }
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        // Default implementation can just return false
        return false;
    }
}
