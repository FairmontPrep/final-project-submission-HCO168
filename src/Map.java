package src;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

public class Map {
    public static void initializeTestMap(){
        ArrayList<ArrayList<Integer>> test_array_2;
        test_array_2 = new ArrayList<>();
        test_array_2.add(new ArrayList<>(Arrays.asList(1, 0, 0, 1, 0, 0, 0, 0)));
        test_array_2.add(new ArrayList<>(Arrays.asList(0, 0, 0, 1, 0, 0, 0, 0)));
        test_array_2.add(new ArrayList<>(Arrays.asList(0, 0, 0, 1, 0, 0, 1, 0)));
        test_array_2.add(new ArrayList<>(Arrays.asList(9, 0, 0, 1, 0, 0, 0, 0)));
        test_array_2.add(new ArrayList<>(Arrays.asList(0, 0, 0, 1, 0, 0, 0, 0)));
        test_array_2.add(new ArrayList<>(Arrays.asList(0, 0, 0, 1, 0, 0, 0, 0)));
        test_array_2.add(new ArrayList<>(Arrays.asList(0, 0, 0, 1, 1, 1, 0, 0)));
        test_array_2.add(new ArrayList<>(Arrays.asList(1, 0, 0, 1, 1, 1, 1, 1)));
        mapInput=new int[test_array_2.size()][test_array_2.get(0).size()];
        for(int i=0; i<test_array_2.size(); i++){
            for(int j=0; j<test_array_2.get(i).size(); j++){
                mapInput[i][j]=test_array_2.get(i).get(j);
            }
        }

    }

    public static int[][] mapInput = new int[][]{
            {0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 1, 1, 1, 1, 1},
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 1},
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 1},
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 1},
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 1},
            {1, 1, 0, 1, 1, 1, 1, 1, 1, 0}
    };

    protected int[][] map;

    public int[][] getMap() {
        return map;
    }
    final MapViewer mapViewer;
    private int updateDelayMillis = 200;

    public static final int outOfBound = -1;
    public static final int wall = 0;
    public static final int unchecked = 1;
    public static final int checked = 2;
    public static final int deadRoute = 4;
    public static final int pastPath = 3;

    public Map(int[][] map) {
        this.map = map.clone();
        initMap();
        mapViewer = new MapViewer(this);
    }

    private void initMap() {
        for (int i = 0; i < xRange(); i++) {
            for (int j = 0; j < yRange(); j++) {
                map[i][j] = (map[i][j] == 1) ? 1 : 0;
            }
        }
    }

    public int xRange() {
        return map.length;
    }

    public int yRange() {
        return map[0].length;
    }

    public int get(Position p) {
        return get(p.x(), p.y());
    }

    public int get(int x, int y) {
        if (x >= 0 && x < xRange() && y >= 0 && y < yRange()) {
            return map[x][y];
        } else {
            return outOfBound;
        }
    }

    public void set(int x, int y, int value) {
        map[x][y] = value;
    }

    public void set(Position p, int value) {
        set(p.x(), p.y(), value);
    }

    public void setUpdateDelay(int millis) {
        this.updateDelayMillis = millis;
    }

    private void updateAndPause() {
        try {
            SwingUtilities.invokeAndWait(mapViewer::repaint);
            Thread.sleep(updateDelayMillis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMapWindow() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Map Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(mapViewer);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private void resetMap() {
        for (int i = 0; i < xRange(); i++) {
            for (int j = 0; j < yRange(); j++) {
                map[i][j] = (map[i][j] == wall) ? wall : unchecked;
            }
        }
    }

    public String autoFindPath() {
        for (int j = 0; j < yRange(); j++) {
            Position p = new Position(0, j);
            if (get(p) == unchecked) {
                String result = tryFindPath(p);
                if (result != null) return result;
            }
        }
        for (int i = 1; i < xRange(); i++) {
            Position p = new Position(i, yRange() - 1);
            if (get(p) == unchecked) {
                String result = tryFindPath(p);
                if (result != null) return result;
            }
        }
        for (int j = yRange() - 2; j >= 0; j--) {
            Position p = new Position(xRange() - 1, j);
            if (get(p) == unchecked) {
                String result = tryFindPath(p);
                if (result != null) return result;
            }
        }
        for (int i = xRange() - 2; i > 0; i--) {
            Position p = new Position(i, 0);
            if (get(p) == unchecked) {
                String result = tryFindPath(p);
                if (result != null) return result;
            }
        }
        return "No valid path found.";
    }

    private String tryFindPath(Position start) {
        resetMap();
        ArrayList<Position> path = new ArrayList<>();
        path.add(start);
        set(start, checked);
        if (findPathRecur(start, path) == 0) {
            for (Position p1 : path) {
                set(p1, pastPath);
            }
            return formatOutput(path);
        }
        for (Position p1 : path) {
            set(p1, deadRoute);
        }
        return null;
    }

    public int findPathRecur(Position start, ArrayList<Position> path) {
        updateAndPause();
        if (get(start) != pastPath) {
            set(start, checked);
        }
        if (isEnd(start, path.getFirst()) && !start.equals(path.getFirst())) {
            return 0;
        }
        for (Position next : getSurrounding(start)) {
            if (get(next) == unchecked) {
                set(next, checked);
                path.add(next);
                int result = findPathRecur(next, path);
                if (result == 0) {
                    return 0;
                }
                path.remove(path.size() - 1);
                set(next, deadRoute);
            }
        }
        return 1;
    }

    public boolean isEnd(Position p, Position start) {
        if (!(p.x() == 0 || p.x() == xRange() - 1 || p.y() == 0 || p.y() == yRange() - 1)) return false;
        if (p.equals(start)) return true;
        boolean onTop = start.x() == 0;
        boolean onBottom = start.x() == xRange() - 1;
        boolean onLeft = start.y() == 0;
        boolean onRight = start.y() == yRange() - 1;
        int edgeCount = (onTop ? 1 : 0) + (onBottom ? 1 : 0) + (onLeft ? 1 : 0) + (onRight ? 1 : 0);
        if (edgeCount >= 2) return true;
        boolean pTop = p.x() == 0;
        boolean pBottom = p.x() == xRange() - 1;
        boolean pLeft = p.y() == 0;
        boolean pRight = p.y() == yRange() - 1;
        if (onTop && (pLeft || pRight)) return true;
        if (onBottom && (pLeft || pRight)) return true;
        if (onLeft && (pTop || pBottom)) return true;
        if (onRight && (pTop || pBottom)) return true;
        return false;
    }

    public Position[] getSurrounding(Position p) {
        return new Position[]{
                new Position(p.x() + 1, p.y()),
                new Position(p.x(), p.y() + 1),
                new Position(p.x() - 1, p.y()),
                new Position(p.x(), p.y() - 1)
        };
    }

    public String formatOutput(ArrayList<Position> path) {
        ArrayList<String> result = new ArrayList<>();
        for (Position p : path) {
            result.add("A[" + p.x() + "][" + p.y() + "]");
        }
        for(int i=0;i<map.length;i++) {
            for(int j=0;j<map[i].length;j++) {
                if (map[i][j] != pastPath) {
                    map[i][j]=wall;
                }
            }
        }
        updateAndPause();
        return result.toString();
    }

    public String formatOutput2(ArrayList<Position> path) {
        int[][] visual = new int[xRange()][yRange()];
        for (Position p : path) {
            visual[p.x()][p.y()] = 1;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < xRange(); i++) {
            sb.append("[");
            for (int j = 0; j < yRange(); j++) {
                sb.append(visual[i][j] == 1 ? " 1 " : "   ");
                if (j < yRange() - 1) sb.append(",");
            }
            sb.append("]");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        helperMethod();
    }

    public static void helperMethod() {
        initializeTestMap();
        Map m=new Map(mapInput);
        m.setUpdateDelay(100);
        m.showMapWindow();
        new Thread(() -> {
            System.out.println(m.autoFindPath());
        }).start();
    }
}
