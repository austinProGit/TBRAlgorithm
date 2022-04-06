import java.util.*;
import java.lang.Math;
import java.io.*;
import java.math.BigInteger;

public class TBRAlgorithm {

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
            String fileName = "4conn_dataset0.txt";
            FileReader fileReader = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fileReader);
            String fileNameSend = "S-E-index-" + fileName;
            FileWriter fw1 = new FileWriter(fileNameSend);

            fw1.write("Type\t\tCount\t\tTime\t\t\tTimeInt\t\tBigData\t\tPrevSendWasCMD\t\tCumTimeInt\n");

            while ((thisLine = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(thisLine, " ");

                if (thisLine.contains(".22: Flags [P.]")) {// checks if the packet is a Send
                    packetType = "Send";
                    currentSendTime = convertToMicrosec(thisLine.substring(0, 15));// get the currentSendTime and
                                                                                   // convert to microSeconds
                    if ((previousSendTime != null) && !previousSendWasCMD) { // If there was a previous Send packet and
                                                                             // the
                                                                             // previous Send packet was not a CMD
                        sendInterval = currentSendTime.subtract(previousSendTime); // Set the sendInterval to the
                                                                                   // difference between the current and
                                                                                   // previous Send times
                        sendIntervals.add(sendInterval.doubleValue());
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
                } else if (thisLine.contains(".22 >") && thisLine.contains("Flags [P.]")) {
                    packetType = "Echo";
                    while (st.hasMoreTokens()) {
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

            fw1.write("Standard deviation: " + String.valueOf(calculateSD(sendIntervals)));

            br.close();
            fw1.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static BigInteger convertToMicrosec(String str) {
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