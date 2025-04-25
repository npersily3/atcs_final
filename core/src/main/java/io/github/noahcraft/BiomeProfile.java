package io.github.noahcraft;

public class BiomeProfile {
    public final int id;
    public final String name;
    public final int baseColor;
    public final double heightModifier;    // How much this biome affects height
    public final double temperatureModifier; // How much this biome affects temperature
    public final double moistureModifier;   // How much this biome affects moisture

    public BiomeProfile(int id, String name, int baseColor,
                        double heightMod, double tempMod, double moistMod) {
        this.id = id;
        this.name = name;
        this.baseColor = baseColor;
        this.heightModifier = heightMod;
        this.temperatureModifier = tempMod;
        this.moistureModifier = moistMod;
    }
}
