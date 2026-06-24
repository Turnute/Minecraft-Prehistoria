package turnute.mcprehistoria.world.feature;

final class DinoJungleMath {
    static final int MIN_Y = -40;
    static final int MAX_Y = 10;

    private DinoJungleMath() {}

    static int floorY(int x, int z) {
        int broad = hashRange(x >> 3, z >> 3, 0, 4);
        int detail = hashRange(x, z, 0, 2);
        return MIN_Y + 3 + broad + detail;
    }

    static int roofY(int x, int z) {
        int broad = hashRange(x >> 4, z >> 4, 1, 4);
        int detail = hashRange(x, z, 2, 2);
        return MAX_Y - 3 - broad - detail;
    }

    static int hashRange(int x, int z, int salt, int bound) {
        return Math.floorMod(hash(x, z, salt), bound);
    }

    static boolean chance(int x, int y, int z, int salt, int chance) {
        return Math.floorMod(hash(x ^ y, z, salt), chance) == 0;
    }

    private static int hash(int x, int z, int salt) {
        int h = x * 73428767 ^ z * 91227153 ^ salt * 42317861;
        h ^= (h >>> 13);
        h *= 1274126177;
        h ^= (h >>> 16);
        return h;
    }
}
