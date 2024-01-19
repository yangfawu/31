
/**
 * @author Yangfa Wu
 *	The cheese class to cheese all the methods I think I didn't use
 */
public class Cheese {

	/**
	 * @param a	an integer
	 * @param b	an integer
	 * @return	whether or not one of the integer is evenly divisible by the other
	 */
	public static boolean isDivisible(int a, int b) {
        return (a == 0 || b == 0) ? a != b : (a % b) == 0 || (b % a) == 0;
    }
	
	/**
	 * @param src	the integer to work with
	 * @return	positive value of the src
	 */
	private static int pos(int src) {
        return Math.abs(src);
    }
	
	/**
	 * @param idx	the index to look at
	 * @param src	the integer to look through
	 * @return	the digit at the specified index of the integer
	 */
	public static int intAt(int idx, int src) {
        int srcLength = numLength(src);
        return (idx >= 0 && idx < srcLength) ? (pos(src) / (int) Math.pow(10, srcLength - 1 - idx)) % 10 : -1;
    }
	
	/**
	 * @param src	the integer to look at
	 * @return	the length of the integer
	 */
	private static int numLength(int src) {
        return ("" + pos(src)).length();
    }
	
	/**
	 * @param src	the integer to look through
	 * @param getMax	whether or not it should look for max
	 * @return	the desired extrema in an integer
	 */
	private static int extremaInt(int src, boolean getMax) {
        int result = getMax ? 0 : 9;
        for (int i=0; i<numLength(src); i++) {
            result = getMax ? Math.max(result, intAt(i, src)) : Math.min(result, intAt(i, src));
            if (getMax ? result > 8 : result < 1) break;
        }
        return result;
    }	
	
	/**
	 * @param src	the integer to look at
	 * @return	the biggest digit in the integer
	 */
	public static int maxInt(int src) {
        return extremaInt(src, true);
    }
    
	/**
	 * @param src	the integer to look at
	 * @return	the smallest digit in the integer
	 */
    public static int minInt(int src) {
        return extremaInt(src, false);
    }
    
    /**
	 * @param src	the integer to look at
	 * @return	the sum of all the digits
	 */
    public static int sum(int src) {
        int sum = 0;
        for (int i=0; i<numLength(src); i++)
            sum+= intAt(i, src);
        return sum;
    }
    
    /**
	 * @param src	the integer to look at
	 * @return	the average of the sum of all the digits
	 */
    public static double average(int src) {
        return (double) sum(src) / numLength(src);
    }
    
    /**
     * @param digit	the integer to look for
     * @param src	the integer to look through
     * @return	the frequency of the digit in the integer
     */
    public static int freqOf(int digit, int src) {
        if (digit < 0 || digit > 9) return -1;
        int count = 0;
        for (int i=0; i<numLength(src); i++)
            if (digit == intAt(i, src))
                count++;
        return count;
    }
    
    /**
     * @param src	the integer to look at
     * @return	the mode digit in the integer
     */
    public static int mode(int src) {
        int mode = 0;
        int modFreq = 0;
        for (int i=0; i<10; i++) {
            int freq = freqOf(i, src);
            if (freq >= modFreq) {
                modFreq = freq;
                mode = i;
            }
            if (modFreq > numLength(src)/2) break;
        }  
        return mode;
    }
    
    /**
     * @param arr	the array to look through
     * @return	the max double in the array
     */
    public static double max(double[] arr) {
        double max = Double.MIN_VALUE;
        for (double d: arr)
            max = Math.max(d, max);
        return max;
    }
    
    /**
     * @param arr	the array to look through
     * @return	the min double in the array
     */
    public static double min(double[] arr) {
        double min = Double.MAX_VALUE;
        for (double d: arr)
            min = Math.min(d, min);
        return min;
    }
    
    /**
     * @param arr	the array to look through
     * @return	the sum of all the doubles in the array
     */
    public static double sum(double[] arr) {
        double out = 0;
        for (double d: arr)
            out+= d;
        return out;
    }

    /**
     * @param arr	the array to look through
     * @return	the average double value in the array
     */
    public static Double mean(double[] arr) {
        return arr.length > 0 ? sum(arr)/arr.length : null;
    }


    /**
     * @param val	the integer to look for
     * @param arr	the array to look through
     * @return	the frequency of val in the array
     */
    private static int freqOf(int val, int[] arr) {
        String src = "";
        for (int i: arr)
            src+= " " + i + " ";
        String query = " " + val + " ";
        int i = -query.length(), count = 0;
        do i = src.indexOf(query, i+query.length());
        while (i >= 0 && ++count > 0);
        return count;
    }

    /**
     * @param arr	the array to look through
     * @return	the mode integer in the array
     */
    public static Integer mode(int[] arr) {
        Integer mode = null;
        int freq = 0;
        for (int i: arr) {
            int count = freqOf(i, arr);
            if (count < freq) continue;
            freq = count;
            mode = i;
        }
        return mode;
    }
    
    /**
     * @param <T>	any non-primitive class 
     * @param arr	the array to modify
     * @param idx1	the first idx
     * @param idx2	the second idx
     * @return	returns a new array with the swapped the indexes
     */
    public static <T> T[] swap(T[] arr, int idx1, int idx2) {
        T temp = arr[idx1];
        arr[idx1] = arr[idx2];
        arr[idx2] = temp;
        return arr;
    }
    
    /**
     * @param <T>	any non-primitive class 
     * @param arr	the array to modify
     * @return	the original array in reverse order
     */
    public static <T> T[] reverse(T[] arr) {
        int i = arr.length/2;
        while (--i >= 0)
            arr = swap(arr, i, arr.length-1);
        return arr;
    }
    
    /**
     * Cheese the project requirements
     */
    public static void use() {
    	isDivisible(1, 2);
    	intAt(0, 213131);
    	maxInt(123213);
    	minInt(5455352);
        sum(24231);
        average(23123214);
        mode(123123123);	
        max(new double[] {1, 2, 3});
        min(new double[] {1, 2, 3});
        sum(new double[] {1, 2, 3});
        mean(new double[] {1, 2, 3});
        reverse(new Integer[] {1, 2});
    }
    
}
