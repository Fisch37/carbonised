package de.fisch37.carbonised.mixin;

import de.fisch37.carbonised.MinOxidizationSearch;
import de.fisch37.carbonised.TagHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Degradable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

import static de.fisch37.carbonised.Carbonised.LOGGER;

@Mixin(Degradable.class)
public interface DegradableMixin {
    /**
     * @author Fisch37
     * @reason Literally the point + this is less error-prone than a cancelling HEAD Injection
     */
    @Overwrite
    default Optional<BlockState> tryDegrade(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        final byte RANGE = 4;

        if (random.nextFloat() > getDegradationChance(world, pos)) {
            return Optional.empty();
        }

        MinOxidizationSearch search = new MinOxidizationSearch(world, pos, RANGE);
        BlockPos leastOxidized = search.aggregate().getLeft();

        // Updating here is a bit janky, but it is basically the only good way
        final BlockState oldState = world.getBlockState(leastOxidized);
        try {
            final Optional<BlockState> newState = ((Degradable<?>) oldState.getBlock())
                    .getDegradationResult(oldState);
            newState.ifPresent(updatedState -> world.setBlockState(leastOxidized, updatedState));
        } catch (Exception e) {
            LOGGER.error("Avoided a crash during degradation attempt!", e);
        }

        return Optional.empty();
    }

    @Unique
    default float getDegradationChance(ServerWorld world, BlockPos pos) {
        int freeDirections = Direction.values().length;
        int acceleration = 0;
        for (Direction direction : Direction.values()) {
            BlockPos testPos = pos.offset(direction);
            BlockState testState = world.getBlockState(testPos);
            boolean covered = testState.isSideSolidFullSquare(world, testPos, direction.getOpposite());
            if (covered) {
                freeDirections--;
            }
            else if (world.getFluidState(testPos).isIn(TagHelper.ACCELERATES_DEGRADATION)) {
                // No acceleration if the block is water-opaque in that direction
                acceleration++;
            }
        }
        return ((Degradable<?>)this).getDegradationChanceMultiplier()
                * ((float) (freeDirections + acceleration) / Direction.values().length);
    }
}
