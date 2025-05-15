package src;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Map {
    protected int[][] map;
    final MapViewer mapViewer;
    private int updateDelayMillis = 200; // default 200ms

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
    private void update(){
        try {
            SwingUtilities.invokeAndWait(mapViewer::repaint);
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAvailable(int status) {
        return status<1;
    }
    public boolean isWall(int status) {
        return status==wall;
    }
    public boolean unchecked(int status) {
        return status== unchecked;
    }
    public boolean checking(int status) {
        return status== checked;
    }
    public boolean deadRoute(int status) {
        return status==deadRoute;
    }
    public boolean pastPath(int status) {
        return status==pastPath;
    }
    public int numOfUnchecked(Position p){
        return numOfUnchecked(p.x(),p.y());
    }
    public int numOfUnchecked(int x,int y) {
        int count=0;
        if(unchecked(get(x+1,y))){
            count++;
        }
        if(unchecked(get(x,y+1))){
            count++;
        }
        if(unchecked(get(x-1,y))){
            count++;
        }
        if(unchecked(get(x,y-1))){
            count++;
        }
        return count;
    }
    public final int outOfBound=2;
    public final int wall=1;
    public final int unchecked=0;
    public final int checked=-1;
    public final int deadRoute=-2;
    public final int pastPath=-3;
    public Map(int[][] map) {
        this.map = map.clone();
        mapViewer=new MapViewer(this);
    }
    public int[][] getMap() {
        return map;
    }
    public int get(Position p){
        return get(p.x(),p.y());
    }
    public int get(int x,int y) {
        if(x>=0&&x<map.length&&y>=0&&y<map[0].length) {
            return map[x][y];
        }else{
            return outOfBound;
        }
    }
    public void set(int x,int y,int value) {
        map[x][y]=value;
    }
    public void set(Position p,int value) {
        set(p.x(),p.y(),value);
    }
    public int distance1(Position p1, Position p2) {
        return Math.abs(p1.x()-p2.x())+Math.abs(p1.y()-p2.y());
    }
    public double distance(Position p1, Position p2) {
        return Math.abs(p1.x()-p2.x())+Math.abs(p1.y()-p2.y());
    }
    public ArrayList<Position> getUncheckedSurrounding(Position p) {
        Position[] positions = getSurrounding(p);
        ArrayList<Position> surroundings=new ArrayList<>();
        for(Position p1:positions){
            if(unchecked(get(p1))){
                surroundings.add(p1);
            }
        }
        return surroundings;
    }

    public ArrayList<Position> getCheckedSurrounding(Position p) {
        Position[] positions = getSurrounding(p);
        ArrayList<Position> surroundings=new ArrayList<>();
        for(Position p1:positions){
            if(checked==(get(p1))){
                surroundings.add(p1);
            }
        }
        return surroundings;
    }
    public Position[] getSurrounding(Position p) {
        return new Position[]{new Position(p.x()+1,p.y()), new Position(p.x(),p.y()+1),
                new Position(p.x()-1,p.y()), new Position(p.x(),p.y()-1)};
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

    public static <T> void removeInterval(ArrayList<T> path, int start, int end) {
        if (start > end) {
            removeInterval(path, end, start);
            return;
        }
        for (int i = end - 1; i >start; i--) {
            path.remove(i);
        }
    }

    public void findShortCut(Position p,ArrayList<Position> path) {
        ArrayList<Position> checkedSurroundings=getCheckedSurrounding(p);
        for(Position p1:checkedSurroundings){
            if(path.contains(p1)&&path.contains(p)){
                removeInterval(path,path.indexOf(p1),path.indexOf(p));

            }
        }

    }

    public String findPath(Position start,Position end) {
        showMapWindow();
        ArrayList<Position> path=new ArrayList<>();
        path.add(start);
        set(start,checked);
        findPathRecur(start,end,path);
        for(Position p1:path){
            set(p1,pastPath);
        }
        return path.toString();
    }
    public int findPathRecur(Position start,Position end,ArrayList<Position> path) {
        updateAndPause();
        System.out.println(start+" "+end);
        System.out.println(mapString());
        if(get(start)!=pastPath){
            set(start,checked);
        }
        if(start.equals(end)){
            path.add(start);
            return 0;
        }
        ArrayList<Position> surroundings=getUncheckedSurrounding(start);
        if(surroundings.isEmpty()){
            return 1;
        }
        //sorting the longest distance to shortest
        surroundings.sort((o1, o2) -> -Double.compare(distance(o1, end),distance(o2, end)));
        for(int i=surroundings.size()-1;i>=0;i--){
            set(surroundings.get(i),checked);
            path.add(surroundings.getLast());
            int code=findPathRecur(surroundings.get(i),end,path);
            if(code==0){
                //if(path.contains(surroundings.get(i))){
                //    findShortCut(surroundings.get(i),path);
                //}
                System.out.println(path);
                return 0;
            }
            set(surroundings.get(i),deadRoute);
            surroundings.removeLast();
            path.removeLast();
        }
        return 1;
    }

    public static String makeTable(String[][] content){
        int n_col=0;//the max number of column
        for(int r=0;r<content.length;r++){
            if(content[r].length>n_col){
                n_col=content[r].length;
            }
        }//get the max number of the columns from every rows
        int[] list_n_colChars=new int[n_col];//list of number of chars in each column
        for(int r=0;r<content.length;r++){
            for(int c=0;c<content[r].length;c++){
                if(content[r][c].length()>list_n_colChars[c]){
                    list_n_colChars[c]=content[r][c].length();
                }
            }
        }
        int n_maxChars=0;
        for(int c=0;c<n_col;c++){
            if(list_n_colChars[c]>n_maxChars){
                n_maxChars=list_n_colChars[c];
            }
        }
        n_maxChars++;
        //create the String full of ' ' to fill up the interval.
        String spaces=" ".repeat(n_maxChars);
        StringBuilder sb=new StringBuilder();
        for(int r=0;r<content.length;r++){
            for(int c=0;c<content[r].length;c++){
                sb.append(content[r][c]).append(spaces,content[r][c].length(),list_n_colChars[c]+1);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    public String mapString(){
        String[][] strs=new String[map.length][map[0].length];
        for(int i=0;i<map.length;i++){
            for(int j=0;j<map[i].length;j++){
                strs[i][j]=switch (map[i][j]){
                    case 0 -> " ";
                    case 1 -> "■";
                    case -1 -> ".";
                    case -2 -> "0";
                    case -3 -> "+";
                    default -> "?";
                };
            }
        }
        return makeTable(strs);
    }
    public static int[][] generatePuzzle(int width, int height, Position start, Position end, double wallProbability) {
        int[][] map = new int[height][width];

        // Initialize map with all cells as wall
        for (int[] row : map) {
            Arrays.fill(row, 1); // Wall
        }

        // Create ensured path
        Random rand = new Random();
        Position current = new Position(start.x(), start.y());
        map[current.x()][current.y()] = 0;

        while (!current.equals(end)) {
            ArrayList<Position> options = new ArrayList<>();

            if (current.x() < end.x()) options.add(new Position(current.x() + 1, current.y()));
            if (current.y() < end.y()) options.add(new Position(current.x(), current.y() + 1));
            if (current.x() > end.x()) options.add(new Position(current.x() - 1, current.y()));
            if (current.y() > end.y()) options.add(new Position(current.x(), current.y() - 1));

            // Randomly choose next step
            if (options.isEmpty()) break;
            Position next = options.get(rand.nextInt(options.size()));
            map[next.x()][next.y()] = 0;
            current = next;
        }

        // Randomly remove some walls (add empty space), but not on ensured path
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (map[i][j] == 1 && rand.nextDouble() > wallProbability) {
                    map[i][j] = 0; // Make space
                }
            }
        }

        return map;
    }
    public static int[][] generateMap(long seed){
        int[][] bigMap = new int[30][30];
        long ms=System.currentTimeMillis();
        System.out.println(ms);
        Random rand = new Random(seed==0?ms:seed);

        // Fill with open paths
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                bigMap[i][j] = (rand.nextDouble() < 0.35) ? 1 : 0; // 30% chance wall
            }
        }

        // Ensure start and end are open
        bigMap[0][0] = 0;
        bigMap[29][29] = 0;
        return bigMap;
    }

    public static void main(String[] args) {
        Map m = new Map(new int[][]{
                {0,0,0,0,0},
                {0,1,0,1,1},
                {1,0,0,0,0},
                {0,0,0,1,1},
                {1,1,0,0,0},
        });
        Map m1 = new Map(generatePuzzle(100,100,
                new Position(0,0),new Position(99,99),0.4));
        m.setUpdateDelay(1000);
        m.showMapWindow();

        new Thread(() -> {
            System.out.println(m.findPath(new Position(0, 0), new Position(4, 4)));
        }).start();
    }

}

