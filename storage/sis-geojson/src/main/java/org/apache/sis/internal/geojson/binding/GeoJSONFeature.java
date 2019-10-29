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

import org.apache.sis.storage.geojson.GeoJSONConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Quentin Boileau (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @version 2.0
 * @since   2.0
 * @module
 */
public class GeoJSONFeature extends GeoJSONObject {

    private GeoJSONGeometry geometry;
    /**
     * Identifier (id attribute) of the feature. According to RFC 7946, it is
     * optional and can either be a number or a string.
     */
    private Object id;
    private Map<String, Object> properties = new HashMap<>();

    public GeoJSONFeature() {
        setType(GeoJSONConstants.FEATURE);
    }

    public GeoJSONGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(GeoJSONGeometry geometry) {
        this.geometry = geometry;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
