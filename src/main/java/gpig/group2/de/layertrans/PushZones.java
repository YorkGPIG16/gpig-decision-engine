package gpig.group2.de.layertrans;

import co.j6mes.infra.srf.query.QueryResponse;
import co.j6mes.infra.srf.query.ServiceQuery;
import co.j6mes.infra.srf.query.SimpleServiceQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 29/05/2016.
 */
public class PushZones {
    public static void main(String[] args) throws IOException {

        IncludeCCZonesAreaGenerator icz = new IncludeCCZonesAreaGenerator();
        List<Extents> popzones = icz.getZones();


        Integer eid = 0;

        ServiceQuery sq = new SimpleServiceQuery();
        String path = "";
        QueryResponse qr = sq.query("c2","maps");
        if(qr.Path!=null) {
            path = "http://"+qr.IP+":"+qr.Port+"/"+qr.Path;
        }



        FeatureCollection fc = new FeatureCollection();
        for(Extents e : popzones) {

            if(e.density<2) {
                continue;
            }

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
            f.setProperty("modelid",eid);


            ObjectMapper mapper = new ObjectMapper();

            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, f);


            Request.Post(path+"deployAreas/create")
                    .useExpectContinue()
                    .bodyString(sw.getBuffer().toString(), ContentType.APPLICATION_JSON)
                    .execute().returnContent().asBytes();
        }





        System.out.println(popzones.size());








    }
}
