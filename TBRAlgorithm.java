import java.util.*;
import java.lang.Math;
import java.io.*;
import java.math.BigInteger;

public class TBRAlgorithm {

    /*
     * @author Austin Lee and Dr. Lixin Wang
     * 
     * @version 1.0
     * 
     * @since 15 APR 2022
     * 
     * @description This program takes an input of a raw data capture text file,
     * processes the data, and outputs a new text file.
     * The purpose of this program is to evaluate a tcpDump pcap and analyze the
     * packets to determine the standard deviation of the keystroke time gaps.
     * keystroke time gaps are defined as the time gaps between successive
     * keystrokes, excluding the time gaps initiated by the return key.
     * 
     * @param indexSend Int counter used to index the number of Send packets.
     * 
     * @param bigData Boolean used to notate whether a given Echo packet contains
     * a payload over the minimum size (and therefore indicates that the previous
     * Send packet was a Command Send packet).
     * 
     * @param previousSendWasCMD Boolean used to track whether the previous Send
     * packet in the pcap was a Command Send packet or not.
     * 
     * @param isFirstEcho Boolean used to track whether the current Echo packet
     * being analyzed is the first Echo packet following a Send packet or not.
     * 
     * @param cumulativeTimeInt Accumulator used to visually verify that the
     * program is correctly accumulating time intervals.
     * 
     * @param previousSendTime BigInteger used to store the time stamp of the
     * previous Send packet.
     * 
     * @param currentSendTime BigInteger used to store the time stamp of the
     * current Send packet.
     * 
     * @param sendInterval BigInteger used to store the time gap between a
     * keystroke's cooresponding Send packet time stamp and the subsequent
     * keystroke's cooresponding Send packet time stamp.
     * 
     * @param echoPayload String used to display the current Echo packet's
     * payload. We assume that a payload over 44 incidcates that the previous
     * Send packet was a Command Send packet.
     * 
     * @param packetType String used to display packet type (Send or Echo)
     * 
     */

    public static void main(String[] args) throws Exception {

        String thisLine = null;

        int indexSend = 1;
        String indexEcho = "N/A";

        boolean bigData = false;
        String bigDataSend = "N/A";

        boolean previousSendWasCMD = false;
        String previousSendCMDEcho = "N/A";

        boolean isFirstEcho = true;

        int cumulativeTimeInt = 0;
        BigInteger previousSendTime = null;
        BigInteger currentSendTime = null;
        String currentEchoTime = "N/A";
        BigInteger sendInterval = null;
        String sendIntervalEcho = "N/A";
        String echoPayload = "";
        String packetType = "";
        ArrayList<Double> sendIntervals = new ArrayList<>();

        try {
            String fileName = "1conn_dataset_test.txt";
            FileReader fileReader = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fileReader);
            String fileNameSend = "S-E-index-" + fileName;
            FileWriter fw1 = new FileWriter(fileNameSend);

            fw1.write("Type\t\tCount\t\tTime\t\t\tTimeInt\t\tBigData\t\tPrevSendWasCMD\t\tCumTimeInt\n");

            while ((thisLine = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(thisLine, " ");

                if (thisLine.contains(".22: Flags [P.]")) {// checks if the packet is a Send
                    packetType = "Send";
                    currentSendTime = convertToMicrosec(thisLine.substring(0, 17));// get the currentSendTime and
                                                                                   // convert to microSeconds
                    if ((previousSendTime != null) && !previousSendWasCMD) { // If there was a previous Send packet and
                                                                             // the
                                                                             // previous Send packet was not a CMD
                        sendInterval = currentSendTime.subtract(previousSendTime); // Set the sendInterval to the
                                                                                   // difference between the current and
                                                                                   // previous Send times
                        sendIntervals.add(sendInterval.doubleValue());// Add the interval to the list of counted send
                                                                      // intervals.
                        cumulativeTimeInt += sendInterval.intValue(); // Add the sendInterval to the cumulativeTimeInt
                        // intervalCounter++;
                    }
                    fw1.write(packetType + "\t\t" + indexSend++ + "\t\t"
                            + currentSendTime + "\t"
                            + sendInterval + "\t\t" + bigDataSend + "\t\t\t" + previousSendWasCMD
                            + "\t\t\t\t" + cumulativeTimeInt + "\n");
                    previousSendTime = currentSendTime;
                    previousSendWasCMD = false;
                    isFirstEcho = true;
                } else if (thisLine.contains(".22 >") && thisLine.contains("Flags [P.]")) {// Check if the packet is an
                    // Echo packet. We exclude all ack packets.
                    packetType = "Echo";
                    while (st.hasMoreTokens()) { // Get the Echo packet's payload from the text file.
                        echoPayload = st.nextToken();
                    }
                    int echoPayloadInt = Integer.parseInt(echoPayload);
                    if (isFirstEcho) {
                        if (echoPayloadInt > 44) {
                            bigData = true;
                            previousSendWasCMD = true;
                        } else {
                            previousSendWasCMD = false;
                            bigData = false;
                        }
                        isFirstEcho = false;
                    } else {
                        if (echoPayloadInt > 44) {
                            bigData = true;
                            previousSendWasCMD = true;
                        } else {
                            bigData = false;
                        }
                    }
                    fw1.write(packetType + "\t\t" + indexEcho + "\t\t"
                            + currentEchoTime + "\t\t\t\t\t"
                            + sendIntervalEcho + "\t\t\t" + bigData + "\t\t" + previousSendCMDEcho
                            + "\t\t\t\t\t" + cumulativeTimeInt + "\n");
                    fw1.write("echoPayload: " + echoPayload + "\n");
                }
            }

            sendIntervals = removeOutliers(sendIntervals);

            fw1.write("Standard deviation: " + String.valueOf(calculateSD(sendIntervals)));

            br.close();
            fw1.close();
            // for (int i = 0; i < sendIntervals.size(); i++) {
            // System.out.println(sendIntervals.get(i));
            // }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Double> removeOutliers(ArrayList<Double> input) {
        ArrayList<Double> output = new ArrayList<Double>();
        List<Double> data1 = new ArrayList<Double>();
        List<Double> data2 = new ArrayList<Double>();
        Collections.sort(input);
        if (input.size() % 2 == 0) {
            data1 = input.subList(0, input.size() / 2);
            data2 = input.subList(input.size() / 2, input.size());
        } else {
            data1 = input.subList(0, input.size() / 2);
            data2 = input.subList(input.size() / 2 + 1, input.size());
        }
        double q1 = getMedian(data1);
        System.out.println("q1: " + q1);
        double q3 = getMedian(data2);
        System.out.println("q3: " + q3);
        double iqr = q3 - q1;
        double lowerFence = q1 - 1.5 * iqr;
        System.out.println("lowerFence: " + lowerFence);
        double upperFence = q3 + 1.5 * iqr;
        System.out.println("upperFence: " + upperFence);
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i) >= lowerFence && input.get(i) <= upperFence) {
                output.add(input.get(i));
            } else {
                System.out.println("Removing outlier value: " + input.get(i));
            }
        }
        return output;
    }

    private static double getMedian(List<Double> data) {
        if (data.size() % 2 == 0)
            return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
        else
            return data.get(data.size() / 2);
    }

    /*
     * @author Originally sourced and modified from:
     * https://www.programiz.com/java-programming/examples/standard-deviation
     * 
     * @param sendIntervals ArrayList<Double> that is used to store the desired Send
     * intervals for the standard deviation calculation.
     * 
     * @return double the standard deviation of all send intervals stored in the
     * sendIntervals array list.
     */

    public static double calculateSD(ArrayList<Double> sendIntervals) {
        Double sum = 0.0, standardDeviation = 0.0;
        int length = sendIntervals.size();
        System.out.println("length of sendIntervals: " + length);

        for (Double num : sendIntervals) {
            sum += num;
        }

        Double mean = sum / length;

        for (Double num : sendIntervals) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
    }

    /*
     * @param str The string timestamp from the packet that we plan to convert to
     * type BigInteger
     * 
     * @return BigInteger The converted packet timestamp.
     */
    public static BigInteger convertToMicrosec(String str) {
        System.out.println(str);
        BigInteger result = BigInteger.ZERO;

        StringTokenizer st = new StringTokenizer(str, ".");
        if (st.hasMoreTokens()) {
            String currString = st.nextToken();
            int sec = Integer.parseInt(currString);
            result = BigInteger.valueOf(sec);
            result = result.multiply(BigInteger.valueOf(1000000));
        }

        if (st.hasMoreTokens()) {
            String currString = st.nextToken();
            int microsec = Integer.parseInt(currString);
            result = result.add(BigInteger.valueOf(microsec));
        }

        return result;
    }
}