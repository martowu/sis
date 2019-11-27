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
package org.apache.sis.internal.filter.sqlmm;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPoint;
import java.text.ParseException;
import org.apache.sis.feature.builder.FeatureTypeBuilder;
import org.apache.sis.feature.builder.PropertyTypeBuilder;
import org.apache.sis.internal.feature.jts.JTSMapping;
import org.opengis.feature.FeatureType;
import org.opengis.filter.expression.Expression;

/**
 * SQL/MM, ISO/IEC 13249-3:2011, ST_ToMultiPoint. <br>
 * Cast an ST_Geometry value to a specific instantiable subtype of ST_Geometry.
 *
 * @author Johann Sorel (Geomatys)
 * @version 2.0
 * @since   2.0
 * @module
 */
final class ST_ToMultiPoint extends AbstractAccessorSpatialFunction<Geometry> {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -2683987052895075949L;

    public static final String NAME = "ST_ToMultiPoint";

    public ST_ToMultiPoint(Expression... parameters) {
        super(parameters);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected Class<Geometry> getExpectedClass() {
        return Geometry.class;
    }

    @Override
    public Object execute(Geometry geom, Object... params) throws ParseException {
        return JTSMapping.convertType(geom, MultiPoint.class);
    }

    @Override
    public PropertyTypeBuilder expectedType(FeatureType valueType, FeatureTypeBuilder addTo) {
        return addTo.addAttribute(MultiPoint.class)
                .setCRS(expectedCrs(valueType, parameters.get(0)))
                .setName(NAME);
    }

}
