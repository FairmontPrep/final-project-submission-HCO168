package src;

public record Position(int x, int y) implements Comparable<Position> {
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public int compareTo(Position o) {
        return 0;
    }
}
