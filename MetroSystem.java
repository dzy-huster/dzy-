package system;

import model.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class MetroSystem {
    private Map<String, StationInfo> stationMap = new HashMap<>();

    public void loadData(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length != 4)
                    continue;
                String lineName = parts[0];
                String from = parts[1];
                String to = parts[2];
                double distance = Double.parseDouble(parts[3].replace("km", ""));

                addStation(from, lineName);
                addStation(to, lineName);
                addEdge(from, to, lineName, distance);
                addEdge(to, from, lineName, distance);
            }
        }
    }

    private void addStation(String station, String line) {
        stationMap.putIfAbsent(station, new StationInfo());
        stationMap.get(station).lines.add(line);
    }

    private void addEdge(String from, String to, String line, double distance) {
        stationMap.get(from).edges.add(new Edge(to, line, distance));
    }

    public Map<String, Set<String>> getTransferStations() {
        return stationMap.entrySet().stream()
                .filter(e -> e.getValue().lines.size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().lines));
    }

    public Map<String, Double> findNearbyStations(String start, double maxDistance) {
        Map<String, Double> results = new HashMap<>();
        if (!stationMap.containsKey(start))
            return results;

        PriorityQueue<Map.Entry<String, Double>> queue = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getValue));
        queue.add(new AbstractMap.SimpleEntry<>(start, 0.0));

        while (!queue.isEmpty()) {
            Map.Entry<String, Double> current = queue.poll();
            String currentStation = current.getKey();
            double currentDist = current.getValue();

            if (results.containsKey(currentStation) || currentDist > maxDistance)
                continue;
            if (!currentStation.equals(start)) {
                results.put(currentStation, currentDist);
            }

            for (Edge edge : stationMap.get(currentStation).edges) {
                double newDist = currentDist + edge.distance;
                queue.add(new AbstractMap.SimpleEntry<>(edge.target, newDist));
            }
        }
        return results;
    }

    public List<List<String>> findAllPaths(String start, String end) {
        List<List<String>> paths = new ArrayList<>();
        dfs(start, end, new ArrayList<>(), new HashSet<>(), paths);
        return paths;
    }

    private void dfs(String current, String end, List<String> path,
            Set<String> visited, List<List<String>> paths) {
        visited.add(current);
        path.add(current);

        if (current.equals(end)) {
            paths.add(new ArrayList<>(path));
        } else {
            for (Edge edge : stationMap.get(current).edges) {
                if (!visited.contains(edge.target)) {
                    dfs(edge.target, end, path, visited, paths);
                }
            }
        }

        visited.remove(current);
        path.remove(path.size() - 1);
    }

    public PathResult findShortestPath(String start, String end) {
        class PathNode implements Comparable<PathNode> {
            String station;
            double distance;
            PathNode prev;
            String line;

            PathNode(String s, double d, PathNode p, String l) {
                station = s;
                distance = d;
                prev = p;
                line = l;
            }

            public int compareTo(PathNode other) {
                return Double.compare(this.distance, other.distance);
            }
        }

        Map<String, PathNode> nodes = new HashMap<>();
        PriorityQueue<PathNode> queue = new PriorityQueue<>();
        queue.add(new PathNode(start, 0, null, null));
        nodes.put(start, new PathNode(start, 0, null, null));

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            if (current.station.equals(end))
                break;

            for (Edge edge : stationMap.get(current.station).edges) {
                double newDist = current.distance + edge.distance;
                if (!nodes.containsKey(edge.target) || newDist < nodes.get(edge.target).distance) {
                    PathNode next = new PathNode(edge.target, newDist, current, edge.line);
                    nodes.put(edge.target, next);
                    queue.add(next);
                }
            }
        }

        PathNode endNode = nodes.get(end);
        if (endNode == null)
            return new PathResult(new ArrayList<>(), new ArrayList<>(), 0);

        List<String> path = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        double dist = endNode.distance;

        for (PathNode n = endNode; n != null; n = n.prev) {
            path.add(0, n.station);
            if (n.line != null)
                lines.add(0, n.line);
        }

        return new PathResult(path, lines, dist);
    }

    public String formatPath(PathResult result) {
        if (result.path.isEmpty())
            return "无有效路径";

        StringBuilder sb = new StringBuilder();
        String currentLine = result.lines.get(0);
        String startStation = result.path.get(0);

        sb.append("乘坐").append(currentLine).append("从").append(startStation);

        for (int i = 0; i < result.lines.size(); i++) {
            String nextStation = result.path.get(i + 1);
            String line = result.lines.get(i);

            if (!line.equals(currentLine)) {
                sb.append("到").append(result.path.get(i)).append("，换乘").append(line);
                currentLine = line;
            }
        }

        sb.append("到").append(result.path.get(result.path.size() - 1)).append("。");
        return sb.toString();
    }

    public double calculateFare(double distance) {
        if (distance <= 4)
            return 2;
        double[] thresholds = { 8, 12, 16, 10, Double.MAX_VALUE };
        double[] rates = { 4, 6, 8, 10, 20 };
        double fare = 2;
        distance -= 4;

        for (int i = 0; i < thresholds.length; i++) {
            if (distance <= thresholds[i]) {
                fare += Math.ceil(distance / rates[i]);
                break;
            }
            fare += Math.ceil(thresholds[i] / rates[i]);
            distance -= thresholds[i];
        }
        return Math.min(fare, 10);
    }

    public double calculateSpecialFare(PathResult path, String type) {
        if ("日票".equals(type))
            return 0;
        double base = calculateFare(path.totalDistance);
        if ("武汉通".equals(type))
            return base * 0.9;
        return base;
    }

    // 用于判断站点是否存在（供 Test.java 调用）
    public boolean stationExists(String name) {
        return stationMap.containsKey(name);
    }

}
