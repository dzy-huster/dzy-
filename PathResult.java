package model;

import java.util.*;

public class PathResult {
    public List<String> path;
    public List<String> lines;
    public double totalDistance;

    public PathResult(List<String> path, List<String> lines, double totalDistance) {
        this.path = path;
        this.lines = lines;
        this.totalDistance = totalDistance;
    }
}
