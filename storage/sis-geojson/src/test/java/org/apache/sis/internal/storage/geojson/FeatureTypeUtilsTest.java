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
package org.apache.sis.internal.storage.geojson;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.junit.Test;
import org.opengis.util.FactoryException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import org.apache.sis.feature.builder.AttributeRole;
import org.apache.sis.feature.builder.FeatureTypeBuilder;
import org.apache.sis.internal.feature.AttributeConvention;
import org.apache.sis.internal.geojson.FeatureTypeUtils;
import org.apache.sis.internal.geojson.GeoJSONUtils;
import org.apache.sis.test.TestCase;

import static org.junit.Assert.*;
import org.opengis.feature.FeatureAssociationRole;
import org.opengis.feature.FeatureType;
import org.opengis.feature.PropertyNotFoundException;
import org.opengis.feature.PropertyType;
import org.opengis.util.GenericName;

/**
 * @author Quentin Boileau (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @version 2.0
 * @since   2.0
 * @module
 */
public class FeatureTypeUtilsTest extends TestCase {

    public static void main(String[] args) throws Exception {
       new FeatureTypeUtilsTest().writeReadFTTest();
    }

    @Test
    public void writeReadFTTest() throws Exception {

        Path featureTypeFile = Files.createTempFile("complexFT", ".json");

        FeatureType featureType = createComplexType();
        FeatureTypeUtils.writeFeatureType(featureType, featureTypeFile);

        assertTrue(Files.size(featureTypeFile) > 0);

        FeatureType readFeatureType = FeatureTypeUtils.readFeatureType(featureTypeFile);

        assertNotNull(readFeatureType);
        assertTrue(hasAGeometry(readFeatureType));
        assertNotNull(GeoJSONUtils.getCRS(readFeatureType));

        equalsIgnoreConvention(featureType, readFeatureType);
    }

    @Test
    public void writeReadNoCRSFTTest() throws Exception {

        Path featureTypeFile = Files.createTempFile("geomFTNC", ".json");

        FeatureType featureType = createGeometryNoCRSFeatureType();
        FeatureTypeUtils.writeFeatureType(featureType, featureTypeFile);

        assertTrue(Files.size(featureTypeFile) > 0);

        FeatureType readFeatureType = FeatureTypeUtils.readFeatureType(featureTypeFile);

        assertNotNull(readFeatureType);
        assertTrue(hasAGeometry(readFeatureType));
        assertNull(GeoJSONUtils.getCRS(readFeatureType));

        equalsIgnoreConvention(featureType, readFeatureType);
    }

    @Test
    public void writeReadCRSFTTest() throws Exception {

        Path featureTypeFile = Files.createTempFile("geomFTC", ".json");

        FeatureType featureType = createGeometryCRSFeatureType();
        FeatureTypeUtils.writeFeatureType(featureType, featureTypeFile);

        assertTrue(Files.size(featureTypeFile) > 0);

        FeatureType readFeatureType = FeatureTypeUtils.readFeatureType(featureTypeFile);

        assertNotNull(readFeatureType);
        assertTrue(hasAGeometry(readFeatureType));
        assertNotNull(GeoJSONUtils.getCRS(readFeatureType));

        equalsIgnoreConvention(featureType, readFeatureType);
    }

    public static FeatureType createComplexType() throws FactoryException {
        FeatureTypeBuilder ftb = new FeatureTypeBuilder();

        ftb.setName("complexAtt1");
        ftb.addAttribute(Long.class).setName("longProp2");
        ftb.addAttribute(String.class).setName("stringProp2");
        final FeatureType complexAtt1 = ftb.build();

        ftb = new FeatureTypeBuilder();
        ftb.setName("complexAtt2");
        ftb.addAttribute(Long.class).setName("longProp2");
        ftb.addAttribute(Date.class).setName("dateProp");
        final FeatureType complexAtt2 = ftb.build();

        ftb = new FeatureTypeBuilder();
        ftb.setName("complexFT");
        ftb.addAttribute(Polygon.class).setName("geometry").setCRS(CommonCRS.WGS84.geographic()).addRole(AttributeRole.DEFAULT_GEOMETRY);
        ftb.addAttribute(Long.class).setName("longProp");
        ftb.addAttribute(String.class).setName("stringProp");
        ftb.addAttribute(Integer.class).setName("integerProp");
        ftb.addAttribute(Boolean.class).setName("booleanProp");
        ftb.addAttribute(Date.class).setName("dateProp");

        ftb.addAssociation(complexAtt1).setName("complexAtt1");
        ftb.addAssociation(complexAtt2).setName("complexAtt2").setMinimumOccurs(0).setMaximumOccurs(Integer.MAX_VALUE);
        ftb.setDescription(new SimpleInternationalString("Description"));
        return ftb.build();
    }

    private FeatureType createGeometryNoCRSFeatureType() {
        final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
        ftb.setName("FT1");
        ftb.addAttribute(Point.class).setName("geometry").addRole(AttributeRole.DEFAULT_GEOMETRY);
        ftb.addAttribute(String.class).setName(AttributeConvention.IDENTIFIER_PROPERTY);
        ftb.addAttribute(String.class).setName("type");

        return ftb.build();
    }

    private FeatureType createGeometryCRSFeatureType() {
        final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
        ftb.setName("FT2");
        ftb.addAttribute(Point.class).setName("geometry").setCRS(CommonCRS.WGS84.geographic()).addRole(AttributeRole.DEFAULT_GEOMETRY);
        ftb.addAttribute(String.class).setName(AttributeConvention.IDENTIFIER_PROPERTY);
        ftb.addAttribute(String.class).setName("type");

        return ftb.build();
    }

    /**
     * Loop on properties, returns true if there is at least one geometry property.
     *
     * @param type
     * @return true if type has a geometry.
     */
    public static boolean hasAGeometry(FeatureType type){
        for (PropertyType pt : type.getProperties(true)){
            if (AttributeConvention.isGeometryAttribute(pt)) return true;
        }
        return false;
    }


    /**
     * Test field equality ignoring convention properties.
     */
    public static boolean equalsIgnoreConvention(FeatureType type1, FeatureType type2){

        if (type1 == type2) {
            return true;
        }

        //check base properties
        if (!Objects.equals(type1.getName(),        type2.getName()) ||
            !Objects.equals(type1.getDefinition(),  type2.getDefinition()) ||
            !Objects.equals(type1.getDesignation(), type2.getDesignation()) ||
            !Objects.equals(type1.getDesignation(), type2.getDesignation()) ||
            !Objects.equals(type1.isAbstract(),     type2.isAbstract())){
            return false;
        }

        //check super types
        final Set<? extends FeatureType> super1 = type1.getSuperTypes();
        final Set<? extends FeatureType> super2 = type2.getSuperTypes();
        if(super1.size() != super2.size()) return false;
        final Iterator<? extends FeatureType> site1 = super1.iterator();
        final Iterator<? extends FeatureType> site2 = super2.iterator();
        while(site1.hasNext()){
            if(!equalsIgnoreConvention(site1.next(), site2.next())) return false;
        }

        //check properties
        final Set<GenericName> visited = new HashSet<>();
        for (PropertyType pt1 : type1.getProperties(true)) {
            visited.add(pt1.getName());
            if (AttributeConvention.contains(pt1.getName())) continue;
            try {
                final PropertyType pt2 = type2.getProperty(pt1.getName().toString());
                if (!equalsIgnoreConvention(pt1, pt2)) return false;
            } catch (PropertyNotFoundException ex) {
                return false;
            }
        }

        for (PropertyType pt2 : type2.getProperties(true)) {
            if (AttributeConvention.contains(pt2.getName()) || visited.contains(pt2.getName())) continue;
            try {
                final PropertyType pt1 = type1.getProperty(pt2.getName().toString());
                if (!equalsIgnoreConvention(pt1, pt2)) return false;
            } catch (PropertyNotFoundException ex) {
                return false;
            }
        }

        return true;
    }

    private static boolean equalsIgnoreConvention(PropertyType pt1, PropertyType pt2){
        if(pt1 instanceof FeatureAssociationRole){
            if(pt2 instanceof FeatureAssociationRole){
                final FeatureAssociationRole far1 = (FeatureAssociationRole) pt1;
                final FeatureAssociationRole far2 = (FeatureAssociationRole) pt2;

                 //check base properties
                if (!Objects.equals(far1.getName(),        far2.getName()) ||
                    !Objects.equals(far1.getDefinition(),  far2.getDefinition()) ||
                    !Objects.equals(far1.getDesignation(), far2.getDesignation()) ||
                    !Objects.equals(far1.getDesignation(), far2.getDesignation())){
                    return false;
                }

                if(far1.getMinimumOccurs()!=far2.getMinimumOccurs()||
                   far1.getMaximumOccurs()!=far2.getMaximumOccurs()){
                    return false;
                }

                if(!equalsIgnoreConvention(far1.getValueType(), far2.getValueType())){
                    return false;
                }

            }else{
                return false;
            }
        }else if(!pt1.equals(pt2)){
            return false;
        }
        return true;
    }

}
