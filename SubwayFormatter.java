import java.io.*;
import java.util.regex.*;

public class SubwayFormatter {
    public static void main(String[] args) {
        String inputFile = "subway.txt";
        String outputFile = "formatted_subway.txt";
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {
            String line;
            String currentLineName = null;
            String previousStation = null;

            Pattern linePattern = Pattern.compile("^([\\d一二三四五六七八九十阳]+号线).*");
            Pattern dataPattern = Pattern.compile("^(.+?)---(.+?)\\s+(\\d+(\\.\\d+)?)");

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                Matcher lineMatcher = linePattern.matcher(line);
                Matcher dataMatcher = dataPattern.matcher(line);

                if (lineMatcher.find()) {
                    currentLineName = lineMatcher.group(1);
                    previousStation = null; // 重置站点
                } else if (dataMatcher.find()) {
                    String stationA = dataMatcher.group(1).trim();
                    String stationB = dataMatcher.group(2).trim();
                    String distance = dataMatcher.group(3).trim();
                    if (currentLineName != null) {
                        writer.write(String.format("%s %s %s %skm%n", currentLineName, stationA, stationB, distance));
                    }
                }
            }

            System.out.println("转换完成，结果已写入 " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
