package gpig.group2.de.layertrans;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 25/05/2016.
 */
public class IncludeCCZonesAreaGenerator {
    public IncludeCCZonesAreaGenerator() {
    }

    static final double maxWidth = 400;
    static final double maxHeight = 400;

    public List<Extents> getZones() throws IOException {

        FileInputStream fis = new FileInputStream(new File("Cold_Calling_Controlled_Zones.geojson"));

        FeatureCollection featureCollection =
                new ObjectMapper().readValue(fis, FeatureCollection.class);


        List<Extents> originalExtents = new ArrayList<>();

        for (Feature f : featureCollection.getFeatures()) {


            GeoJsonObject g = f.getGeometry();
            if (g instanceof Polygon) {
                Extents e = new Extents(((Polygon) f.getGeometry()).getExteriorRing());
                originalExtents.add(e);
            } else if (g instanceof MultiPolygon) {
                for (List<List<LngLatAlt>> t : ((MultiPolygon) g).getCoordinates()) {

                    for (List<LngLatAlt> u : t) {
                        Extents e = new Extents(u);
                        originalExtents.add(e);
                    }
                }
            }


        }


        PopDensityAreaGenerator pdag = new PopDensityAreaGenerator();

        List<Extents> popzones = pdag.getFeatures();

        Double maxDensity = 0.0;
        for (Extents e : popzones) {

            if (e.density > maxDensity) {
                maxDensity = e.density;
            }

        }


        for (Extents e : popzones) {
            for (Extents c : originalExtents) {

                if (e.getBoxOfBoundingBox().contains(c.getBoxOfBoundingBox()) || e.getBoxOfBoundingBox().intersects(c.getBoxOfBoundingBox())) {
                    e.density = maxDensity + 50;
                }

            }
        }


        return popzones;
    }


    public static void main(String[] args) throws IOException {


        IncludeCCZonesAreaGenerator icz = new IncludeCCZonesAreaGenerator();
        List<Extents> popzones = icz.getZones();


        Integer eid = 0;

        FeatureCollection fc = new FeatureCollection();
        for(Extents e : popzones) {

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
            f.setProperty("value",e.density);
        }




        System.out.println(popzones.size());

        ObjectMapper mapper = new ObjectMapper();

        mapper.writeValue(new File("das.geojson"), fc);

    }
}
