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
package org.apache.sis.internal.geojson;

import com.fasterxml.jackson.core.JsonLocation;

import java.util.Objects;

/**
 * Lightweight pojo of {@link JsonLocation} without internal source object
 * reference and offset. Because since 2.3.x+ of jackson byteOffset and
 * charOffset values depend of underling source type. (InputStream to use
 * byteOffset, BufferedReader to use charOffset)
 *
 * @author Quentin Boileau (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @version 2.0
 * @since   2.0
 * @module
 */
public class LiteJsonLocation {

    private final int lineNr;
    private final int columnNr;

    public LiteJsonLocation(JsonLocation location) {
        this.lineNr = location.getLineNr();
        this.columnNr = location.getColumnNr();
    }

    public int getLineNr() {
        return lineNr;
    }

    public int getColumnNr() {
        return columnNr;
    }

    /**
     * Check if an JsonLocation position (line and column) is before current
     * LiteJsonLocation.
     *
     * @param o JsonLocation
     * @return true if before and false if input JsonLocation is equals or after
     * current LiteJsonLocation
     */
    public boolean isBefore(JsonLocation o) {
        if (o == null) {
            return false;
        }
        LiteJsonLocation that = new LiteJsonLocation(o);

        return lineNr < that.lineNr || (lineNr == that.lineNr && columnNr < that.columnNr);
    }

    /**
     * Test equality with LiteJsonLocation and JsonLocation input objects
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        // not equals if o is null or not an instance of LiteJsonLocation or JsonLocation
        if (o == null
                || (!LiteJsonLocation.class.isAssignableFrom(o.getClass())
                && !JsonLocation.class.isAssignableFrom(o.getClass()))) {
            return false;
        }

        LiteJsonLocation that;
        if (JsonLocation.class.isAssignableFrom(o.getClass())) {
            that = new LiteJsonLocation((JsonLocation) o);
        } else {
            that = (LiteJsonLocation) o;
        }

        return lineNr == that.lineNr && columnNr == that.columnNr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNr, columnNr);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LiteJsonLocation{");
        sb.append("lineNr=").append(lineNr);
        sb.append(", columnNr=").append(columnNr);
        sb.append('}');
        return sb.toString();
    }
}
