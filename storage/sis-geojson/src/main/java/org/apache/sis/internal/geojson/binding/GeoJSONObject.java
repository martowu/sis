/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.internal.geojson.binding;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static org.apache.sis.internal.geojson.binding.GeoJSONGeometry.*;

import java.io.Serializable;

/**
 * @author Quentin Boileau (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @version 2.0
 * @since   2.0
 * @module
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = GeoJSONFeatureCollection.class, name = "FeatureCollection"),
    @JsonSubTypes.Type(value = GeoJSONFeature.class, name = "Feature"),
    @JsonSubTypes.Type(value = GeoJSONPoint.class, name = "Point"),
    @JsonSubTypes.Type(value = GeoJSONLineString.class, name = "LineString"),
    @JsonSubTypes.Type(value = GeoJSONPolygon.class, name = "Polygon"),
    @JsonSubTypes.Type(value = GeoJSONMultiPoint.class, name = "MultiPoint"),
    @JsonSubTypes.Type(value = GeoJSONMultiLineString.class, name = "MultiLineString"),
    @JsonSubTypes.Type(value = GeoJSONMultiPolygon.class, name = "MultiPolygon"),
    @JsonSubTypes.Type(value = GeoJSONGeometryCollection.class, name = "GeometryCollection")
})
public class GeoJSONObject implements Serializable {

    private String type;
    private double[] bbox;
    private GeoJSONCRS crs;

    public GeoJSONObject() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double[] getBbox() {
        return bbox;
    }

    public void setBbox(double[] bbox) {
        this.bbox = bbox;
    }

    public GeoJSONCRS getCrs() {
        return crs;
    }

    public void setCrs(GeoJSONCRS crs) {
        this.crs = crs;
    }
}
