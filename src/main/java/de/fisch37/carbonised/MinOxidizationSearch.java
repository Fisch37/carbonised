package de.fisch37.carbonised;

import de.fisch37.noah.FloodAggregate;
import net.minecraft.block.Degradable;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MinOxidizationSearch extends FloodAggregate<Pair<BlockPos, Enum<?>>> {
    private final World world;
    private final Pair<BlockPos, Enum<?>> center;

    public MinOxidizationSearch(World world, BlockPos center, byte radius) {
        super(center, radius);
        this.world = world;
        this.center = getDegradation(center).orElseThrow(() -> new IllegalStateException("Degradation center was non-degradable"));
    }

    // NOTE: Redundant block state call
    //  This is a small performance loss buuut it doesn't really matter.
    //  This mod doesn't have any performance issues and besides:
    //  Minecraft stores blocks in a hash map. That's decently fast
    @Override
    protected boolean filter(BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof Degradable<?>;
    }

    private Optional<Pair<BlockPos, Enum<?>>> getDegradation(BlockPos pos) {
        if (world.getBlockState(pos).getBlock() instanceof Degradable<?> block) {
            return Optional.of(new Pair<>(pos, block.getDegradationLevel()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected Pair<BlockPos, Enum<?>> function(@Nullable Pair<BlockPos, Enum<?>> current, BlockPos pos) {
        if (current == null) current = center;

        Pair<BlockPos, Enum<?>> potential = getDegradation(pos).orElseThrow();
        if (potential.getRight().ordinal() < current.getRight().ordinal()) {
            return potential;
        } else {
            return current;
        }
    }
}
