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
package org.apache.sis.storage.earthobservation;

import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.sis.internal.storage.AbstractResource;
import org.opengis.metadata.Metadata;
import org.opengis.util.FactoryException;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.test.TestCase;
import org.junit.Test;

import static org.apache.sis.test.Assert.*;
import static org.apache.sis.test.TestUtilities.formatMetadata;
import static org.apache.sis.storage.earthobservation.LandsatReader.DIM;


/**
 * Tests {@link LandsatReader}.
 *
 * @author  Thi Phuong Hao Nguyen (VNSC)
 * @version 1.0
 * @since   0.8
 * @module
 */
public class LandsatReaderTest extends TestCase {
    /**
     * Tests the regular expression used for detecting the
     * “Image courtesy of the U.S. Geological Survey” credit.
     */
    @Test
    public void testCreditPattern() {
        final Matcher m = LandsatReader.CREDIT.matcher("Image courtesy of the U.S. Geological Survey");
        assertTrue("matches", m.find());
        assertEquals("end", 22, m.end());
    }

    /**
     * Verifies the value of the {@link LandsatReader#BAND_GROUPS} mask.
     */
    @Test
    public void verifyBandGroupsMask() {
        final int[] PANCHROMATIC = {8};
        final int[] REFLECTIVE   = {1, 2, 3, 4, 5, 6, 7, 9};
        final int[] THERMAL      = {10, 11};
        long mask = 0;
        for (int i=0; i < PANCHROMATIC.length; i++) mask |= (LandsatReader.PANCHROMATIC/DIM << 2*(PANCHROMATIC[i] - 1));
        for (int i=0; i <   REFLECTIVE.length; i++) mask |= (LandsatReader.REFLECTIVE/DIM   <<   2*(REFLECTIVE[i] - 1));
        for (int i=0; i <      THERMAL.length; i++) mask |= (LandsatReader.THERMAL/DIM      <<      2*(THERMAL[i] - 1));
        assertEquals("BAND_GROUPS", mask, LandsatReader.BAND_GROUPS);
    }

    /**
     * Tests {@link LandsatReader#read(BufferedReader)}.
     *
     * <p><b>NOTE FOR MAINTAINER:</b> if the result of this test changes, consider updating
     * <a href="./doc-files/LandsatMetadata.html">./doc-files/LandsatMetadata.html</a> accordingly.</p>
     *
     * @throws IOException if an error occurred while reading the test file.
     * @throws DataStoreException if a property value can not be parsed as a number or a date.
     * @throws FactoryException if an error occurred while creating the Coordinate Reference System.
     */
    @Test
    @org.junit.Ignore("Requires GeoAPI 3.1.")
    public void testRead() throws IOException, DataStoreException, FactoryException {
        final Metadata actual;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                LandsatReaderTest.class.getResourceAsStream("LandsatTest.txt"), "UTF-8")))
        {
            final LandsatReader reader = new LandsatReader("LandsatTest.txt", new AbstractResource(null));
            reader.read(in);
            actual = reader.getMetadata();
        }
        final String text = formatMetadata(DefaultMetadata.castOrCopy(actual).asTreeTable());
        assertMultilinesEquals(
                "Metadata\n"
                + "  ├─Metadata identifier……………………………………………………………… LandsatTest\n"
                + "  ├─Metadata standard (1 of 2)…………………………………………… Geographic Information — Metadata Part 1: Fundamentals\n"
                + "  │   ├─Alternate title……………………………………………………………… ISO 19115-1\n"
                + "  │   ├─Edition…………………………………………………………………………………… ISO 19115-1:2014(E)\n"
                + "  │   ├─Identifier…………………………………………………………………………… 19115-1\n"
                + "  │   ├─Cited responsible party\n"
                + "  │   │   ├─Role………………………………………………………………………………… Principal investigator\n"
                + "  │   │   └─Party……………………………………………………………………………… International Organization for Standardization\n"
                + "  │   └─Presentation form………………………………………………………… Document digital\n"
                + "  ├─Metadata standard (2 of 2)…………………………………………… Geographic Information — Metadata Part 2: Extensions for imagery and gridded data\n"
                + "  │   ├─Alternate title……………………………………………………………… ISO 19115-2\n"
                + "  │   ├─Edition…………………………………………………………………………………… ISO 19115-2:2009(E)\n"
                + "  │   ├─Identifier…………………………………………………………………………… 19115-2\n"
                + "  │   ├─Cited responsible party\n"
                + "  │   │   ├─Role………………………………………………………………………………… Principal investigator\n"
                + "  │   │   └─Party……………………………………………………………………………… International Organization for Standardization\n"
                + "  │   └─Presentation form………………………………………………………… Document digital\n"
                + "  ├─Spatial representation info (1 of 2)\n"
                + "  │   ├─Number of dimensions………………………………………………… 2\n"
                + "  │   ├─Axis dimension properties (1 of 2)…………… Sample\n"
                + "  │   │   └─Dimension size……………………………………………………… 15000\n"
                + "  │   ├─Axis dimension properties (2 of 2)…………… Line\n"
                + "  │   │   └─Dimension size……………………………………………………… 15500\n"
                + "  │   ├─Transformation parameter availability…… false\n"
                + "  │   └─Check point availability……………………………………… false\n"
                + "  ├─Spatial representation info (2 of 2)\n"
                + "  │   ├─Number of dimensions………………………………………………… 2\n"
                + "  │   ├─Axis dimension properties (1 of 2)…………… Sample\n"
                + "  │   │   └─Dimension size……………………………………………………… 7600\n"
                + "  │   ├─Axis dimension properties (2 of 2)…………… Line\n"
                + "  │   │   └─Dimension size……………………………………………………… 7800\n"
                + "  │   ├─Transformation parameter availability…… false\n"
                + "  │   └─Check point availability……………………………………… false\n"
                + "  ├─Reference system info………………………………………………………… EPSG:WGS 84 / UTM zone 49N\n"
                + "  ├─Identification info\n"
                + "  │   ├─Citation………………………………………………………………………………… LandsatTest\n"
                + "  │   │   └─Date………………………………………………………………………………… 2016-06-27 16:48:12\n"
                + "  │   │       └─Date type………………………………………………………… Creation\n"
                + "  │   ├─Credit……………………………………………………………………………………… Derived from U.S. Geological Survey data\n"
                + "  │   ├─Spatial resolution (1 of 2)\n"
                + "  │   │   └─Distance……………………………………………………………………… 15.0\n"
                + "  │   ├─Spatial resolution (2 of 2)\n"
                + "  │   │   └─Distance……………………………………………………………………… 30.0\n"
                + "  │   ├─Topic category………………………………………………………………… Geoscientific information\n"
                + "  │   ├─Extent\n"
                + "  │   │   └─Geographic element\n"
                + "  │   │       ├─West bound longitude…………………………… 108°20′24″E\n"
                + "  │   │       ├─East bound longitude…………………………… 110°26′24″E\n"
                + "  │   │       ├─South bound latitude…………………………… 10°30′N\n"
                + "  │   │       ├─North bound latitude…………………………… 12°37′12″N\n"
                + "  │   │       └─Extent type code……………………………………… true\n"
                + "  │   └─Resource format\n"
                + "  │       └─Format specification citation……………… GeoTIFF Coverage Encoding Profile\n"
                + "  │           └─Alternate title………………………………………… GeoTIFF\n"
                + "  ├─Content info\n"
                + "  │   ├─Processing level code……………………………………………… Pseudo LT1\n"
                + "  │   │   ├─Authority…………………………………………………………………… Landsat\n"
                + "  │   │   └─Code space………………………………………………………………… Landsat\n"
                + "  │   ├─Attribute group (1 of 3)\n"
                + "  │   │   ├─Content type…………………………………………………………… Physical measurement\n"
                + "  │   │   ├─Attribute (1 of 8)\n"
                + "  │   │   │   ├─Description…………………………………………………… Coastal Aerosol\n"
                + "  │   │   │   ├─Name……………………………………………………………………… TestImage_B1.TIF\n"
                + "  │   │   │   ├─Max value………………………………………………………… 65535.0\n"
                + "  │   │   │   ├─Min value………………………………………………………… 1.0\n"
                + "  │   │   │   ├─Scale factor………………………………………………… 0.0127\n"
                + "  │   │   │   ├─Offset………………………………………………………………… -63.6\n"
                + "  │   │   │   ├─Bound units…………………………………………………… nm\n"
                + "  │   │   │   ├─Peak response……………………………………………… 433.0\n"
                + "  │   │   │   └─Transfer function type……………………… Linear\n"
                + "  │   │   ├─Attribute (2 of 8)\n"
                + "  │   │   │   ├─Description…………………………………………………… Blue\n"
                + "  │   │   │   ├─Name……………………………………………………………………… TestImage_B2.TIF\n"
                + "  │   │   │   ├─Max value………………………………………………………… 65535.0\n"
                + "  │   │   │   ├─Min value………………………………………………………… 1.0\n"
                + "  │   │   │   ├─Scale factor………………………………………………… 0.013\n"
                + "  │   │   │   ├─Offset………………………………………………………………… -65.1\n"
                + "  │   │   │   ├─Bound units…………………………………………………… nm\n"
                + "  │   │   │   ├─Peak response……………………………………………… 482.0\n"
                + "  │   │   │   └─Transfer function type……………………… Linear\n"
                + "  │   │   ├─Attribute (3 of 8)\n"
                + "  │   │   │   ├─Description…………………………………………………… Green\n"
                + "  │   │   │   ├─Name……………………………………………………………………… TestImage_B3.TIF\n"
                + "  │   │   │   ├─Max value………………………………………………………… 65535.0\n"
                + "  │   │   │   ├─Min value………………………………………………………… 1.0\n"
                + "  │   │   │   ├─Scale factor………………………………………………… 0.012\n"
                + "  │   │   │   ├─Offset………………………………………………………………… -60.0\n"
                + "  │   │   │   ├─Bound units…………………………………………………… nm\n"
                + "  │   │   │   ├─Peak response……………………………………………… 562.0\n"
                + "  │   │   │   └─Transfer function type……………………… Linear\n"
                + "  │   │   ├─Attribute (4 of 8)\n"
                + "  │   │   │   ├─Description…………………………………………………… Red\n"
                + "  │   │   │   ├─Name……………………………………………………………………… TestImage_B4.TIF\n"
                + "  │   │   │   ├─Max value………………………………………………………… 65535.0\n"
                + "  │   │   │   ├─Min value………………………………………………………… 1.0\n"
                + "  │   │   │   ├─Scale factor………………………………………………… 0.0101\n"
                + "  │   │   │   ├─Offset………………………………………………………………… -50.6\n"
                + "  │   │   │   ├─Bound units…………………………………………………… nm\n"
                + "  │   │   │   ├─Peak response……………………………………………… 655.0\n"
                + "  │   │   │   └─Transfer function type……………………… Linear\n"
                + "  │   │   ├─Attribute (5 of 8)\n"
                + "  │   │   │   ├─Description…………………………………………………… Near-Infrared\n"
                + "  │   │   │   ├─Name……………………………………………………………………… TestImage_B5.TIF\n"
                + "  │   │   │   ├─Max value………………………………………………………… 65535.0\n"
                + "  │   │   │   ├─Min value………………………………………………………… 1.0\n"
                + "  │   │   │   ├─Scale factor………………………………………………… 0.00619\n"
                + "  │   │   │   ├─Offset………………………………………………………………… -31.0\n"
                + "  │   │   │   ├─Bound units…………………………………………………… nm\n"
                + "  │   │   │   ├─Peak response……………………………………………… 865.0\n"
                + "  │   │   │   └─Transfer function type……………………… Linear\n"
                + "  │   │   ├─Attribute (6 of 8)\n"
                + "  │   │   │   ├─Description…………………………………………………… Short Wavelength Infrared (SWIR) 1\n"
                + "  │   │   │   ├─Name……………………………………………………………………… TestImage_B6.TIF\n"
                + "  │   │   │   ├─Max value………………………………………………………… 65535.0\n"
                + "  │   │   │   ├─Min value………………………………………………………… 1.0\n"
                + "  │   │   │   ├─Scale factor………………………………………………… 0.00154\n"
                + "  │   │   │   ├─Offset………………………………………………………………… -7.7\n"
                + "  │   │   │   ├─Bound units…………………………………………………… nm\n"
                + "  │   │   │   ├─Peak response……………………………………………… 1610.0\n"
                + "  │   │   │   └─Transfer function type……………………… Linear\n"
                + "  │   │   ├─Attribute (7 of 8)\n"
                + "  │   │   │   ├─Description…………………………………………………… Short Wavelength Infrared (SWIR) 2\n"
                + "  │   │   │   ├─Name……………………………………………………………………… TestImage_B7.TIF\n"
                + "  │   │   │   ├─Max value………………………………………………………… 65535.0\n"
                + "  │   │   │   ├─Min value………………………………………………………… 1.0\n"
                + "  │   │   │   ├─Scale factor………………………………………………… 5.19E-4\n"
                + "  │   │   │   ├─Offset………………………………………………………………… -2.6\n"
                + "  │   │   │   ├─Bound units…………………………………………………… nm\n"
                + "  │   │   │   ├─Peak response……………………………………………… 2200.0\n"
                + "  │   │   │   └─Transfer function type……………………… Linear\n"
                + "  │   │   └─Attribute (8 of 8)\n"
                + "  │   │       ├─Description…………………………………………………… Cirrus\n"
                + "  │   │       ├─Name……………………………………………………………………… TestImage_B9.TIF\n"
                + "  │   │       ├─Max value………………………………………………………… 65535.0\n"
                + "  │   │       ├─Min value………………………………………………………… 1.0\n"
                + "  │   │       ├─Scale factor………………………………………………… 0.00242\n"
                + "  │   │       ├─Offset………………………………………………………………… -12.1\n"
                + "  │   │       ├─Bound units…………………………………………………… nm\n"
                + "  │   │       ├─Peak response……………………………………………… 1375.0\n"
                + "  │   │       └─Transfer function type……………………… Linear\n"
                + "  │   ├─Attribute group (2 of 3)\n"
                + "  │   │   ├─Content type…………………………………………………………… Physical measurement\n"
                + "  │   │   └─Attribute\n"
                + "  │   │       ├─Description…………………………………………………… Panchromatic\n"
                + "  │   │       ├─Name……………………………………………………………………… TestImage_B8.TIF\n"
                + "  │   │       ├─Max value………………………………………………………… 65535.0\n"
                + "  │   │       ├─Min value………………………………………………………… 1.0\n"
                + "  │   │       ├─Scale factor………………………………………………… 0.0115\n"
                + "  │   │       ├─Offset………………………………………………………………… -57.3\n"
                + "  │   │       ├─Bound units…………………………………………………… nm\n"
                + "  │   │       ├─Peak response……………………………………………… 590.0\n"
                + "  │   │       └─Transfer function type……………………… Linear\n"
                + "  │   ├─Attribute group (3 of 3)\n"
                + "  │   │   ├─Content type…………………………………………………………… Physical measurement\n"
                + "  │   │   ├─Attribute (1 of 2)\n"
                + "  │   │   │   ├─Description…………………………………………………… Thermal Infrared Sensor (TIRS) 1\n"
                + "  │   │   │   ├─Name……………………………………………………………………… TestImage_B10.TIF\n"
                + "  │   │   │   ├─Max value………………………………………………………… 65535.0\n"
                + "  │   │   │   ├─Min value………………………………………………………… 1.0\n"
                + "  │   │   │   ├─Scale factor………………………………………………… 3.34E-4\n"
                + "  │   │   │   ├─Offset………………………………………………………………… 0.1\n"
                + "  │   │   │   ├─Bound units…………………………………………………… nm\n"
                + "  │   │   │   ├─Peak response……………………………………………… 10800.0\n"
                + "  │   │   │   └─Transfer function type……………………… Linear\n"
                + "  │   │   └─Attribute (2 of 2)\n"
                + "  │   │       ├─Description…………………………………………………… Thermal Infrared Sensor (TIRS) 2\n"
                + "  │   │       ├─Name……………………………………………………………………… TestImage_B11.TIF\n"
                + "  │   │       ├─Max value………………………………………………………… 65535.0\n"
                + "  │   │       ├─Min value………………………………………………………… 1.0\n"
                + "  │   │       ├─Scale factor………………………………………………… 3.34E-4\n"
                + "  │   │       ├─Offset………………………………………………………………… 0.1\n"
                + "  │   │       ├─Bound units…………………………………………………… nm\n"
                + "  │   │       ├─Peak response……………………………………………… 12000.0\n"
                + "  │   │       └─Transfer function type……………………… Linear\n"
                + "  │   ├─Illumination elevation angle…………………………… 58.8\n"
                + "  │   ├─Illumination azimuth angle………………………………… 116.9\n"
                + "  │   └─Cloud cover percentage…………………………………………… 8.3\n"
                + "  ├─Resource lineage\n"
                + "  │   └─Source……………………………………………………………………………………… Pseudo GLS\n"
                + "  ├─Metadata scope\n"
                + "  │   └─Resource scope………………………………………………………………… COVERAGE\n"
                + "  ├─Acquisition information\n"
                + "  │   ├─Acquisition requirement\n"
                + "  │   │   └─Identifier………………………………………………………………… Software unit tests\n"
                + "  │   ├─Operation\n"
                + "  │   │   ├─Status…………………………………………………………………………… Completed\n"
                + "  │   │   ├─Type………………………………………………………………………………… Real\n"
                + "  │   │   └─Significant event\n"
                + "  │   │       ├─Context……………………………………………………………… Acquisition\n"
                + "  │   │       └─Time……………………………………………………………………… 2016-06-26 03:02:01\n"
                + "  │   └─Platform\n"
                + "  │       ├─Identifier………………………………………………………………… Pseudo LANDSAT\n"
                + "  │       └─Instrument\n"
                + "  │           └─Identifier……………………………………………………… Pseudo TIRS\n"
                + "  ├─Date info………………………………………………………………………………………… 2016-06-27 16:48:12\n"
                + "  │   └─Date type……………………………………………………………………………… Creation\n"
                + "  └─Default locale+other locale………………………………………… en\n", text);
    }
}
