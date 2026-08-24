package me.liwk.karhu.util;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.evictinglist.EvictingList;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.mc.axisalignedbb.AxisAlignedBB;
import me.liwk.karhu.util.mc.vec.Vec3;
import me.liwk.karhu.util.mc.vec.Vec3d;
import me.liwk.karhu.util.player.BlockUtil;
import me.liwk.karhu.util.tuple.Tuple;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MathUtil {

    public static final double LN_2 = Math.log(2D);
    private static final float PI_2 = 1.5707963F; // Math.PI / 2

    private static final double FAST_MATH_ERROR = 3.0D / 4096.0D;

    private static final Set<Integer> FORWARD_DIRECTION = new HashSet<>(Arrays.asList(0, 45, 360));
    private static final Set<Integer> BACKWARD_DIRECTION = new HashSet<>(Arrays.asList(135, 180));
    private static final Set<Integer> SIDE_DIRECTION = new HashSet<>(Arrays.asList(45, 135, 90));

    public static float fastatan2_float(float y, float x) {
        float angle;
        if (x == 0f && y == 0f) {
            return 0;
        }
        if (y >= 0f) {
            if (x >= 0f) {
                angle = y / (x + y);
            } else {
                angle = 1f - x / (-x + y);
            }
        } else {
            if (x < 0f) {
                angle = -y / (-x - y) - 2f;
            } else {
                angle = x / (x - y) - 1f;
            }
        }
        return angle * PI_2;
    }
    
    public static double fastatan2_double(double y, double x) {
        double angle;
        if (x == 0d && y == 0d) {
            return 0;
        }
        if (y >= 0d) {
            if (x >= 0d) {
                angle = y / (x + y);
            } else {
                angle = 1d - x / (-x + y);
            }
        } else {
            if (x < 0d) {
                angle = -y / (-x - y) - 2d;
            } else {
                angle = x / (x - y) - 1d;
            }
        }
        return angle * PI_2;
    }

    public static double getRandomDouble(double maximum, double minimum) {
        Random r = new Random();
        return minimum + (maximum - minimum) * r.nextDouble();
    }

    public static double average(final Iterable<? extends Number> iterable) {
        double n = 0.0;
        int n2 = 0;
        for (Number number : iterable) {
            n += number.doubleValue();
            ++n2;
        }
        return n / n2;
    }

    public static double stdDev(double average, Iterable<? extends Number> numbers) {
        double stdDev = 0.0;
        int i = 0;
        for (Number number : numbers) {
            stdDev += Math.pow(number.doubleValue() - average, 2.0);
            i++;
        }
        stdDev /= i;
        stdDev = Math.sqrt(stdDev);
        return stdDev;
    }

    public static boolean checkStackIntegrity(ItemStack itemStack) {
        if(itemStack != null && itemStack.getType() != Material.AIR) {
            return true;
        }
        return false;
    }

    public static int getIndex(Set<? extends Object> set, Object value) {
        int result = 0;
        for (Object entry:set) {
            if (entry.equals(value)) return result;
            result++;
        }
        return -1;
    }

    public static <E> E randomElement(final Collection<? extends E> collection) {
        if (collection.size() == 0) return null;
        int index = new Random().nextInt(collection.size());

        if (collection instanceof List) {
            return ((List<? extends E>) collection).get(index);
        } else {
            Iterator<? extends E> iter = collection.iterator();
            for (int i = 0; i < index; i++) iter.next();
            return iter.next();
        }
    }

    public static double getEntropy(Collection<? extends Number> values) {
        double n = values.size();

        if (n < 2)
            return Double.NaN;

        Map<Integer, Integer> map = new HashMap<>();

        values.stream()
                .mapToInt(Number::intValue)
                .forEach(value -> map.put(value, map.computeIfAbsent(value, k -> 0) + 1));

        double entropy = map.values().stream()
                .mapToDouble(freq -> (double) freq / n)
                .map(probability -> probability * log2(probability))
                .sum();

        return -entropy;
    }

    private static double log2(double n) {
        return Math.log(n) / LN_2;
    }

    public static double offset(Vector from, Vector to) {
        from.setY(0);
        to.setY(0);
        return to.subtract(from).length();
    }

    public static AxisAlignedBB getHitbox(KarhuPlayer data, AxisAlignedBB baseBox) {
        if (data.lastPos > 0 || data.getMoveTicks() <= 1) {
            baseBox = baseBox.expand(0.1305 + FAST_MATH_ERROR, 0.1305 + FAST_MATH_ERROR, 0.1305 + FAST_MATH_ERROR);
        } else {
            baseBox = baseBox.expand(0.1 + FAST_MATH_ERROR, 0.1 + FAST_MATH_ERROR, 0.1 + FAST_MATH_ERROR);
        }
        return baseBox;
    }

    public static AxisAlignedBB getHitboxLenient(KarhuPlayer data, AxisAlignedBB baseBox) {
        if(data.isNewerThan8()) {
            baseBox = baseBox.expand(0.1, 0.1, 0.1);
        } else if (data.getMoveTicks() <= 2) {
            baseBox = baseBox.expand(0.18, 0.18, 0.18);
        } else {
            baseBox = baseBox.expand(0.15, 0.15, 0.15);
        }
        return baseBox;
    }

    public static double square(double number) {
        return FastMath.pow(number, 2.0D);
    }

    public static double varianceSquared(Number value, Iterable<? extends Number> numbers) {
        double variance = 0.0;
        int i = 0;
        for (Number number : numbers) {
            variance += FastMath.pow(number.doubleValue() - value.doubleValue(), 2.0);
            ++i;
        }
        return variance / (double)(i - 1);
    }

    public static double getDifference(Iterable<? extends Number> list) {
        double i = 0.0;
        double p = -1.0;
        int count = 0;
        for (Number z : list) {
            if (p != -1.0)
                i += Math.abs(p - z.doubleValue());

            p = z.doubleValue();
            ++count;
        }
        return i / count;
    }

    public static double getOscillation(Iterable<? extends Number> samples) {
        return highest(samples) - lowest(samples);
    }


    public static double difference(Iterable<? extends Number> numbers) {
        double total = 0.0;
        double lastNum = 0;
        int i = 0;
        for (Number number : numbers) {
            total += Math.abs(number.doubleValue() - lastNum);
            lastNum = number.doubleValue();
            ++i;
        }
        return total / i;
    }


    public static int getV(Deque<Integer> list) {

        int f = getNumbers(list, 1) + 1;
        int s = getNumbers(list, 2) + 1;
        int t = getNumbers(list, 3) + 1;
        int v = ((f + s) / t) * 50;

        return v / list.size();
    }

    public static int getNumbers(Deque<Integer> list, int num) {
        int amount = 0;
        for (int i : list) {
            if (i == num) ++amount;
        }
        return amount;
    }

    public static int[] getNumbersArray(Deque<Integer> samples, int size) {

        int[] counter = new int[size];

        for(int i : samples) {
            if (i > 0 && i <= size)
                counter[i - 1]++;
        }

        return counter;
    }

    public static int getOutliers(Deque<Integer> list) {
        return (int) list.stream().filter(delay -> delay > 3).count();
    }

    public static double computeAverageDifference(Deque<Integer> deque) {
        if (deque == null || deque.size() < 2) {
            throw new IllegalArgumentException("Deque must contain at least two numbers.");
        }

        double sumOfDifferences = 0.0;
        int count = 0;

        // Use a temporary variable to hold the previous value while traversing.
        Integer prev = deque.pollFirst();

        while (!deque.isEmpty()) {
            Integer current = deque.pollFirst();
            sumOfDifferences += Math.abs(current - prev);
            prev = current;
            count++;
        }

        return sumOfDifferences / count;
    }

    public static int getRepeated(Deque<Integer> list) {
        return (int) list.stream().distinct().count();
    }

    public static int getDuplicatedNumbers(Deque<Integer> list) {
        int amount = 0;
        for (double i : list) {
            for (double ii : list) {
                if (i == ii) {
                    amount++;
                }
            }
        }
        return amount / list.size();
    }

    public static double getStuff(Iterable<Integer> numbers) {
        int previous = -1;
        double n0 = 0;
        for (double z : numbers) {
            n0 += z / 2;
        }
        double statistic = 0.0D;
        for (int number : numbers) {
            if (previous == -1) {
                previous = number;
            } else {
                statistic += (n0 / ((n0 - 1) * (n0 - 2))) * number;
            }
        }
        return (statistic - 2.0) * 2.0;
    }

    public static int getW(Deque<Integer> list) {

        int f = getNumbers(list, 1) + 1;
        int s = getNumbers(list, 2) + 1;
        int t = getNumbers(list, 3) + 1;
        int f1 = getNumbers(list, 4) + 1;
        int f2 = getNumbers(list, 5) + 1;
        int w = ((f + s) / (t + f1 + f2)) * 50;

        return w / list.size();
    }


    public static <T> void removeOldestItems(Collection<T> collection, int numberOfItemsToRemove) {
        if (collection == null || numberOfItemsToRemove <= 0) {
            return;
        }

        if (collection instanceof List) {
            List<T> list = (List<T>) collection;
            if (list.size() > numberOfItemsToRemove) {
                list.subList(0, numberOfItemsToRemove).clear();
            }
        } else if (collection instanceof Deque) {
            Deque<T> deque = (Deque<T>) collection;
            for (int i = 0; i < numberOfItemsToRemove && !deque.isEmpty(); i++) {
                deque.removeFirst();
            }
        } else {
            // For other collection types, create a new list and remove items
            List<T> listCopy = new ArrayList<>(collection);
            if (listCopy.size() > numberOfItemsToRemove) {
                listCopy.subList(0, numberOfItemsToRemove).clear();
                collection.clear();
                collection.addAll(listCopy);
            }
        }
    }


    public static double getRatio(Deque<Integer> list) {
        return (getNumbers(list, 1) + 1.0) / (getNumbers(list, 3) + 1.0);
    }

    public static double[] dequeTranslator(final Collection<? extends Number> numbers) {
        return numbers.stream().mapToDouble(Number::doubleValue).toArray();
    }

    public static final String format(int places, Object obj) {
        return String.format("%." + places + "f", obj);
    }

    public static double hypotFast(double... numbers) {
        double squaredSum = 0.0D;
        for (double number : numbers)
            squaredSum += FastMath.pow(number, 2.0D);
        return sqrt(squaredSum);
    }

    public static double power(double number) {
        return FastMath.pow(number, 2.0D);
    }

    public static <T> Stream<T> stream(T... array) {
        return Arrays.stream(array);
    }

    public static <T> T firstNonNull(@Nullable T t, @Nullable T t2) {
        return t != null ? t : t2;
    }

    public static <T> Queue<T> trim(Queue<T> queue, int n) {
        for (int i = queue.size(); i > n; --i) {
            queue.poll();
        }

        return queue;
    }

    public static double trimDouble(int degree, double d) {
        StringBuilder format = new StringBuilder("#.#");

        for(int i = 1; i < degree; ++i) {
            format.append("#");
        }

        DecimalFormat twoDForm = new DecimalFormat(format.toString());
        return Double.parseDouble(twoDForm.format(d).replaceAll(",", "."));
    }

    public static double getStandardDeviation(Collection<? extends Number> doubles) {
        double average = 0.0;
        double std = 0.0;

        double size = doubles.size();

        for (final Number number : doubles) {
            average += number.doubleValue();
        }

        double nigger = average / size;
        for (Number doubler : doubles) {
            std += FastMath.pow(doubler.doubleValue() - nigger, 2.0);
        }
        return FastMath.sqrt(std / size);
    }

    public static double getStandardDeviation(double[] doubles) {
        double average = 0.0;
        double std = 0.0;

        double size = doubles.length;

        for (double number : doubles) {
            average += number;
        }

        double nigger = average / size;
        for (double doubler : doubles) {
            std += FastMath.pow(doubler - nigger, 2.0);
        }
        return FastMath.sqrt(std / size);
    }

    public static double getVariance(final Collection<? extends Number> data) {
        int count = 0;

        double sum = 0.0;
        double variance = 0.0;

        double average;

        // Increase the sum and the count to find the average and the standard deviation
        for (final Number number : data) {
            sum += number.doubleValue();
            ++count;
        }

        average = sum / count;

        // Run the standard deviation formula
        for (final Number number : data) {
            variance += FastMath.pow(number.doubleValue() - average, 2.0);
        }

        return variance;
    }

    public static double getAverage(Collection<? extends Number> values) {
        double average = 0.0;

        double size = values.size();

        for (final Number number : values) {
            average += number.doubleValue();
        }

        return average / size;
    }


    public static double getSkewness(final Collection<? extends Number> data) {
        double sum = 0;
        int count = 0;

        final List<Double> numbers = Lists.newArrayList();

        // Get the sum of all the data and the amount via looping
        for (final Number number : data) {
            sum += number.doubleValue();
            ++count;

            numbers.add(number.doubleValue());
        }

        // Sort the numbers to run the calculations in the next part
        Collections.sort(numbers);

        // Run the formula to get skewness
        final double mean = sum / count;
        final double median = (count % 2 != 0) ? numbers.get(count / 2) : (numbers.get((count - 1) / 2) + numbers.get(count / 2)) / 2;
        final double variance = getVariance(data);

        return 3 * (mean - median) / variance;
    }

    public static boolean isReallyPlacingBlock(Vector block, Vector player, BlockFace face) {
        switch (face) {
            case UP: {
                return true;
            }
            case DOWN: {
                final double limit = block.getY() - 0.03;
                return player.getY() < limit;
            }
            case WEST: {
                final double limit = block.getX() + 0.03;
                return limit > player.getX();
            }
            case EAST: {
                final double limit = block.getX() + 0.97;
                return player.getX() > limit;
            }
            case NORTH: {
                final double limit = block.getZ() + 0.03;
                return player.getZ() < limit;
            }
            case SOUTH: {
                final double limit = block.getZ() + 0.97;
                return player.getZ() > limit;
            }

            default: return true;
        }
    }


    public static double getKurtosis(final Collection<? extends Number> data) {
        double sum = 0.0;
        int count = 0;

        for (Number number : data) {
            sum += number.doubleValue();
            ++count;
        }

        if (count < 3.0) {
            return 0.0;
        }

        final double efficiencyFirst = count * (count + 1.0) / ((count - 1.0) * (count - 2.0) * (count - 3.0));
        final double efficiencySecond = 3.0 * FastMath.pow(count - 1.0, 2.0) / ((count - 2.0) * (count - 3.0));
        final double average = sum / count;

        double variance = 0.0;
        double varianceSquared = 0.0;

        for (final Number number : data) {
            variance += FastMath.pow(average - number.doubleValue(), 2.0);
            varianceSquared += FastMath.pow(average - number.doubleValue(), 4.0);
        }

        return efficiencyFirst * (varianceSquared / FastMath.pow(variance / sum, 2.0)) - efficiencySecond;
    }

    public static double getKurtosis2(Collection<Double> data) {
        double size = data.size();
        if (size < 3.0) {
            return Double.NaN;
        }
        double black = data.stream().mapToDouble(value -> value).average().getAsDouble();
        double dark = MathUtil.getStandardDeviation(data.stream().mapToDouble(value -> value).toArray());
        AtomicDouble atomicDouble = new AtomicDouble(0.0);
        data.forEach(value -> atomicDouble.getAndAdd(FastMath.pow(value - black, 4.0)));
        return size * (size + 1.0) / (size - 1.0) * (size - 2.0) * (size - 3.0) * atomicDouble.get() / FastMath.pow(dark, 4.0) - 3.0 * FastMath.pow(size - 1.0, 2.0) / (size - 2.0) * (size - 3.0);
    }

    public static double getMedian(final List<Double> data) {
        if (data.size() % 2 == 0) {
            return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
        } else {
            return data.get(data.size() / 2);
        }
    }

    public static double getCPS(final Collection<? extends Number> data) {
        final double average = data.stream().mapToDouble(Number::doubleValue).average().orElse(0.0);

        return 20 / average;
    }

    public static double distanceToHorizontalCollision(double position) {
        double dividedPos = Math.abs(position) % 0.0015625d;
        return Math.min(dividedPos, Math.abs(dividedPos - 0.0015625d));
    }

    public static double getCPSLong(Collection<? extends Number> values) {
        return 1000D / getAverage(values);
    }

    public static float getMoveAngle(CustomLocation from, CustomLocation to, boolean clamp) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();

        float moveAngle = (float) (Math.toDegrees(FastMath.atan2(dz, dx)) - 90F); // have to subtract by 90 because minecraft does it

        return clamp
                ? Math.abs(wrapAngleTo180_float(moveAngle - to.getYaw()))
                : Math.abs(moveAngle - to.getYaw());
    }

    public static Vector getMoveChange(CustomLocation from, CustomLocation to, KarhuPlayer data) {

        final float friction = data.isLastOnGroundPacket() ? data.getCurrentFriction() : 0.91F;

        double dx = (to.getX() - from.getX()) / friction;
        double dz = (to.getZ() - from.getZ()) / friction;

        if(data.isJumped()) {
            float f = to.yaw * (float)Math.PI / 180.0f;
            dx += (MathHelper.sin(f) * 0.2F);
            dz -= (MathHelper.cos(f) * 0.2F);
        }

        dx -= data.deltas.lastDX;
        dz -= data.deltas.lastDZ;

        return new Vector(dx, 0.0, dz);
    }

    public static float[] getStrafeForward(CustomLocation from, CustomLocation to, KarhuPlayer data) {
        float forward = 0F;
        float strafe = 0F;

        final float friction = data.isLastOnGroundPacket() ? data.getCurrentFriction() : 0.91F;

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();

        dx /= friction;
        dz /= friction;

        if(data.isJumped()) {
            float f = to.yaw * (float)Math.PI / 180.0f;
            dx += (MathHelper.sin(f) * 0.2F);
            dz -= (MathHelper.cos(f) * 0.2F);
        }

        dx -= data.deltas.lastDX;
        dz -= data.deltas.lastDZ;

        Vector move = new Vector(dx, 0.0, dz);
        if (move.length() < 0.01D) {
            return new float[]{0F, 0F};
        }

        move.normalize();
        Vector angle = new Vector(-Math.sin(Math.toRadians(to.getYaw())), 0.0, Math.cos(Math.toRadians(to.getYaw())));

        double degree = Math.toDegrees(angle.angle(move));

        for(int direction : FORWARD_DIRECTION) {

            double diff = Math.abs(direction - degree);

            if (diff < 5F) {
                forward = 1F;
                break;
            }

        }

        if (forward != 1F) {
            for (int direction : BACKWARD_DIRECTION) {

                double diff = Math.abs(direction - degree);

                if (diff < 5F) {
                    forward = -1F;
                    break;
                }
            }
        }

        for(int direction : SIDE_DIRECTION) {

            double diff = Math.abs(direction - degree);

            if (diff < 5F) {
                strafe = angle.getX() * move.getZ() - angle.getZ() * move.getX() > 0.0 ? 1 : -1;
                break;
            }
        }

        return new float[]{forward, strafe};


    }

    public static float getMoveAngleNoAbs(CustomLocation from, CustomLocation to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();

        float moveAngle = (float) (Math.toDegrees(FastMath.atan2(dz, dx)) - 90F); // have to subtract by 90 because minecraft does it

        return wrapAngleTo180_float(moveAngle - to.getYaw());
    }

    public static Vector getVectorSpeed(CustomLocation from, CustomLocation to) {
        return new Vector(to.getX() - from.getX(), 0.0D, to.getZ() - from.getZ());
    }

    public static Vector getDirection(KarhuPlayer data) {
        return new Vector(-MathHelper.sin(data.getLocation().getYaw() * ((float)Math.PI) / 180.0F) * 1.0D * 0.5D, 0.0D,
                MathHelper.cos(data.getLocation().getYaw() * ((float)Math.PI) / 180.0F) * 1.0D * 0.5D);
    }


    public static Vector getDirection2(float yaw, float pitch) {
        Vector vector = new Vector();
        vector.setY(-Math.sin(Math.toRadians(pitch)));
        double xz = Math.cos(Math.toRadians(pitch));
        vector.setX(-xz * Math.sin(Math.toRadians(yaw)));
        vector.setZ(xz * Math.cos(Math.toRadians(yaw)));
        return vector;
    }

    public static double getDirectionShit(Location from, Location to) {
        if (from == null || to == null) {
            return 0.0D;
        }
        double difX = to.getX() - from.getX();
        double difZ = to.getZ() - from.getZ();

        return (float) ((FastMath.atan2(difZ, difX) * 180.0D / Math.PI) - 90.0F);
    }

    public static double lowestAbs(Iterable<? extends Number> iterable) {
        Double value = null;
        Iterator var2 = iterable.iterator();

        while (true) {
            Number n;
            do {
                if (!var2.hasNext()) {
                    return (Double) firstNonNull(value, 0.0D);
                }

                n = (Number) var2.next();
            } while (value != null && Math.abs(n.doubleValue()) >= Math.abs(value));

            value = n.doubleValue();
        }
    }

    public static double lowest(Iterable<? extends Number> numbers) {
        double lowest = Double.MAX_VALUE;
        int i = 0;
        for (Number number : numbers) {
            if (number.doubleValue() < lowest) {
                lowest = number.doubleValue();
            }
            ++i;
        }
        return lowest;
    }

    public static double highest(Iterable<? extends Number> numbers) {
        double lowest = 0;
        int i = 0;
        for (Number number : numbers) {
            if (number.doubleValue() > lowest) {
                lowest = number.doubleValue();
            }
            ++i;
        }
        return lowest;
    }

    public static float averageFloat(List<Float> list) {
        float avg = 0.0f;
        for (float value : list) {
            avg += value;
        }
        if (list.size() > 0) {
            return avg / (float) list.size();
        }
        return 0.0f;
    }

    public static float averageLong(Deque<Long> list) {
        float avg = 0;
        for (float value : list) {
            avg += value;
        }
        if (list.size() > 0) {
            return avg / list.size();
        }
        return 0;
    }

    public static Double findMin(EvictingList<Double> list)
    {

        // check list is empty or not
        if (list == null || list.size() == 0) {
            return Double.MAX_VALUE;
        }

        // create a new list to avoid modification
        // in the original list
        EvictingList<Double> sortedlist = new EvictingList<Double>(list.size());

        // sort list in natural order
        Collections.sort(sortedlist);

        // first element in the sorted list
        // would be minimum
        return sortedlist.get(0);
    }

    public static Double findMax(EvictingList<Double> list)
    {

        // check list is empty or not
        if (list == null || list.size() == 0) {
            return Double.MIN_VALUE;
        }

        // create a new list to avoid modification
        // in the original list
        EvictingList<Double> sortedlist = new EvictingList<Double>(list.size());

        // sort list in natural order
        Collections.sort(sortedlist);

        // last element in the sorted list would be maximum
        return sortedlist.get(sortedlist.size() - 1);
    }

    public static int getPingInTicks(long ping) {
        return (int) Math.floor(ping / 50.);
    }

    public static int getPingToTimer(long ping) {
        return (int) ping / 10000;
    }

    public static double deviation(Iterable<? extends Number> iterable) {
        return FastMath.sqrt(deviationSquared(iterable));
    }

    public static boolean onGround(double coord) {
        return coord % 0.015625 == 0;
    }

    public static double deviationSquared(Iterable<? extends Number> iterable) {
        double n = 0.0;
        int n2 = 0;
        for (Number number : iterable) {
            n += number.doubleValue();
            ++n2;
        }
        final double n3 = n / n2;
        double n4 = 0.0;
        for (Number number : iterable) {
            n4 += FastMath.pow(number.doubleValue() - n3, 2.0);
        }
        return (n4 == 0.0) ? 0.0 : (n4 / (n2 - 1));
    }

    public static double sqrt(double number) {
        return FastMath.sqrt(number);
    }

    public static double horizontalDistance(Vector vector1, Vector vector2) {
        return Math.sqrt(NumberConversions.square(vector1.getX() - vector2.getX()) +
                NumberConversions.square(vector1.getZ() - vector2.getZ()));
    }

    public static double verticalDistance(Vector vector1, Vector vector2) {
        return Math.sqrt(NumberConversions.square(vector1.getY() - vector2.getY()));
    }

    public static float f(List<Float> list) {
        float n = 0.0f;
        Iterator<Float> iterator = list.iterator();
        while (iterator.hasNext()) {
            n += iterator.next();
            try {
                if (iterator.toString() == null) {
                    return 0.0f;
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
            break;
        }
        try {
            if (list.size() > 0) {
                return n / list.size();
            }
        } catch (IllegalArgumentException ex2) {
            ex2.printStackTrace();
        }
        return 0.0f;
    }

    public static double clamp180(double theta) {
        theta %= 360.0;

        if (theta >= 180.0) {
            theta -= 360.0;
        }

        if (theta < -180.0) {
            theta += 360.0;
        }
        return theta;
    }

    public static double getDirection(Location from, Location to) {
        if (from == null || to == null) {
            return 0.0D;
        }
        double difX = to.getX() - from.getX();
        double difZ = to.getZ() - from.getZ();

        return (float) ((FastMath.atan2(difZ, difX) * 180.0D / Math.PI) - 90.0F);
    }

    public static double getDirection(Location from, Vector vector) {
        if (from == null || vector == null) {
            return 0.0D;
        }
        double difX = vector.getX() - from.getX();
        double difZ = vector.getZ() - from.getZ();

        return (float) ((FastMath.atan2(difZ, difX) * 180.0D / Math.PI) - 90.0F);
    }

    public static double getDirection(CustomLocation location, Vector vector) {
        double difX = vector.getX() - location.getX();
        double difZ = vector.getZ() - location.getZ();

        return (float) ((FastMath.atan2(difZ, difX) * 180.0D / Math.PI) - 90.0F);
    }

    public static Vec3 getPositionEyes(double x, double y, double z, float eyeHeight) {
        return new Vec3(x, y + (double) eyeHeight, z);
    }

    public static double getBlockDistance(double eye, int eyeBlock, double dir, int blockDiff) {
        if (blockDiff == 0) {
            return 0;
        }

        double eyeOffset = Math.abs(eye - eyeBlock);
        return ((dir < 0 ? eyeOffset : 1 - eyeOffset) + (double) (Math.abs(blockDiff) - 1)) / Math.abs(dir);
    }

    public static int floor_double(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }

    /*public static Vec3 getLook(float partialTicks, KarhuPlayer karhuPlayer) {
        if (partialTicks == 1.0F) {
            return getVectorForRotation(karhuPlayer.getLocation().getPitch(), karhuPlayer.getLocation().getYaw());
        } else {
            float f = karhuPlayer.getLastLocation().getPitch() + (karhuPlayer.getLocation().getPitch() - karhuPlayer.getLastLocation().getPitch()) * partialTicks;
            float f1 = karhuPlayer.getLastLocation().getYaw() + (karhuPlayer.getLocation().getYaw() - karhuPlayer.getLastLocation().getYaw()) * partialTicks;
            return getVectorForRotation(f, f1);
        }
    }*/

    public static Vec3 getVectorForRotation(float pitch, float yaw, KarhuPlayer data) {
        if (!data.isNewerThan12()) {
            float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
            float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
            float f2 = -MathHelper.cos(-pitch * 0.017453292F);
            float f3 = MathHelper.sin(-pitch * 0.017453292F);
            return new Vec3(f1 * f2, f3, f * f2);
        } else {
            float f = pitch * ((float) Math.PI / 180F);
            float f1 = -yaw * ((float) Math.PI / 180F);
            float f2 = MathHelper.cos(f1);
            float f3 = MathHelper.sin(f1);
            float f4 = MathHelper.cos(f);
            float f5 = MathHelper.sin(f);
            return new Vec3(f3 * f4, -f5, f2 * f4);
        }
    }

    public static Vec3d getLook3d(float partialTicks, KarhuPlayer karhuPlayer) {
        if (partialTicks == 1.0F) {
            return getVectorForRotation3d(karhuPlayer.getLocation().getPitch(), karhuPlayer.getLocation().getYaw());
        } else {
            float f = karhuPlayer.getLastLocation().getPitch() + (karhuPlayer.getLocation().getPitch() - karhuPlayer.getLastLocation().getPitch()) * partialTicks;
            float f1 = karhuPlayer.getLastLocation().getYaw() + (karhuPlayer.getLocation().getYaw() - karhuPlayer.getLastLocation().getYaw()) * partialTicks;
            return getVectorForRotation3d(f, f1);
        }
    }

    public static Vec3d getVectorForRotation3d(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d(f1 * f2, f3, f * f2);
    }

    public static AxisAlignedBB getEntityBoundingBox(Location l) {
        return getEntityBoundingBox(l.getX(), l.getY(), l.getZ());
    }

    public static AxisAlignedBB getEntityBoundingBox(double x, double y, double z) {
        float f = 0.6F / 2.0F;
        float f1 = 1.8F;
        return (new AxisAlignedBB(x - (double) f, y, z - (double) f, x + (double) f, y + (double) f1, z + (double) f));
    }

    public static double getGcd(double current, double previous) {
        double temp;

        if (previous > current) {
            temp = current;
            current = previous;
            previous = temp;
        }

        while (previous > 0.001) {
            temp = current % previous;
            current = previous;
            previous = temp;
        }

        return current;
    }

    public static double getGcd2(final double a, final double b) {
        if (a < b) {
            return getGcd2(b, a);
        }

        if (Math.abs(b) < 0.001) {
            return a;
        } else {
            return getGcd2(b, a - Math.floor(a / b) * b);
        }
    }

    public static float getGcd(float current, float previous) {
        float temp;

        if (previous > current) {
            temp = current;
            current = previous;
            previous = temp;
        }

        while (previous > 0.001f) {
            temp = current % previous;
            current = previous;
            previous = temp;
        }

        return current;
    }


    public static double calculateGcd(double a, double b) {
        if (a == 0) {
            return b;
        }

        if(a > Float.MAX_VALUE || b > Float.MAX_VALUE
                || a < Float.MIN_VALUE || b < Float.MIN_VALUE) {
            return 0;
        }

        int quotient = calculateWholeQuotient(b, a);
        double remainder = ((b / a) - quotient) * a;

        if (Math.abs(remainder) < Math.max(a, b) * 0.001) {
            remainder = 0;
        }

        return calculateGcd(remainder, a);
    }

    public static int calculateWholeQuotient(double dividend, double divisor) {
        double result = dividend / divisor;
        double remainder = Math.max(dividend, divisor) * 0.001;
        return (int) (result + remainder);
    }

    public static double gcd(double a, double b) {
        if (a == 0) {
            return b;
        }

        if(Math.abs(a) >= Float.MAX_VALUE || Math.abs(b) >= Float.MAX_VALUE) {
            return 0;
        }

        if (a < b)
            return gcd(b, a);
        else if (Math.abs(b) < 0.001) // base case
            return a;
        else
            return gcd(b, a - MathHelper.floor(a / b) * b);
    }

    public static float gcdFloat(float a, float b) {
        if (a == 0) {
            return b;
        }

        if(Math.abs(a) >= Float.MAX_VALUE || Math.abs(b) >= Float.MAX_VALUE) {
            return 0;
        }

        if (a < b)
            return gcdFloat(b, a);
        else if (Math.abs(b) < 0.001f) // base case
            return a;
        else
            return gcdFloat(b, (a - MathHelper.floor_float(a / b) * b));
    }

    public static double gcdTest(double a, double b) {
        if (a == 0) {
            return b;
        }

        if(Math.abs(a) >= Float.MAX_VALUE || Math.abs(b) >= Float.MAX_VALUE) {
            return 0;
        }

        if (a < b)
            return gcdTest(b, a);
        else if (Math.abs(b) < 0.001f) // base case
            return a;
        else
            return gcdTest(b, (a - (a / b) * b));
    }

    public static float gcdTestFloat(float a, float b) {
        if (a == 0) {
            return b;
        }

        if(a >= Float.MAX_VALUE || b >= Float.MAX_VALUE
                || a <= Float.MIN_VALUE || b <= Float.MIN_VALUE) {
            return 0;
        }

        if (a < b)
            return gcdTestFloat(b, a);
        else if (Math.abs(b) < 0.001f) // base case
            return a;
        else
            return gcdTestFloat(b, (a - (a / b) * b));
    }


    public static float absFloat(float a) {
        return (a <= 0.0F) ? 0.0F - a : a;
    }

    public static double absDouble(double a) {
        return (a <= 0.0D) ? 0.0D - a : a;
    }

    public static double trim(int degree, double d) {
        String format = "#.#";

        for(int i = 1; i < degree; ++i) {
            format = format + "#";
        }

        DecimalFormat twoDForm = new DecimalFormat(format);
        return Double.parseDouble(twoDForm.format(d).replaceAll(",", "."));
    }

    public static float trimFloat(int degree, float d) {
        StringBuilder format = new StringBuilder("#.#");

        for(int i = 1; i < degree; ++i) {
            format.append("#");
        }

        DecimalFormat twoDForm = new DecimalFormat(format.toString());
        return Float.parseFloat(twoDForm.format(d).replaceAll(",", "."));
    }

    public static String parseVersion(ClientVersion ver) {
        return ver.toString().replaceAll("_", ".").replaceAll("v.", "");
    }

    public static <T extends Number> T getMode(Collection<T> collect) {
        Map<T, Integer> repeated = new HashMap<>();

        //Sorting each value by how to repeat into a map.
        for(T c : collect) {
            int number = repeated.getOrDefault(c, 0);

            repeated.put(c, number + 1);
        }

        /*collect.forEach(val -> {
            int number = repeated.getOrDefault(val, 0);

            repeated.put(val, number + 1);
        });*/

        //Calculating the largest value to the key, which would be the mode.
        return repeated.keySet().stream()
                .map(key -> new Tuple<>(key, repeated.get(key))) //We map it into a Tuple for easier sorting.
                .max(Comparator.comparing(Tuple::b, Comparator.naturalOrder()))
                .orElseThrow(NullPointerException::new).a();
    }

    private static long getDelta(long alpha, long beta) {
        return alpha % beta;
    }

    public static float[] getRotationFromPosition(CustomLocation playerLocation, CustomLocation targetLocation) {
        double xDiff = targetLocation.getX() - playerLocation.getX();
        double zDiff = targetLocation.getZ() - playerLocation.getZ();
        double yDiff = targetLocation.getY() - (playerLocation.getY() + 0.12);
        double dist = sqrt(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) (FastMath.atan2(zDiff, xDiff) * 180.0 / 3.141592653589793) - 90.0f;
        float pitch = (float) (-(FastMath.atan2(yDiff, dist) * 180.0 / 3.141592653589793));
        return new float[]{yaw, pitch};
    }

    public static float getRotationYaw(double mx, double mz, float yaw) {
        float yaw2 = (float) (Math.atan2(mz, mx) * 180.0D / Math.PI) - 90.0F;
        yaw2 -= yaw;
        while (yaw2 > 360.0F)
            yaw2 -= 360.0F;
        while (yaw2 < 0.0F)
            yaw2 += 360.0F;
        return yaw2;
    }

    public static double pingFormula(long ping) {
        return Math.ceil((ping + 5) / 50.0D);
    }

    public static double invSqrt(double x) {
        double xhalf = 0.5d * x;
        long i = Double.doubleToLongBits(x);
        i = 0x5fe6ec85e7de30daL - (i >> 1);
        x = Double.longBitsToDouble(i);
        x *= (1.5d - xhalf * x * x);
        return x;
    }

    public static double hypotNEW(double... value) {
        double total = 0.0D;
        double[] var3 = value;
        int var4 = value.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            double val = var3[var5];
            total += val * val;
        }

        return FastMath.sqrt(total);
    }

    public static double hypot(double... value) {
        double total = 0;

        for (double val : value) {
            total += (val * val);
        }

        return FastMath.sqrt(total);
    }

    public static float hypot(float... value) {
        float total = 0;

        for (float val : value) {
            total += (val * val);
        }

        return (float) FastMath.sqrt(total);
    }

    public static float round(float value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double roundDown(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }

    public static float roundFloat(float value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public static float round(float value, int places, RoundingMode mode) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, mode);
        return bd.floatValue();
    }

    public static float round(float value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(0, RoundingMode.UP);
        return bd.floatValue();
    }


    public static float getDistanceBetweenAngles(float angle1, float angle2) {
        float distance = Math.abs(angle1 - angle2) % 360.0f;
        if (distance > 180.0f) {
            distance = 360.0f - distance;
        }
        return distance;
    }

    public static float trimAngle(float angle) {
        float normalized = Math.abs(angle) % 360.0F;
        return angle < 0 ? 360.0F - normalized : normalized;
    }

    public static float getAngleDiff(float a, float b) {
        float diff = Math.abs(a - b);
        float altDiff = b + 360 - a;
        float altAltDiff = a + 360 - b;
        if(altDiff < diff) diff = altDiff;
        if(altAltDiff < diff) diff = altAltDiff;
        return diff;
    }


    public static double getAngleDistance(double alpha, double beta) {
        double abs = Math.abs(alpha % 360.0D - beta % 360.0D);
        return Math.abs(Math.min(360.0D - abs, abs));
    }

    public static float getAngleDistance(float alpha, float beta) {
        float abs = Math.abs(alpha % 360.0F - beta % 360.0F);
        return Math.abs(Math.min(360.0F - abs, abs));
    }

    // Get delta of a double list
    public static List<Double> calculateDelta(List<Double> doubleList) {
        if (doubleList.size() <= 1)
            throw new IllegalArgumentException("The list must contain 2 or more elements in order to calculate delta");

        List<Double> out = new ArrayList<>();
        for (int i = 1; i <= doubleList.size() - 1; i++)
            out.add(doubleList.get(i) - doubleList.get(i - 1));
        return out;
    }

    // Convert a float list to a double list
    public static List<Double> toDoubleList(List<Float> floatList) {
        return floatList.stream().map(e -> (double) e).collect(Collectors.toList());
    }

    // Get mean average of a double sequence
    public static double mean(List<Double> angles) {
        return angles.stream().mapToDouble(e -> e).sum() / angles.size();
    }

    // Get mean average of a double sequence
    public static double mean(Deque<Float> angles) {
        return angles.stream().mapToDouble(e -> e).sum() / angles.size();
    }

    public static double mean2(Deque<Double> angles) {
        return angles.stream().mapToDouble(e -> e).sum() / angles.size();
    }

    // Get standard deviation of a double sequence
    public static double stddev(List<Double> angles) {
        double mean = mean(angles);
        double output = 0;
        for (double angle : angles)
            output += FastMath.pow(angle - mean, 2);
        return output / angles.size();
    }

    // Get euclidean distance of two vector
    public static double euclideanDistance(double[] vectorA, double[] vectorB) {
        validateDimension("Two vectors need to have exact the same dimension", vectorA, vectorB);

        double dist = 0;
        for (int i = 0; i <= vectorA.length - 1; i++)
            dist += FastMath.pow(vectorA[i] - vectorB[i], 2);
        return FastMath.sqrt(dist);
    }

    // Convert a double array to a double list
    public static List<Double> toList(double[] doubleArray) {
        return Arrays.asList(ArrayUtils.toObject(doubleArray));
    }

    // Convert a double list to a double array
    public static double[] toArray(List<Double> doubleList) {
        return doubleList.stream().mapToDouble(e -> e).toArray();
    }

    // generate a double array filled with random values from 0 to 1
    public static double[] randomArray(int length) {
        double[] randomArray = new double[length];
        applyFunc(randomArray, e -> e = ThreadLocalRandom.current().nextDouble());
        return randomArray;
    }

    // apply function on a array
    public static void applyFunc(double[] doubleArray, Function<Double, Double> func) {
        for (int i = 0; i <= doubleArray.length - 1; i++)
            doubleArray[i] = func.apply(doubleArray[i]);
    }

    // add two vector together
    public static double[] add(double[] vectorA, double[] vectorB) {
        validateDimension("Two vectors need to have exact the same dimension", vectorA, vectorB);

        double[] output = new double[vectorA.length];
        for (int i = 0; i <= vectorA.length - 1; i++)
            output[i] = vectorA[i] + vectorB[i];
        return output;
    }

    // Get diff of two different vectors (subtract)
    public static double[] subtract(double[] vectorA, double[] vectorB) {
        validateDimension("Two vectors need to have exact the same dimension", vectorA, vectorB);

        return add(vectorA, opposite(vectorB));
    }

    // get opposite numbers of elements in the vector
    public static double[] opposite(double[] vector) {
        return multiply(vector, -1);
    }

    // multiply all elements in the vector with a value
    public static double[] multiply(double[] vector, double factor) {
        double[] output = vector.clone();
        applyFunc(output, e -> e * factor);
        return output;
    }

    // normalize a value with feature scaling according to the given min and max
    public static double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }

    // round a value with given arguments
    public static double round(double value, int precision, RoundingMode mode) {
        return BigDecimal.valueOf(value).round(new MathContext(precision, mode)).doubleValue();
    }

    public static double roundBD(double value, int places, RoundingMode mode) {
        if (places < 0) {
            throw new IllegalArgumentException();
        } else {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, mode);
            return bd.doubleValue();
        }
    }

    public static double round(double value) {
        return value - value % 1000;
    }

    @SuppressWarnings("SameParameterValue")
    private static void validateDimension(String message, double[]... vectors) {
        for (int i = 0; i <= vectors.length - 1; i++)
            if (vectors[0].length != vectors[i].length)
                throw new IllegalArgumentException(message);
    }

    public static float wrapAngleTo180_float(float value) {
        value = value % 360.0F;

        if (value >= 180.0F) {
            value -= 360.0F;
        }

        if (value < -180.0F) {
            value += 360.0F;
        }

        return value;
    }

    public static double wrapAngleTo180_double(double value) {
        value %= 360D;

        if (value >= 180D)
            value -= 360D;

        if (value < -180D)
            value += 360D;

        return value;
    }


    public static double positiveSmaller(Number n, Number n2) {
        return Math.abs(n.doubleValue()) < Math.abs(n2.doubleValue()) ? n.doubleValue() : n2.doubleValue();
    }

    public static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public static final Random RANDOM = new Random();

    public static double getDistanceToGround(Player p) {
        Location loc = p.getLocation().clone();
        double y = loc.getBlockY();
        double distance = 0.0;
        for (double i = y; i >= 0.0; --i) {
            loc.setY(i);
            if (loc.getBlock().getType().isSolid()) {
                break;
            }
            ++distance;
        }
        return distance;
    }

    public static boolean isNegative(short number) {
        return number < 0;
    }

    public static boolean isNegativeDouble(double number) {
        return number < 0;
    }

    public static boolean isNearlySame(double d1, double d2, double number) {
        return Math.abs(d1 - d2) < number;
    }

    public static double delta(double d1, double d2) {
        return Math.abs(d1 - d2);
    }

    public static Vector getDirection(float yaw, float pitch) {
        Vector vector = new Vector();
        float radiansYaw = (float)Math.toRadians(yaw);
        float radiansPitch = (float)Math.toRadians(pitch);
        vector.setY(-MathHelper.sin(radiansPitch));
        double xz = MathHelper.cos(radiansPitch);
        vector.setX(-xz * MathHelper.sin(radiansYaw));
        vector.setZ(xz * MathHelper.cos(radiansYaw));
        return vector;
    }

    /**
     * Returns the angle between two non-zero, finite vectors without ever returning
     * NaN, unlike Bukkit's Vector#angle(Vector)
     */
    public static double angle(Vector a, Vector b) {
        double dot = Math.min(Math.max(a.dot(b) / (a.length() * b.length()), -1), 1);
        return Math.acos(dot);
    }

    public static String booleanToString(boolean b) {
        if(b) return "true";
        return "false";
    }


    public static Block getTargetedBlock(Player player, int range) {
        BlockIterator bi;

        bi = new BlockIterator(player, range);
        if(!BlockUtil.chunkLoaded(player.getWorld(), bi.next().getX(), bi.next().getZ())) {
            return null;
        }

        Block lastBlock = null;
        while (bi.hasNext()) {
            lastBlock = bi.next();
            if (lastBlock.getType() == Material.AIR)
                continue;
            break;
        }
        return lastBlock;

    }

    public static <K, V extends Comparable<? super V>> HashMap<K, V> sortByValue(HashMap<K, V> map) {

        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Collections.reverse(list);

        HashMap<K, V> result = new LinkedHashMap<>();
        for (HashMap.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static float clampFloat(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public static boolean getIntAsBoolean(int i) {
        switch (i) {
            case 0:
                return false;
            case 1:
                return true;
            default:
                return true;
        }
    }
    public static long toMillis(long time) {
        return TimeUnit.NANOSECONDS.toMillis(time);
    }

    public static long toNanos(long time) {
        return TimeUnit.MILLISECONDS.toNanos(time);
    }

}

