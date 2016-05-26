package gpig.group2.de.layertrans;

import gpig.group2.maps.geographic.Point;
import gpig.group2.maps.geographic.position.BoundingBox;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.gavaghan.geodesy.GlobalPosition;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.MultiPolygon;
import org.geojson.Polygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by james on 25/05/2016.
 */
public class Extents {
    BoundingBox extents;
    GeoJsonObject geometry;


    public Extents(List<LngLatAlt> exteriorRing) {
        this.extents = getExtentOfPoly(exteriorRing,null,null,null,null);
    }

    public BoundingBox getExtents() {
        return extents;
    }

    public double getWidth() {
        GeodeticCalculator geoCalc = new GeodeticCalculator();
        Ellipsoid wgs = Ellipsoid.WGS84;

        GlobalCoordinates a = new GlobalCoordinates(extents.getTopLeftX().getLatitudeX(),extents.getTopLeftX().getLongitudeX());
        GlobalCoordinates b = new GlobalCoordinates(extents.getTopLeftX().getLatitudeX(),extents.getBottomRightX().getLongitudeX());

        return geoCalc.calculateGeodeticCurve(wgs, a, b).getEllipsoidalDistance();
    }

    public Double getHeight() {GeodeticCalculator geoCalc = new GeodeticCalculator();
        Ellipsoid wgs = Ellipsoid.WGS84;

        GlobalCoordinates a = new GlobalCoordinates(extents.getTopLeftX().getLatitudeX(),extents.getTopLeftX().getLongitudeX());
        GlobalCoordinates b = new GlobalCoordinates(extents.getBottomRightX().getLatitudeX(),extents.getTopLeftX().getLongitudeX());

        return geoCalc.calculateGeodeticCurve(wgs, a, b).getEllipsoidalDistance();
    }

    public Extents() {

    }

    public Extents(GeoJsonObject geometry) {
        this.geometry = geometry;
        List<BoundingBox> bboxes = new ArrayList<>();


        if(geometry instanceof Polygon) {

            this.extents = getExtentOfPoly(((Polygon) geometry).getExteriorRing(),null,null,null,null);

        } else if (geometry instanceof MultiPolygon) {

            for(List<List<LngLatAlt>> p : ((MultiPolygon) geometry).getCoordinates()) {
                for(List<LngLatAlt> q : p) {
                    bboxes.add(getExtentOfPoly(q,null,null,null,null));
                }
            }


            Double right  = bboxes.get(0).getTopLeftX().getLatitudeX();
            Double left = bboxes.get(0).getBottomRightX().getLatitudeX();

            Double bottom = bboxes.get(0).getTopLeftX().getLongitudeX();
            Double top = bboxes.get(0).getBottomRightX().getLongitudeX();


            for(BoundingBox bbx : bboxes) {
                if(bbx.getTopLeftX().getLatitudeX() > right) {
                    right = bbx.getTopLeftX().getLatitudeX();
                } else if(bbx.getBottomRightX().getLatitudeX() < left) {
                    left = bbx.getBottomRightX().getLatitudeX();
                }

                if(bbx.getTopLeftX().getLongitudeX() < bottom) {
                    bottom = bbx.getTopLeftX().getLongitudeX();
                } else if(bbx.getBottomRightX().getLongitudeX() > top) {
                    top = bbx.getBottomRightX().getLongitudeX();
                }
            }


            this.extents = new BoundingBox(new Point(top,left),new Point(bottom,right));


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


        for(LngLatAlt lla : geometry) {

            if( lla.getLongitude() < left) {
                left = lla.getLongitude();
            } else if (lla.getLongitude() > right) {
                right = lla.getLongitude();
            }

            if( lla.getLatitude() < bottom) {
                bottom = lla.getLatitude();
            } else if (lla.getLatitude() > top) {
                top = lla.getLatitude();
            }

        }


        return new BoundingBox(new Point(bottom,right),new Point(top,left));

    }


    public List<Extents> hSplit(double size) {
        List<Extents> newExtents = new ArrayList<>();

        int chops = (int) Math.ceil(this.getHeight()/size);


        double deltaLat = this.extents.getTopLeftX().getLatitudeX() - this.extents.getBottomRightX().getLatitudeX();

        for(int i = 0; i<chops; i++) {
            double newBottomLat = this.extents.getBottomRightX().getLatitudeX() + i*(deltaLat/(double)chops);
            double newTopLat = this.extents.getBottomRightX().getLatitudeX() + (i+1)*(deltaLat/(double)chops);
            newExtents.add(new ChildExtents(this,geometry,new BoundingBox(new Point(newTopLat,this.extents.getTopLeftX().getLongitudeX()),new Point(newBottomLat,this.extents.getBottomRightX().getLongitudeX()))));
        }


        return newExtents;

    }


    public List<Extents> wSplit(double size) {

        List<Extents> newExtents = new ArrayList<>();

        int chops = (int) Math.ceil(this.getWidth()/size);


        double deltaLong = this.extents.getTopLeftX().getLongitudeX() - this.extents.getBottomRightX().getLongitudeX();

        for(int i = 0; i<chops; i++) {
            double newLeftLongitude = this.extents.getBottomRightX().getLongitudeX() + (i+1)*(deltaLong/(double)chops);
            double newRightLongitude = this.extents.getBottomRightX().getLongitudeX() + (i)*(deltaLong/(double)chops);
            newExtents.add(new ChildExtents(this,geometry,new BoundingBox(new Point(this.extents.getTopLeftX().getLatitudeX(),newLeftLongitude),new Point(this.extents.getBottomRightX().getLatitudeX(),newRightLongitude))));
        }

        return newExtents;

    }
}
