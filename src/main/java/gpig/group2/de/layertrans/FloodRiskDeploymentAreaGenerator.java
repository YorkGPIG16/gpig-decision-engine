package gpig.group2.de.layertrans;

import com.fasterxml.jackson.databind.ObjectMapper;
import gpig.group2.maps.geographic.position.BoundingBox;
import org.geojson.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 25/05/2016.
 */
public class FloodRiskDeploymentAreaGenerator {
    public FloodRiskDeploymentAreaGenerator() {
    }

    static final double maxWidth = 400;
    static final double maxHeight = 400;

    public static void main(String args[]) throws IOException {

        FileInputStream fis = new FileInputStream(new File("SFRA_Flood_Zones.geojson"));

        FeatureCollection featureCollection =
                new ObjectMapper().readValue(fis, FeatureCollection.class);


        List<Extents> originalExtents = new ArrayList<>();
        for(Feature f : featureCollection.getFeatures()) {

            if(f.getProperty("BASED_ON_E") != null) {
                if(f.getProperty("BASED_ON_E").equals("1in1000"))
                {
                    GeoJsonObject g = f.getGeometry();
                    if(g instanceof Polygon) {
                        Extents e = new Extents(((Polygon)f.getGeometry()).getExteriorRing());
                        originalExtents.add(e);
                    } else if (g instanceof MultiPolygon){
                        for(List<List<LngLatAlt>> t : ((MultiPolygon) g).getCoordinates()) {

                            for(List<LngLatAlt> u : t) {
                                Extents e = new Extents(u);
                                originalExtents.add(e);
                            }
                        }
                    }

                }
            }

/*
            Extents e = new Extents(f.getGeometry());
            originalExtents.add(e);
  */






        }



        List<Extents> newExtentsToCheck = new ArrayList<>();
        for(Extents e : originalExtents) {


            if(e.getWidth() > maxWidth && e.getHeight() > maxHeight) {
                // trim both

                List<Extents> tmp = new ArrayList<>();
                tmp.addAll(e.hSplit(maxHeight));

                for(Extents tempe : tmp) {
                    newExtentsToCheck.addAll(tempe.wSplit(maxWidth));
                }

            } else if(e.getWidth() > maxWidth) {
                // trim width
                newExtentsToCheck.addAll(e.wSplit(maxWidth));
            } else if(e.getHeight() > maxHeight) {
                //trim height
                newExtentsToCheck.addAll(e.hSplit(maxHeight));
            } else {
                newExtentsToCheck.add(e);

            }

        }





        List<Extents> newExtents = new ArrayList<>();
        for(Extents e : newExtentsToCheck) {

            if(e.intersectsExtent() || e.containedByExtent()) {
                newExtents.add(e);
            }
        }





        Integer eid = 0;

        FeatureCollection fc = new FeatureCollection();
        for(Extents e : newExtents) {

            List<LngLatAlt> llas = new ArrayList<>();

            llas.add(new LngLatAlt(e.getExtents().getTopLeftX().getLongitudeX(),e.getExtents().getTopLeftX().getLatitudeX()));
            llas.add(new LngLatAlt(e.getExtents().getBottomRightX().getLongitudeX(),e.getExtents().getTopLeftX().getLatitudeX()));
            llas.add(new LngLatAlt(e.getExtents().getBottomRightX().getLongitudeX(),e.getExtents().getBottomRightX().getLatitudeX()));
            llas.add(new LngLatAlt(e.getExtents().getTopLeftX().getLongitudeX(),e.getExtents().getBottomRightX().getLatitudeX()));
            llas.add(new LngLatAlt(e.getExtents().getTopLeftX().getLongitudeX(),e.getExtents().getTopLeftX().getLatitudeX()));


            Feature f = new Feature();

            Polygon p = new Polygon();
            p.setExteriorRing(llas);

            f.setGeometry(p);
            eid++;
            f.setId(eid.toString());
            fc.add(f);
        }




        System.out.println(newExtents.size());

        ObjectMapper mapper = new ObjectMapper();

        mapper.writeValue(new File("das.geojson"), fc);


    }
}
