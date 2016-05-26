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

    static final double maxWidth = 500;
    static final double maxHeight = 500;

    public static void main(String args[]) throws IOException {

        FileInputStream fis = new FileInputStream(new File("SFRA_Flood_Zones.geojson"));

        FeatureCollection featureCollection =
                new ObjectMapper().readValue(fis, FeatureCollection.class);


        List<Extents> originalExtents = new ArrayList<>();
        for(Feature f : featureCollection.getFeatures()) {

            System.out.println(f.getGeometry().getClass().getName());

            Extents e = new Extents(f.getGeometry());
            originalExtents.add(e);


            /*
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
            */




        }


        List<Extents> newExtents = new ArrayList<>();
        for(Extents e : originalExtents) {


            if(e.getWidth() > maxWidth && e.getHeight() > maxHeight) {
                // trim both

                List<Extents> tmp = new ArrayList<>();
                tmp.addAll(e.hSplit(maxHeight));

                for(Extents tempe : tmp) {
                    newExtents.addAll(tempe.wSplit(maxWidth));
                }

            } else if(e.getWidth() > maxWidth) {
                // trim width
                newExtents.addAll(e.wSplit(maxWidth));
            } else if(e.getHeight() > maxHeight) {
                //trim height
                newExtents.addAll(e.hSplit(maxHeight));
            } else {
                newExtents.add(e);

            }



        }


        System.out.println(newExtents.size());

    }
}
