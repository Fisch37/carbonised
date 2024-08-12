package de.fisch37.carbonised.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Degradable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;

@Mixin(Degradable.class)
public interface DegradableMixin {
    /**
     * @author Fisch37
     * @reason Literally the point + this is less error-prone than a cancelling HEAD Injection
     */
    @Overwrite
    default Optional<BlockState> tryDegrade(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        final int RANGE = 4;

        Degradable<?> obj = (Degradable<?>)((Object)this);
        if (random.nextFloat() > obj.getDegradationChanceMultiplier()) {
            return Optional.empty();
        }

        BlockPos leastOxidized = pos;
        Enum<?> leastOxidizedLevel = obj.getDegradationLevel();
        for (BlockPos target : BlockPos.iterateOutwards(pos, RANGE, RANGE, RANGE)) {
            int distance = target.getManhattanDistance(pos);
            if (distance > 4) continue;

            BlockState targetState = world.getBlockState(target);
            if (!(targetState.getBlock() instanceof Degradable<?> targetBlock)) continue;

            Enum<?> targetLevel = targetBlock.getDegradationLevel();
            if (targetLevel.getClass() != leastOxidizedLevel.getClass()) continue;

            if (targetLevel.ordinal() < leastOxidizedLevel.ordinal()) {
                leastOxidized = target;
                leastOxidizedLevel = targetLevel;
            }
        }
        // Updating here is a bit janky, but it is basically the only good way
        final BlockState oldState = world.getBlockState(leastOxidized);
        if ((oldState.getBlock() instanceof Degradable<?>)) {
            final Optional<BlockState> newState = ((Degradable<?>) oldState.getBlock())
                    .getDegradationResult(oldState);
            BlockPos finalLeastOxidized = leastOxidized;
            newState.ifPresent(updatedState -> world.setBlockState(finalLeastOxidized, updatedState));
        } else {
            System.err.println("Avoided CCE in DegradableMixin from Carbonised");
        }

        return Optional.empty();
    }
}
