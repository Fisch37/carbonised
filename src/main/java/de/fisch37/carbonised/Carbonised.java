package de.fisch37.carbonised;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Carbonised implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Carbonised.class);
    public static final String MOD_ID = "carbonised";
    public static final GameRules.Key<GameRules.IntRule> FLOOD_RADIUS = GameRuleRegistry.register(
            "oxidisationRadius",
            GameRules.Category.UPDATES,
            GameRuleFactory.createIntRule(4, 0, 127)
    );

    @Override
    public void onInitialize() {
    }
}
