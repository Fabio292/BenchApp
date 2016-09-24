package it.polito.softeng.sort;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * This is the main class to run power trace experiments with sorting algorithms with integers.
 * <p/>
 * An experiment consists in repeating a given number of times (e.g. 30) the sorting of an array.
 * To be able to identify the work interval in the power trace, a marker is inserted before
 * and after each run. A marker is a power pulse.
 * <p/>
 * The pulse consists of three phases
 * S(Sleep) B(Busy) S(Sleep) that are intended to precede
 * the execution of the sorting algorithm
 * <pre>
 *    _B__
 *    |  |   _ Run__
 *    |  |   |     |
 * _S_|  |_S_|     |_...
 * </pre>
 * <p/>
 * An experiment is defined by three main parameters
 * <ul>
 * <li>the sorting algorithm (typically chosen among the predefined ones)
 * <li>the size of the array to be sorted
 * <li>the sorting of the array, one of the predefined values {@link SortingType}
 * </ul>
 * Two additional parameters can be defined for the experiment:
 * <ul>
 * <li>the marker pulse width ({@code markerLength}, default: 500 ms)
 * <li>the number of repetitions of the sorting procedure ({@code nRuns}, default: 30 times)
 * </ul>
 * <p/>
 * The usage requires instantiating an experiment object and running it.
 * Before running the experiment, one of the additional parameters can be set.
 * <pre>
 * PowerSortExperiment pse = new PowerSortExperiment("Quick Sort",
 * 													 50000,
 * 													 PowerSortExperiment.SortingType.RANDOM1);
 * pse.setMarkerLength(250);
 * pse.runExperiment();
 * </pre>
 *
 * @author mtk
 */
public class PowerSortExperiment {
    private static Map<String, IntSortAlgorithm> algorithms = new HashMap<String, IntSortAlgorithm>();

    static {
        algorithms.put("bubble", new Bubblesort());
        algorithms.put("quick", new Quicksort());
        algorithms.put("merge", new Mergesort());
        algorithms.put("counting", new Countingsort());
    }

    /**
     * Used for testing
     */
    public boolean KEEP_RESULTS = false;
    /**
     * Used for testing
     */
    public int[][] results;
    private IntSortAlgorithm sorter;
    private int size;
    private SortingType type;
    private int[] a;
    private int nRuns = 30;
    private long markerLength = 500; // ms

    /**
     * Creates a new power tracing experiment for sorting.
     *
     * @param algorithmName name of the algorithm to be used, e.g. {@code "merge"} or {@code "MergeSort"} or {@code "Merge sort"}
     * @param size          the size of the {@code int} array to be sorted in the experiment
     * @param type          the initial sorting of the array, one of {@link SortingType} values
     */
    public PowerSortExperiment(String algorithmName, int size, SortingType type) {
        algorithmName = algorithmName.toLowerCase().replaceAll("\\s*sort$", "");
        if (!algorithms.containsKey(algorithmName)) {
            throw new IllegalArgumentException("Could not find any algorithm named " + algorithmName);
        }
        this.sorter = algorithms.get(algorithmName);
        this.size = size;
        this.type = type;

        a = generateArray(type, size);
    }

    /**
     * Creates a new power tracing experiment for sorting.
     *
     * @param algorithmName name of the algorithm to be used, e.g. {@code "merge"} or {@code "MergeSort"} or {@code "Merge sort"}
     * @param size          the size of the {@code int} array to be sorted in the experiment
     * @param typeNum       the initial sorting of the array, index of one of {@link SortingType} values
     */
    public PowerSortExperiment(String algorithmName, int size, int typeNum) {
        int maxType = SortingType.values().length;
        if (typeNum < 0 || typeNum >= maxType) {
            throw new IllegalArgumentException("The type must be between 0 and " + maxType);
        }
        SortingType type = SortingType.values()[typeNum];

        this.sorter = algorithms.get(algorithmName);
        this.size = size;
        this.type = type;

        a = generateArray(type, size);
    }

    /**
     * Creates a new power tracing experiment for sorting.
     *
     * @param sorter the sorting algorithm
     * @param size   the size of the {@code int} array to be sorted in the experiment
     * @param type   the initial sorting of the array, one of {@link SortingType} values
     */
    public PowerSortExperiment(IntSortAlgorithm sorter, int size, SortingType type) {
        this.sorter = sorter;
        this.size = size;
        this.type = type;

        a = generateArray(type, size);
    }

    /**
     * Retrieves the name of the available registered sorting algorithms.
     * <p/>
     * The names can be used as first argument of the constructor.
     *
     * @return string array with the names
     */
    public static String[] availableSorters() {
        return algorithms.keySet().toArray(new String[algorithms.size()]);
    }

    /**
     * Register a new sorted, with the relative name.
     * <p/>
     * This is typically not required for standard experiemtns since
     * there are already the basic predefined sorting algorithms.
     *
     * @param name   name of the algorithm
     * @param sorter the sorting algorithm object, extending {@link IntSortAlgorithm}
     */
    public static void registerSorter(String name, IntSortAlgorithm sorter) {
        algorithms.put(name, sorter);
    }

    /**
     * Generate a power trace marker, that is a single square impulse
     * with the given width.
     * <p/>
     * The pulse consists of three phases
     * S(Sleep) B(Busy) S(Sleep) that are intended to precede
     * the execution of the sorting algorithm
     * <pre>
     *    _B__
     *    |  |   _ Run__
     *    |  |   |     |
     * _S_|  |_S_|     |_...
     * </pre>
     *
     * @param markerLength width of the impulse in ms
     */
    public static void marker(long markerLength) {
        try {
            // SLEEP
            Thread.sleep(markerLength);

            // BUSY
            long temp = System.currentTimeMillis() + markerLength;
            while (temp > System.currentTimeMillis()) {
                @SuppressWarnings("unused") // this is just to keep the CPU busy...
                        int count = 0;
                for (int i = 0; i < markerLength; ++i) {
                    count += i;
                }
            }

            // SLEEP
            Thread.sleep(markerLength);
        } catch (InterruptedException e) {

        }
    }

    /**
     * This method generates predefined arrays for testing sorting algorithms.
     * <p/>
     * The size and type of algorithm can be determined.
     *
     * @param type type of array elements sorting {@link SortingType}
     * @param size length of the array to be generated
     * @return an {@code int} array with the given size and sorting
     */
    public static int[] generateArray(SortingType type, int size) {
        int[] a = new int[size];
        Random r = null;
        switch (type) {
            case RANDOM1:
                r = new Random(13);
                //break;
            case RANDOM2:
                if (r == null) r = new Random(51);
                //break;
            case RANDOM3:
                if (r == null) r = new Random(97);
                for (int i = 0; i < size; ++i) {
                    a[i] = r.nextInt(size); // or size?
                }
                break;
            case REVERSE:
                for (int i = size - 1; i >= 0; --i) {
                    a[i] = i;
                }
                break;
            case SORTED:
                for (int i = 0; i < size; ++i) {
                    a[i] = i;
                }
                break;
            default:
                break;

        }
        return a;
    }

    public String toString() {
        return "Experiment on " + sorter.getClass().getSimpleName() + ", array size: " + size
                + " sorted: " + type;
    }

    /**
     * Retrieves the number of runs forming the experiment.
     * The default is 30.
     *
     * @return number of runs to be executed
     */
    public int getNRuns() {
        return nRuns;
    }

    /**
     * Sets the number of runs forming the experiment.
     * The default is 30.
     *
     * @param nRuns the new number of runs
     */
    public void setNRuns(int nRuns) {
        this.nRuns = nRuns;
    }

    /**
     * Retrieves the length of marker pulse in milliseconds.
     * The default is 500 ms.
     *
     * @return the pulse width in ms
     */
    public long getMarkerLength() {
        return markerLength;
    }

    /**
     * Sets the length of marker pulse in milliseconds.
     * The default is 500 ms.
     *
     * @param markerLength the new marker pulse width
     */
    public void setMarkerLength(long markerLength) {
        this.markerLength = markerLength;
    }

    public long runExperiment() {
        long res = System.currentTimeMillis();

        int[][] replica = new int[nRuns][a.length];

        for (int i = 0; i < nRuns; ++i) {
            System.arraycopy(a, 0, replica[i], 0, a.length);
        }

        // start runs
        marker(markerLength);
        for (int i = 0; i < nRuns; ++i) {
            sorter.sort(replica[i]);
            //marker(markerLength);
        }

        // end runs
        marker(markerLength);

        res -= System.currentTimeMillis();

        if (KEEP_RESULTS) {
            results = replica;
        }
        return res;
    }

    /**
     * Type of array sorting: SORTED, REVERSE, RANDOM1, RANDOM2, RANDOM3
     */
    public enum SortingType {
        /**
         * Increasing sorting from 0 to size-1
         */
        SORTED,
        /**
         * Decreasing sorting from size-1 to 0
         */
        REVERSE,
        /**
         * Random numbers with seed 13
         */
        RANDOM1,
        /**
         * Random numbers with seed 51
         */
        RANDOM2,
        /**
         * Random numbers with seed 97
         */
        RANDOM3
    }


}
