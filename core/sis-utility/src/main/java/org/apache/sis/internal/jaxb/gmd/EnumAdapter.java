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
package org.apache.sis.internal.jaxb.gmd;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.opengis.util.ControlledVocabulary;
import org.apache.sis.util.iso.Types;


/**
 * An adapter for {@link Enum}, in order to implement the ISO-19139 standard.
 * Example:
 *
 * {@preformat xml
 *   <srv:direction>
 *     <srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
 *   </srv:direction>
 * }
 *
 * @param <ValueType> The subclass implementing this adapter.
 * @param <BoundType> The enum being adapted.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.5
 * @version 0.6
 * @module
 */
public abstract class EnumAdapter<ValueType extends EnumAdapter<ValueType,BoundType>,
        BoundType extends Enum<BoundType>> extends XmlAdapter<ValueType,BoundType>
{
    /**
     * For subclass constructors.
     */
    protected EnumAdapter() {
    }

    /**
     * Converts the given XML value to an enumeration constant name.
     *
     * @param  value The text in the XML element.
     * @return The presumed enumeration constant name.
     */
    protected static String name(final String value) {
        /*
         * Replace space ! " # $ % & ' ( ) * + , - . / punction characters by '_'.
         * For example this replace "in/out" by "IN_OUT" in ParameterDirection.
         *
         * Note: we do not use codepoint API because this method is mostly for
         * GeoAPI programmatic constant names, which are written in English.
         */
        final int length = value.length();
        final StringBuilder buffer = new StringBuilder(length);
        for (int i=0; i<length; i++) {
            char c = value.charAt(i);
            if (c < '0') {
                c = '_';
            } else if (!Character.isUpperCase(c)) {
                c = Character.toUpperCase(c);
            } else if (i != 0) {
                buffer.append('_');
            }
            buffer.append(c);
        }
        return buffer.toString();
    }

    /**
     * Returns the text to write in the XML element for the given enumeration constant.
     *
     * @param  e The enumeration constant.
     * @return The text to write in the XML element.
     */
    protected static String value(final ControlledVocabulary e) {
        return Types.getCodeName(e);
    }
}