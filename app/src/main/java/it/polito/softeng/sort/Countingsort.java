package it.polito.softeng.sort;

/**
 * Counting sort algorithm implementation
 */
public class Countingsort extends IntSortAlgorithm {

    @Override
    public void sort(int[] a) {
        int M = -1;
        for (int i = 0; i < a.length; ++i) {
            if (a[i] > M) M = a[i];
        }
        int c[] = new int[M + 1];
        for (int i = 0; i < a.length; i++)
            c[a[i]]++;

        for (int i = 1; i < c.length; i++)
            c[i] += c[i - 1];
        int b[] = new int[a.length];
        for (int i = a.length - 1; i >= 0; i--)
            b[--c[a[i]]] = a[i];

        System.arraycopy(b, 0, a, 0, a.length);
//        int current=0;
//        for(int i=0; i<c.length; ++i){
//        	Arrays.fill(a, current, current+c[i], i);
//        	current += c[i];
//        }
    }

}
