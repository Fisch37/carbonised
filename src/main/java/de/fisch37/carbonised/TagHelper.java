package de.fisch37.carbonised;

import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import static de.fisch37.carbonised.Carbonised.MOD_ID;

public class TagHelper {
    private static final Identifier ID_ACCELERATES_DEGRADATION = of("accelerates_oxidisation");
    public static final TagKey<Fluid> ACCELERATES_DEGRADATION = TagKey.of(
            RegistryKeys.FLUID,
            ID_ACCELERATES_DEGRADATION
    );

    private static Identifier of(String name) {
        return Identifier.of(MOD_ID, name);
    }
}
