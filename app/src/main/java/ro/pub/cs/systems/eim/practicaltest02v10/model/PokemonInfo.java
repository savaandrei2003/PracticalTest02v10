package ro.pub.cs.systems.eim.practicaltest02v10.model;

public class PokemonInfo {

    private final String name;
    private final String types;      // ex: "electric"
    private final String abilities;  // ex: "static, lightning-rod"
    private final String imageUrl;   // sprites.front_default

    public PokemonInfo(String name, String types, String abilities, String imageUrl) {
        this.name = name;
        this.types = types;
        this.abilities = abilities;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public String getTypes() { return types; }
    public String getAbilities() { return abilities; }
    public String getImageUrl() { return imageUrl; }
}