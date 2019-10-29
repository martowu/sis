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
package org.apache.sis.storage.geojson;

import com.fasterxml.jackson.core.JsonEncoding;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.sis.internal.feature.AttributeConvention;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.collection.BackingStoreException;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;

/**
 * @author Quentin Boileau (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @version 2.0
 * @since   2.0
 * @module
 */
final class GeoJSONFileWriter extends GeoJSONReader {

    private final GeoJSONWriter writer;

    private Feature edited;
    private Feature lastWritten;
    private Path tmpFile;

    public GeoJSONFileWriter(Path jsonFile, FeatureType featureType, ReadWriteLock rwLock,
            final String encoding, final int doubleAccuracy) throws DataStoreException {
        super(jsonFile, featureType, rwLock);

        JsonEncoding jsonEncoding = JsonEncoding.UTF8;

        try {
            final String name = featureType.getName().tip().toString();
            tmpFile = jsonFile.resolveSibling(name + ".wjson");
            writer = new GeoJSONWriter(tmpFile, jsonEncoding, doubleAccuracy, false);

            //start write feature collection.
            writer.writeStartFeatureCollection(crs, null);
            writer.flush();
        } catch (IOException ex) {
            throw new DataStoreException(ex.getMessage(), ex);
        }
    }

    @Override
    public FeatureType getFeatureType() {
        return super.getFeatureType();
    }

    @Override
    public Feature next() throws BackingStoreException {
        try {
            write();
            edited = super.next();
        } catch (BackingStoreException ex) {
            //we reach append mode
            //create empty feature
            edited = featureType.newInstance();
            if (hasIdentifier) {
                edited.setPropertyValue(AttributeConvention.IDENTIFIER_PROPERTY.toString(), idConverter.apply(currentFeatureIdx++));
            }
        }
        return edited;
    }

    public void write(Feature edited) throws BackingStoreException {
        this.edited = edited;
        write();
    }

    public void write() throws BackingStoreException {
        if (edited == null || edited.equals(lastWritten)) {
            return;
        }

        lastWritten = edited;
        try {
            writer.writeFeature(edited);
            writer.flush();
        } catch (IOException | IllegalArgumentException e) {
            throw new BackingStoreException(e.getMessage(), e);
        }
    }

    @Override
    public void remove() {
        edited = null;
    }

    @Override
    public void close() {
        try (GeoJSONWriter toClose = writer) {
            toClose.writeEndFeatureCollection();
            toClose.flush();
        } catch (IOException ex) {
            throw new BackingStoreException(ex);
        } finally {
            super.close();
        }

        //flip files
        rwlock.writeLock().lock();
        try {
            Files.move(tmpFile, jsonFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BackingStoreException(ex);
        } finally {
            rwlock.writeLock().unlock();
        }
    }
}
