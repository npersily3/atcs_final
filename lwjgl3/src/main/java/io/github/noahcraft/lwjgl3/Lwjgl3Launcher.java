package main.java.io.github.noahcraft.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import main.java.io.github.noahcraft.Main;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        // Set JVM arguments to increase heap space
        System.setProperty("org.lwjgl.system.allocator", "system");

        // Add JVM argument for larger heap size
        if (System.getProperty("java.vm.vendor", "").contains("Oracle") ||
            System.getProperty("java.vm.name", "").contains("OpenJDK")) {
            System.out.println("For larger heap size, run with: -Xmx2048m or higher");
        }

        if (io.github.noahcraft.lwjgl3.StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("NoahCraft");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);

        // Set a reasonable window size - bigger than before for better visibility
        configuration.setWindowedMode(800, 600);

        // Enable OpenGL debug for tracking potential leaks
        configuration.enableGLDebugOutput(true, System.err);

        // Remove the window icon setting since the files don't exist
        // If you want icons, place them in the assets folder and uncomment:
        // configuration.setWindowIcon("icons/icon128.png", "icons/icon64.png", "icons/icon32.png", "icons/icon16.png");

        return configuration;
    }
}
