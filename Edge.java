package model;

public class Edge {
    public String target;
    public String line;
    public double distance;

    public Edge(String target, String line, double distance) {
        this.target = target;
        this.line = line;
        this.distance = distance;
    }
}
