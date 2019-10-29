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

import org.apache.sis.internal.geojson.GeoJSONParser;
import org.apache.sis.internal.geojson.LiteJsonLocation;
import org.apache.sis.storage.geojson.GeoJSONConstants;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.sis.util.collection.BackingStoreException;

/**
 * @author Quentin Boileau (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @version 2.0
 * @since   2.0
 * @module
 */
public class GeoJSONFeatureCollection extends GeoJSONObject implements Iterator<GeoJSONFeature>, Closeable {

    private List<GeoJSONFeature> features = new ArrayList<>();

    transient JsonLocation currentPos;
    transient GeoJSONFeature current;
    transient int currentIdx;
    transient InputStream readStream;
    transient JsonParser parser;

    /**
     * If current GeoJSONFeatureCollection is in lazy parsing mode, sourceInput
     * should be not {@code null} and used to create {@link JsonParser object}
     */
    transient Path sourceInput;
    transient LiteJsonLocation startPos;
    transient LiteJsonLocation endPos;
    transient Boolean lazyMode;

    public GeoJSONFeatureCollection(Boolean lazyMode) {
        setType(GeoJSONConstants.FEATURE_COLLECTION);
        this.lazyMode = lazyMode;
    }

    public List<GeoJSONFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<GeoJSONFeature> features) {
        this.features = features;
    }

    public void setStartPosition(JsonLocation startPos) {
        this.startPos = new LiteJsonLocation(startPos);
    }

    public void setEndPosition(JsonLocation endPos) {
        this.endPos = new LiteJsonLocation(endPos);
    }

    public void setSourceInput(Path input) {
        this.sourceInput = input;
    }

    public Boolean isLazyMode() {
        return lazyMode;
    }

    @Override
    public boolean hasNext() {
        try {
            findNext();
            return current != null;
        } catch (IOException e) {
            throw new BackingStoreException(e.getMessage(), e);
        }
    }

    @Override
    public GeoJSONFeature next() {
        try {
            findNext();
            final GeoJSONFeature ob = current;
            current = null;
            if (ob == null) {
                throw new BackingStoreException("No more feature.");
            }
            return ob;
        } catch (IOException e) {
            throw new BackingStoreException(e.getMessage(), e);
        }
    }

    /**
     * Find next Feature from features list or as lazy parsing.
     *
     * @throws IOException
     */
    private void findNext() throws IOException {
        if (current != null) {
            return;
        }
        if (lazyMode) {
            if (sourceInput == null || startPos == null || endPos == null) {
                return;
            }

            if (parser == null) {
                readStream = Files.newInputStream(sourceInput);
                parser = GeoJSONParser.FACTORY.createParser(readStream);
            }

            //loop to FeatureCollection start
            if (currentPos == null) {
                while (!startPos.equals(currentPos)) {
                    parser.nextToken();
                    currentPos = parser.getCurrentLocation();
                }
            }

            current = null;

            // set parser to feature object start
            while (parser.getCurrentToken() != JsonToken.START_OBJECT && !endPos.equals(currentPos)) {

                if (parser.getCurrentToken() != JsonToken.START_OBJECT && endPos.isBefore(currentPos)) {
                    //cannot find collection end token and no more start object token
                    //break loop to avoid infinite search
                    break;
                }
                parser.nextToken();
                currentPos = parser.getCurrentLocation();
            }

            if (!endPos.equals(currentPos)) {
                GeoJSONObject obj = GeoJSONParser.parseGeoJSONObject(parser);
                if (obj instanceof GeoJSONFeature) {
                    current = (GeoJSONFeature) obj;
                }
                currentPos = parser.getCurrentLocation();
            }
        } else {
            if (currentIdx < features.size()) {
                current = features.get(currentIdx++);
            } else {
                current = null;
            }
        }
    }

    @Override
    public void remove() {
        //do nothing
    }

    @Override
    public void close() {
        //close read stream
        if (readStream != null) {
            try {
                readStream.close();
            } catch (IOException e) {
                throw new BackingStoreException(e.getMessage(), e);
            }
        }
        //close parser
        if (parser != null && !parser.isClosed()) {
            try {
                parser.close();
            } catch (IOException e) {
                throw new BackingStoreException(e.getMessage(), e);
            }
        }
    }
}
