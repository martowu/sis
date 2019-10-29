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

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.sis.internal.geojson.GeoJSONParser;
import org.apache.sis.internal.geojson.GeoJSONUtils;
import org.apache.sis.internal.geojson.LiteJsonLocation;
import org.apache.sis.test.TestCase;

/**
 * @author Quentin Boileau (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @version 2.0
 * @since   2.0
 * @module
 */
public class LiteJsonLocationTest extends TestCase {

    @Test
    public void testEquality () throws URISyntaxException, IOException {
        URL fcFile = GeoJSONReadTest.class.getResource("/org/apache/sis/internal/storage/geojson/featurecollection.json");
        Path fcPath = Paths.get(fcFile.toURI());

        JsonLocation streamLocation = null;
        JsonLocation readerLocation = null;

        //get Location from stream
        try (InputStream stream = Files.newInputStream(fcPath);
             JsonParser parser = GeoJSONParser.FACTORY.createParser(stream)) {
            streamLocation = moveAndReturnPos(parser);
        }

        //get Location from reader
        try (BufferedReader reader = Files.newBufferedReader(fcPath, Charset.forName("UTF-8"));
             JsonParser parser = GeoJSONParser.FACTORY.createParser(reader)) {
            readerLocation = moveAndReturnPos(parser);
        }

        Assert.assertFalse(streamLocation.equals(readerLocation));
        Assert.assertFalse(GeoJSONUtils.equals(streamLocation, readerLocation));

        LiteJsonLocation liteStreamLocation = new LiteJsonLocation(streamLocation);
        LiteJsonLocation liteReaderLocation = new LiteJsonLocation(readerLocation);

        Assert.assertTrue(liteStreamLocation.equals(liteReaderLocation));

        Assert.assertTrue(liteStreamLocation.equals(streamLocation));
        Assert.assertTrue(liteStreamLocation.equals(readerLocation));

        Assert.assertTrue(liteReaderLocation.equals(streamLocation));
        Assert.assertTrue(liteReaderLocation.equals(readerLocation));

    }


    @Test
    public void testBefore () throws URISyntaxException, IOException {
        URL fcFile = GeoJSONReadTest.class.getResource("/org/apache/sis/internal/storage/geojson/featurecollection.json");
        Path fcPath = Paths.get(fcFile.toURI());

        //get Location from stream
        try (InputStream stream = Files.newInputStream(fcPath);
             JsonParser parser = GeoJSONParser.FACTORY.createParser(stream)) {
            parser.nextToken();

            JsonLocation currentLocation = parser.getCurrentLocation();
            LiteJsonLocation liteJsonLocation = new LiteJsonLocation(currentLocation);
            Assert.assertFalse(liteJsonLocation.isBefore(currentLocation));

            parser.nextToken();
            currentLocation = parser.getCurrentLocation();
            Assert.assertTrue(liteJsonLocation.isBefore(currentLocation));
        }
    }


    private JsonLocation moveAndReturnPos(JsonParser parser) throws IOException {
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
        return parser.getCurrentLocation();
    }
}
