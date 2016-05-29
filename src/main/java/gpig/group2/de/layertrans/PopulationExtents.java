package gpig.group2.de.layertrans;

import org.geojson.LngLatAlt;

import java.util.List;

/**
 * Created by james on 27/05/2016.
 */
public class PopulationExtents extends Extents {

    public Scale pd;


    public PopulationExtents(List<LngLatAlt> area) {
        super(area);
    }
}
