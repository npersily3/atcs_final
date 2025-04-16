package io.github.noahcraft;

public class PerlinNoise {
    // Hash lookup table as defined by Ken Perlin
    // This is a randomly arranged array of all numbers from 0-255 inclusive
    private static final int[] permutation = {
        151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225,
        140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148,
        247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32,
        57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175,
        74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122,
        60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54,
        65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169,
        200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64,
        52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212,
        207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213,
        119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9,
        129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104,
        218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241,
        81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157,
        184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93,
        222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
    };

    // Doubled permutation to avoid overflow
    private static final int[] p = new int[512];

    static {
        for (int i = 0; i < 512; i++) {
            p[i] = permutation[i & 255];
        }
    }

    // Fade function as defined by Ken Perlin
    // This is a quintic interpolation curve
    private static double fade(double t) {
        // 6t^5 - 15t^4 + 10t^3
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    // Linear interpolation
    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    // Gradient function finds the dot product between a pseudorandom gradient vector and the vector from the input coordinate to the 8 surrounding points
    private static double grad(int hash, double x, double y, double z) {
        // Use only the lowest 4 bits of the hash (0-15)
        int h = hash & 15;

        // Map the hash to one of 12 gradient vectors
        double gradX = 0, gradY = 0, gradZ = 0;

        switch (h % 12) { // Ensure we only get values 0-11
            case 0:  gradX =  1; gradY =  1; gradZ =  0; break;
            case 1:  gradX = -1; gradY =  1; gradZ =  0; break;
            case 2:  gradX =  1; gradY = -1; gradZ =  0; break;
            case 3:  gradX = -1; gradY = -1; gradZ =  0; break;
            case 4:  gradX =  1; gradY =  0; gradZ =  1; break;
            case 5:  gradX = -1; gradY =  0; gradZ =  1; break;
            case 6:  gradX =  1; gradY =  0; gradZ = -1; break;
            case 7:  gradX = -1; gradY =  0; gradZ = -1; break;
            case 8:  gradX =  0; gradY =  1; gradZ =  1; break;
            case 9:  gradX =  0; gradY = -1; gradZ =  1; break;
            case 10: gradX =  0; gradY =  1; gradZ = -1; break;
            case 11: gradX =  0; gradY = -1; gradZ = -1; break;
        }

        // Calculate the dot product between gradient vector and offset vector
        return gradX * x + gradY * y + gradZ * z;
    }

    // Classic Perlin noise function - 3D version
    /**
     * Classic Perlin noise function with un-nested lerp statements for clarity
     */
    public static double noise(double x, double y, double z) {
        // Find unit cube that contains the point
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        int Z = (int) Math.floor(z) & 255;

        // Find relative x, y, z of point in cube
        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);

        // Compute fade curves for each of x, y, z
        double u = fade(x);
        double v = fade(y);
        double w = fade(z);

        // Hash coordinates of the 8 cube corners
        int A = p[X] + Y;
        int AA = p[A] + Z;
        int AB = p[A + 1] + Z;
        int B = p[X + 1] + Y;
        int BA = p[B] + Z;
        int BB = p[B + 1] + Z;

        // Calculate the gradients for all 8 corners of the cube
        double gradAA = grad(p[AA], x, y, z);         // (0,0,0) relative to cube
        double gradBA = grad(p[BA], x-1, y, z);       // (1,0,0) relative to cube
        double gradAB = grad(p[AB], x, y-1, z);       // (0,1,0) relative to cube
        double gradBB = grad(p[BB], x-1, y-1, z);     // (1,1,0) relative to cube
        double gradAA1 = grad(p[AA+1], x, y, z-1);    // (0,0,1) relative to cube
        double gradBA1 = grad(p[BA+1], x-1, y, z-1);  // (1,0,1) relative to cube
        double gradAB1 = grad(p[AB+1], x, y-1, z-1);  // (0,1,1) relative to cube
        double gradBB1 = grad(p[BB+1], x-1, y-1, z-1);// (1,1,1) relative to cube

        // Step 1: Interpolate along X axis (4 lerps)
        double x1 = lerp(u, gradAA, gradBA);   // Bottom edge of front face
        double x2 = lerp(u, gradAB, gradBB);   // Top edge of front face
        double x3 = lerp(u, gradAA1, gradBA1); // Bottom edge of back face
        double x4 = lerp(u, gradAB1, gradBB1); // Top edge of back face

        // Step 2: Interpolate along Y axis (2 lerps)
        double y1 = lerp(v, x1, x2);  // Front face
        double y2 = lerp(v, x3, x4);  // Back face

        // Step 3: Interpolate along Z axis (1 lerp)
        double result = lerp(w, y1, y2);

        return result;
    }

    // Convenience method for 2D noise
    public static double noise(double x, double y) {
        return noise(x, y, 0);
    }

    // Helper method to generate octaved Perlin noise (layering multiple frequencies)
    public static double octaveNoise(double x, double y, int octaves, double persistence) {
        double total = 0;
        double frequency = 1;
        double amplitude = 1;
        double maxValue = 0;  // Used for normalizing result

        for(int i = 0; i < octaves; i++) {
            total += noise(x * frequency, y * frequency) * amplitude;

            maxValue += amplitude;

            amplitude *= persistence;
            frequency *= 2;
        }

        return total/maxValue;
    }

    // Example usage
    public static void main(String[] args) {
        // Generate a simple 2D noise map
        int width = 20;
        int height = 20;

        System.out.println("2D Perlin Noise Map:");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Sample noise at different scales
                double value = noise(x * 0.1, y * 0.1);

                // Map the noise value (-1 to 1) to a character
                char c = mapToChar(value);
                System.out.print(c);
            }
            System.out.println();
        }

        System.out.println("\nOctaved Perlin Noise Map (more detailed):");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Sample octaved noise for more natural looking results
                double value = octaveNoise(x * 0.1, y * 0.1, 4, 0.5);

                // Map the noise value (0 to 1) to a character
                char c = mapToChar(value);
                System.out.print(c);
            }
            System.out.println();
        }
    }

    // Helper method to visualize noise values as ASCII
    private static char mapToChar(double value) {
        // Map the noise value from -1,1 to 0,1
        value = (value + 1) / 2.0;

        // Map to ASCII characters of increasing "density"
        String chars = " .:-=+*#%@";
        int index = (int) Math.min(chars.length() - 1, Math.floor(value * chars.length()));
        return chars.charAt(index);
    }
}
