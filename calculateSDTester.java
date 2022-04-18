import java.util.*;
import java.lang.Math;

public class calculateSDTester {
    public static void main(String[] args) {
        ArrayList<Double> sendIntervals = new ArrayList<Double>();
        sendIntervals.add(4.0);
        sendIntervals.add(9.0);
        sendIntervals.add(11.0);
        sendIntervals.add(12.0);
        sendIntervals.add(17.0);
        sendIntervals.add(5.0);
        sendIntervals.add(8.0);
        sendIntervals.add(12.0);
        sendIntervals.add(14.0);

        System.out.println(calculateSD(sendIntervals));
    }

    public static double calculateSD(ArrayList<Double> sendIntervals) {
        Double sum = 0.0, standardDeviation = 0.0;
        int length = sendIntervals.size();

        for (Double num : sendIntervals) {
            sum += num;
        }

        Double mean = sum / length;

        for (Double num : sendIntervals) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
    }
}