package src;

import javax.swing.*;
import java.awt.*;

public class MapViewer extends JPanel {
    private final Map map;
    private int preferredCellSize=30;
    public MapViewer(Map map) {
        this.map = map;
        setPreferredSize(new Dimension(map.getMap()[0].length * preferredCellSize, map.getMap().length * preferredCellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int[][] grid = map.getMap();
        int rows = grid.length;
        int cols = grid[0].length;

        // Calculate cell size based on current panel size
        int cellWidth = getWidth() / cols;
        int cellHeight = getHeight() / rows;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                g.setColor(getColor(grid[y][x]));
                g.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
            }
        }
    }

    private Color getColor(int status) {
        return switch (status) {
            case Map.unchecked -> Color.WHITE;       // Unchecked
            case Map.wall -> Color.BLACK;       // Wall
            case Map.checked -> Color.BLUE;       // Checked
            case Map.deadRoute -> Color.RED;        // Dead Route
            case Map.pastPath -> Color.GREEN;      // Past Path
            default -> Color.GRAY;       // Out-of-bounds or unknown
        };
    }
}
