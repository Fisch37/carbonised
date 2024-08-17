package de.fisch37.carbonised;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class Gamerules {
    public static final GameRules.Key<GameRules.IntRule> FLOOD_RADIUS = GameRuleRegistry.register(
            "oxidisationRadius",
            GameRules.Category.UPDATES,
            GameRuleFactory.createIntRule(4)
    );
}
