package gpig.group2.de.layertrans;

import gpig.group2.maps.geographic.position.BoundingBox;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;

import java.util.List;

/**
 * Created by james on 25/05/2016.
 */
public class ChildExtents extends Extents {

    Extents parent = new Extents();
    public ChildExtents(Extents e) {
        super();
        this.parent = e;

    }

    public ChildExtents(Extents extents, List<LngLatAlt> geometry, BoundingBox boundingBox) {
        super();
        this.parent = extents;
        this.extents = boundingBox;
        this.geometry = geometry;
    }
}
