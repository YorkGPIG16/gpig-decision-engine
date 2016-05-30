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
public class PopDensityAreaGenerator {
    public PopDensityAreaGenerator() {
    }

    static final double maxWidth = 400;
    static final double maxHeight = 400;

    public List<Extents> getFeatures() throws IOException {
        FileInputStream fis = new FileInputStream(new File("cdrc-2011-census-data-packs-for-local-authority-district-york-e06000014.geojson"));

        FeatureCollection featureCollection =
                new ObjectMapper().readValue(fis, FeatureCollection.class);


        List<PopulationExtents> originalExtents = new ArrayList<>();
        for (Feature f : featureCollection.getFeatures()) {


            if (f.getProperty("value") != null) {

                GeoJsonObject g = f.getGeometry();
                if (g instanceof Polygon) {
                    PopulationExtents e = new PopulationExtents(((Polygon) f.getGeometry()).getExteriorRing());


                    if ((Double) f.getProperty("value") > 100) {
                        e.pd = Scale.VeryHigh;
                    } else if ((Double) f.getProperty("value") > 50) {
                        e.pd = Scale.High;
                    } else if ((Double) f.getProperty("value") > 10) {
                        e.pd = Scale.Medium;
                    } else if ((Double) f.getProperty("value") > 5) {
                        e.pd = Scale.Low;
                    } else {
                        e.pd = Scale.VeryLow;
                    }
                    e.density = f.getProperty("value");


                    originalExtents.add(e);
                } else if (g instanceof MultiPolygon) {
                    for (List<List<LngLatAlt>> t : ((MultiPolygon) g).getCoordinates()) {

                        for (List<LngLatAlt> u : t) {
                            PopulationExtents e = new PopulationExtents(u);

                            if ((Double) f.getProperty("value") > 100) {
                                e.pd = Scale.VeryHigh;
                            } else if ((Double) f.getProperty("value") > 50) {
                                e.pd = Scale.High;
                            } else if ((Double) f.getProperty("value") > 10) {
                                e.pd = Scale.Medium;
                            } else if ((Double) f.getProperty("value") > 5) {
                                e.pd = Scale.Low;
                            } else {
                                e.pd = Scale.VeryLow;
                            }
                            e.density = f.getProperty("value");


                            originalExtents.add(e);
                        }
                    }
                }


            }

        }


        FloodRiskDeploymentAreaGenerator fag = new FloodRiskDeploymentAreaGenerator();
        List<Extents> floodsAreas = fag.getFeatures();

        for (Extents e : floodsAreas) {
            for (PopulationExtents p : originalExtents) {

                if (p.getPathOfExtents().intersects(e.getBoxOfBoundingBox()) || e.getBoxOfBoundingBox().contains(p.getBoxOfBoundingBox())) {
                    e.upgrade(p.pd);
                    e.upgradeDensity(p.density);
                }
            }
        }


        return floodsAreas;

    }

    public static void main(String args[]) throws IOException {
        PopDensityAreaGenerator pdag = new PopDensityAreaGenerator();
        List<Extents> floodsAreas = pdag.getFeatures();

        Integer eid = 0;

        FeatureCollection fc = new FeatureCollection();
        for(Extents e : floodsAreas) {


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




        System.out.println(floodsAreas.size());

        ObjectMapper mapper = new ObjectMapper();

        mapper.writeValue(new File("das.geojson"), fc);


    }
}
