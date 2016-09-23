package it.polito.softeng.sort;

/**
 * Bubble sort algorithm implementation
 */
public class Bubblesort extends IntSortAlgorithm {

    @Override
    public void sort(int[] a) {
        int j;
        boolean flag = true;
        int temp;

        while (flag) {
            flag = false;
            for (j = 0; j < a.length - 1; j++) {
                if (a[j] > a[j + 1]) {

                    temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                    flag = true;
                }
            }
        }
    }

}
