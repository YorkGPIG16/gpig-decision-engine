package gpig.group2.de.layertrans;
import gpig.group2.maps.geographic.Point;
import gpig.group2.maps.geographic.position.BoundingBox;
import javafx.scene.shape.Rectangle;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.MultiPolygon;
import org.geojson.Polygon;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 25/05/2016.
 */
public class Extents {
    BoundingBox extents;
    List<LngLatAlt> geometry;


    public Extents(List<LngLatAlt> exteriorRing) {
        this.geometry = exteriorRing;
        this.extents = getExtentOfPoly(exteriorRing, null, null, null, null);
    }

    public BoundingBox getExtents() {
        return extents;
    }

    public double getWidth() {
        GeodeticCalculator geoCalc = new GeodeticCalculator();
        Ellipsoid wgs = Ellipsoid.WGS84;

        GlobalCoordinates a = new GlobalCoordinates(extents.getTopLeftX().getLatitudeX(), extents.getTopLeftX().getLongitudeX());
        GlobalCoordinates b = new GlobalCoordinates(extents.getTopLeftX().getLatitudeX(), extents.getBottomRightX().getLongitudeX());

        return geoCalc.calculateGeodeticCurve(wgs, a, b).getEllipsoidalDistance();
    }

    public Double getHeight() {
        GeodeticCalculator geoCalc = new GeodeticCalculator();
        Ellipsoid wgs = Ellipsoid.WGS84;

        GlobalCoordinates a = new GlobalCoordinates(extents.getTopLeftX().getLatitudeX(), extents.getTopLeftX().getLongitudeX());
        GlobalCoordinates b = new GlobalCoordinates(extents.getBottomRightX().getLatitudeX(), extents.getTopLeftX().getLongitudeX());

        return geoCalc.calculateGeodeticCurve(wgs, a, b).getEllipsoidalDistance();
    }

    public Extents() {

    }

    public Extents(GeoJsonObject geometry) {
        List<BoundingBox> bboxes = new ArrayList<>();


        if (geometry instanceof Polygon) {

            this.extents = getExtentOfPoly(((Polygon) geometry).getExteriorRing(), null, null, null, null);

        } else if (geometry instanceof MultiPolygon) {

            for (List<List<LngLatAlt>> p : ((MultiPolygon) geometry).getCoordinates()) {
                for (List<LngLatAlt> q : p) {
                    bboxes.add(getExtentOfPoly(q, null, null, null, null));
                }
            }


            Double right = bboxes.get(0).getTopLeftX().getLongitudeX();
            Double left = bboxes.get(0).getBottomRightX().getLongitudeX();

            Double bottom = bboxes.get(0).getTopLeftX().getLatitudeX();
            Double top = bboxes.get(0).getBottomRightX().getLatitudeX();


            for (BoundingBox bbx : bboxes) {
                if (bbx.getTopLeftX().getLongitudeX() > right) {
                    right = bbx.getTopLeftX().getLongitudeX();
                } else if (bbx.getBottomRightX().getLongitudeX() < left) {
                    left = bbx.getBottomRightX().getLongitudeX();
                }

                if (bbx.getTopLeftX().getLatitudeX() < bottom) {
                    bottom = bbx.getTopLeftX().getLatitudeX();
                } else if (bbx.getBottomRightX().getLatitudeX() > top) {
                    top = bbx.getBottomRightX().getLatitudeX();
                }
            }


            this.extents = new BoundingBox(new Point(top, left), new Point(bottom, right));


        }
    }


    public BoundingBox getExtentOfPoly(List<LngLatAlt> geometry, Double bottom, Double right, Double top, Double left) {

        {
            LngLatAlt lla = geometry.get(0);
            if (right == null) {
                right = lla.getLongitude();
            }

            if (left == null) {
                left = lla.getLongitude();
            }

            if (bottom == null) {
                bottom = lla.getLatitude();
            }

            if (top == null) {
                top = lla.getLatitude();
            }

        }


        for (LngLatAlt lla : geometry) {

            if (lla.getLongitude() < left) {
                left = lla.getLongitude();
            } else if (lla.getLongitude() > right) {
                right = lla.getLongitude();
            }

            if (lla.getLatitude() < bottom) {
                bottom = lla.getLatitude();
            } else if (lla.getLatitude() > top) {
                top = lla.getLatitude();
            }

        }


        return new BoundingBox(new Point(bottom, right), new Point(top, left));

    }


    public List<Extents> hSplit(double size) {
        List<Extents> newExtents = new ArrayList<>();

        int chops = (int) Math.ceil(this.getHeight() / size);


        double deltaLat = this.extents.getTopLeftX().getLatitudeX() - this.extents.getBottomRightX().getLatitudeX();

        for (int i = 0; i < chops; i++) {
            double newBottomLat = this.extents.getBottomRightX().getLatitudeX() + i * (deltaLat / (double) chops);
            double newTopLat = this.extents.getBottomRightX().getLatitudeX() + (i + 1) * (deltaLat / (double) chops);
            newExtents.add(new ChildExtents(this, geometry, new BoundingBox(new Point(newTopLat, this.extents.getTopLeftX().getLongitudeX()), new Point(newBottomLat, this.extents.getBottomRightX().getLongitudeX()))));
        }


        return newExtents;

    }


    public List<Extents> wSplit(double size) {

        List<Extents> newExtents = new ArrayList<>();

        int chops = (int) Math.ceil(this.getWidth() / size);


        double deltaLong = this.extents.getTopLeftX().getLongitudeX() - this.extents.getBottomRightX().getLongitudeX();

        for (int i = 0; i < chops; i++) {
            double newLeftLongitude = this.extents.getBottomRightX().getLongitudeX() + (i + 1) * (deltaLong / (double) chops);
            double newRightLongitude = this.extents.getBottomRightX().getLongitudeX() + (i) * (deltaLong / (double) chops);
            newExtents.add(new ChildExtents(this, geometry, new BoundingBox(new Point(this.extents.getTopLeftX().getLatitudeX(), newLeftLongitude), new Point(this.extents.getBottomRightX().getLatitudeX(), newRightLongitude))));
        }

        return newExtents;

    }


    public Rectangle2D.Double getBoxOfBoundingBox() {
        Rectangle2D.Double r = new Rectangle2D.Double();

        r.setFrameFromDiagonal(new Point2D.Double(this.getExtents().getTopLeftX().getLatitudeX(),this.getExtents().getTopLeftX().getLongitudeX()),
                new Point2D.Double(this.getExtents().getBottomRightX().getLatitudeX(),this.getExtents().getBottomRightX().getLongitudeX()));

        return r;
    }

    public Path2D.Double getPathOfExtents() {


        Path2D.Double p = new Path2D.Double();


        boolean first = true;
        for(LngLatAlt lla : this.geometry) {
            if(first) {
                first = false;
                p.moveTo(lla.getLatitude(),lla.getLongitude());
            } else {
                p.lineTo(lla.getLatitude(), lla.getLongitude());
            }
        }
        p.closePath();

        return p;
    }


    public boolean containedByExtent() {

        return this.getPathOfExtents().contains(getBoxOfBoundingBox());
    }

    public boolean intersectsExtent() {
        return this.getPathOfExtents().intersects(getBoxOfBoundingBox());
    }

}

