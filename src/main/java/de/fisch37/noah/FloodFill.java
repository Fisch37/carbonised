package de.fisch37.noah;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Optional;

// WARNING: BLACK MAGIC AHEAD
public abstract class FloodFill {
    private final BlockPos center;
    private final Vec3i localCenter;
    private final byte radius;

    private final @Nullable Node[][][] area;
    // A better nerd might do this in an array if they knew maximum number of source nodes.
    // I don't though, and I spent considerable time trying.
    private LinkedList<Vec3i> sources = new LinkedList<>();
    private LinkedList<Vec3i> temp_sources = new LinkedList<>();

    protected FloodFill(
            BlockPos center,
            byte radius
    ) {
        this.center = center;
        this.radius = radius;

        final int dim = 2*radius + 1;
        area = new Node[dim][dim][dim];
        localCenter = new Vec3i(radius, radius, radius);
        area[radius][radius][radius] = new Node(false);
        sources.add(localCenter);
    }

    public Optional<BlockPos> search() {
        for (byte b = 0; b < radius; b++) {
            @Nullable Vec3i searchResult = step();
            if (searchResult != null)
                return Optional.of(localToGlobal(searchResult));
        }
        for (Vec3i pos : sources) {
            if (matches(localToGlobal(pos)))
                return Optional.of(localToGlobal(pos));
        }
        return Optional.empty();
    }

    protected abstract boolean matches(BlockPos pos);

    protected abstract void visit(BlockPos pos);

    protected boolean filter(BlockPos pos) {
        return true;
    }

    private @Nullable Vec3i step() {
        // Redirection shenanigans
        LinkedList<Vec3i> local_sources = sources;
        sources = temp_sources;
        for (Vec3i pos : local_sources) {
            final BlockPos global = localToGlobal(pos);
            Node node = get(pos);
            assert node != null;
            if (!node.isDead()) {
                visit(global);
                if (matches(global)) {
                    return pos;
                }
            }
            propagateNode(pos);
        }
        local_sources.clear();
        temp_sources = local_sources;
        return null;
    }

    private @Nullable Node get(Vec3i pos) {
        return area[pos.getX()][pos.getY()][pos.getZ()];
    }

    private void set(Vec3i pos, Node val) {
        area[pos.getX()][pos.getY()][pos.getZ()] = val;
    }

    private BlockPos localToGlobal(Vec3i local) {
        return center.add(local).subtract(localCenter);
    }


    private void propagateNode(Vec3i pos) {
        Node node = get(pos);
        assert node != null;
        for (byte b = 0; b < Node.CHILD_DIRECTIONS.length; b++) {
            Vec3i direction = node.children[b];
            if (direction == null) continue;
            Vec3i childPos = pos.add(direction);
            BlockPos childGlobal = localToGlobal(childPos);

            Node child = get(childPos);
            if (child == null) {
                child = new Node(node.isDead() || !filter(childGlobal));
                set(childPos, child);
                sources.add(childPos);
            } else if (child.isDead()) {
                 child.resurrect();
            }
            child.removeOrigin(b);
        }
    }

    private static final class Node {
        private final @Nullable Vec3i[] children = CHILD_DIRECTIONS.clone();
        private boolean dead;

        private Node(boolean dead) {
            this.dead = dead;
        }

        private void resurrect() {
            dead = true;
        }

        private boolean isDead() {
            return dead;
        }

        private void removeOrigin(byte dirIndex) {
            children[getInverse(dirIndex)] = null;
        }

        // I considered adding diagonals to fix some odd behaviour with trapdoors.
        // Here's why I didn't: in theory checking a cube instead of a diamond shape tests against twice as many blocks.
        // However, the shell of a 3x3x3 diamond is 8 blocks (ergo 8 directions).
        // The shell of a 3x3x3 cube is 26 blocks. 26 directions looped for every Node.
        // This means performance as of right now would be <em>much</em> worse.
        // My machine can handle it, but weaker devices might not.
        // There might be a possible optimisation with the Noah algorithm such that we avoid revisiting blocks.
        // If this is indeed possible, diagonals may be viable.
        private final static Vec3i[] CHILD_DIRECTIONS = new Vec3i[]{
                new Vec3i(1,0,0),
                new Vec3i(-1,0,0),
                new Vec3i(0,0,1),
                new Vec3i(0,0,-1),
                new Vec3i(0,1,0),
                new Vec3i(0,-1,0),
        };

        private static byte getInverse(byte dirIndex) {
            // This thing is cursed but the math checks out
            if ((dirIndex & 1) == 0) {
                return (byte) (dirIndex + 1);
            } else {
                return (byte) (dirIndex - 1);
            }
        }
    }
}
