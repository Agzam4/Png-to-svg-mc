package logic;

import java.awt.*;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Objects;

public class SVGPath {
    int rgba;
    ArrayList<Node> path = new ArrayList<>();
    Area hole_area;

    int minX;
    int minY;
    int maxX;
    int maxY;
    int area;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SVGPath svgPath = (SVGPath) o;
        return Objects.equals(path, svgPath.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }
}
