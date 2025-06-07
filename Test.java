import system.MetroSystem;
import model.PathResult;

import java.util.Map;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) throws Exception {
        MetroSystem metro = new MetroSystem();
        metro.loadData("formatted_subway.txt");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n *武汉地铁系统 - 功能菜单*");
            System.out.println("1. 查看所有换乘站");
            System.out.println("2. 查找指定站点附近的站点（距离 < N）");
            System.out.println("3. 查找两站之间所有路径（不含环路）");
            System.out.println("4. 查找两站之间的最短路径");
            System.out.println("5. 格式化输出最短路径换乘信息");
            System.out.println("6. 计算普通票价");
            System.out.println("7. 计算武汉通/日票票价");
            System.out.println("0. 退出");
            System.out.print("请输入功能编号：");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    System.out.println("#所有换乘站:");
                    metro.getTransferStations().forEach((k, v) -> System.out.println(k + " : " + v));
                }
                case "2" -> {
                    String start = promptValidStation(scanner, metro, "请输入起始站名：");
                    System.out.print("请输入最大距离（KM）：");
                    try {
                        double maxDist = Double.parseDouble(scanner.nextLine().trim());
                        Map<String, Double> nearby = metro.findNearbyStations(start, maxDist);
                        if (nearby.isEmpty()) {
                            System.out.println("未找到距离内的站点。");
                        } else {
                            nearby.forEach((k, v) -> System.out.printf("%s : %.2fkm\n", k, v));
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("× 输入的不是有效数字！");
                    }
                }
                case "3" -> {
                    String start = promptValidStation(scanner, metro, "请输入起点站：");
                    String end = promptValidStation(scanner, metro, "请输入终点站：");
                    var paths = metro.findAllPaths(start, end);
                    if (paths.isEmpty()) {
                        System.out.println("未找到路径。");
                    } else {
                        paths.forEach(p -> System.out.println(String.join(" -> ", p)));
                    }
                }
                case "4", "5", "6", "7" -> {
                    String start = promptValidStation(scanner, metro, "请输入起点站：");
                    String end = promptValidStation(scanner, metro, "请输入终点站：");
                    PathResult path = metro.findShortestPath(start, end);
                    if (path.path.isEmpty()) {
                        System.out.println("未找到路径。");
                    } else {
                        switch (choice) {
                            case "4" ->
                                System.out.println("最短路径: " + path.path + "\n总距离: " + path.totalDistance + "km");
                            case "5" -> System.out.println("格式化路径: " + metro.formatPath(path));
                            case "6" -> System.out.printf("普通票价: %.1f 元\n", metro.calculateFare(path.totalDistance));
                            case "7" -> {
                                System.out.print("请输入票种（普通/武汉通/日票）：");
                                String type = scanner.nextLine().trim();
                                System.out.printf("票价（%s）：%.1f 元\n", type, metro.calculateSpecialFare(path, type));
                            }
                        }
                    }
                }
                case "0" -> {
                    System.out.println("√ 感谢使用，程序退出。");
                    return;
                }
                default -> System.out.println("× 请输入有效选项（0-7）");
            }
        }
    }

    // 辅助方法：不断提示用户直到输入有效站点
    private static String promptValidStation(Scanner scanner, MetroSystem metro, String prompt) {
        while (true) {
            System.out.print(prompt);
            String station = scanner.nextLine().trim();
            if (metro.stationExists(station)) {
                return station;
            } else {
                System.out.println("× 输入的站点不存在，请重新输入！");
            }
        }
    }
}
