package de.fisch37.noah;

import net.minecraft.util.math.BlockPos;

public abstract class FloodAggregate<T> extends FloodFill {
    private T state;

    protected FloodAggregate(BlockPos center, byte radius) {
        super(center, radius);
    }

    public T aggregate() {
        search();
        return getResult();
    }

    public T getResult() {
        return state;
    }

    @Override
    protected boolean matches(BlockPos pos) {
        return false;
    }

    @Override
    protected void visit(BlockPos pos) {
        state = function(state, pos);
    }

    protected abstract T function(T state, BlockPos pos);
}
