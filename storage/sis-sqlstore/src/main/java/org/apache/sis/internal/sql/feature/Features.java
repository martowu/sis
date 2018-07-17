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
package org.apache.sis.internal.sql.feature;

import java.util.List;
import java.util.ArrayList;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.reflect.Array;
import org.apache.sis.internal.metadata.sql.SQLBuilder;
import org.apache.sis.util.collection.WeakValueHashMap;
import org.apache.sis.util.collection.BackingStoreException;

// Branch-dependent imports
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;


/**
 * Iterator over feature instances.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 * @module
 */
final class Features implements Spliterator<Feature>, Runnable {
    /**
     * The type of features to create.
     */
    private final FeatureType featureType;

    /**
     * Name of attributes in feature instances, excluding operations and associations to other tables.
     * Those names are in the order of columns declared in the {@code SELECT <columns} statement.
     * This array is a shared instance and shall not be modified.
     */
    private final String[] attributeNames;

    /**
     * Name of the properties where are stored associations in feature instances.
     */
    private final String[] associationNames;

    /**
     * The feature sets referenced through foreigner keys. The length of this array shall be the same as
     * {@link #associationNames} array length. Imported features are index <var>i</var> will be stored in
     * the association named {@code associationNames[i]}.
     */
    private final Features[] importedFeatures;

    /**
     * Zero-based index of the first column to query for each {@link #importedFeatures}.
     * The length of this array shall be one more than {@link #importedFeatures}, with
     * the last value set to the zero-based index after the last column.
     */
    private final int[] foreignerKeyIndices;

    /**
     * If this iterator returns only the feature matching some condition (typically a primary key value),
     * the statement for performing that filtering. Otherwise if this iterator returns all features, then
     * this field is {@code null}.
     */
    private final PreparedStatement statement;

    /**
     * The result of executing the SQL query for a {@link Table}.
     */
    private ResultSet result;

    /**
     * Feature instances already created, or {@code null} if the features created by this iterator are not cached.
     * This map is used when requesting a feature by identifier, not when iterating over all features (note: we
     * could perform an opportunistic check in a future SIS version). The same map may be shared by all iterators
     * on the same {@link Table}, but {@link WeakValueHashMap} already provides the required synchronizations.
     *
     * <p>This {@code Features} class does not require the identifiers to be built from primary key columns.
     * However if this map has been provided by {@link Table#instanceForPrimaryKeys()}, then the identifiers
     * need to be primary keys with columns in the exact same order for allowing the same map to be shared.</p>
     */
    private final WeakValueHashMap<?,Object> instances;

    /**
     * The component class of the keys in the {@link #instances} map, or {@code null} if the keys are not array.
     * For example if a primary key is made of two columns of type {@code String}, then this field may be set to
     * {@code String}.
     */
    private final Class<?> keyComponentClass;

    /**
     * Estimated number of rows, or {@literal <= 0} if unknown.
     */
    private final long estimatedSize;

    /**
     * Creates a new iterator over the feature instances.
     */
    Features(final Table table, final Connection connection, final String[] attributeNames, final String[] attributeColumns,
             final Relation[] importedKeys, final Relation componentOf) throws SQLException
    {
        this.featureType = table.featureType;
        this.attributeNames = attributeNames;
        final DatabaseMetaData metadata = connection.getMetaData();
        estimatedSize = (componentOf == null) ? table.countRows(metadata, true) : 0;
        final SQLBuilder sql = new SQLBuilder(metadata, true).append("SELECT");
        /*
         * Create a SELECT clause with all columns that are ordinary attributes.
         * Order matter, since 'Features' iterator will map the columns to the
         * attributes listed in the 'attributeNames' array in that order.
         */
        int count = 0;
        for (String column : attributeColumns) {
            if (count != 0) sql.append(',');
            sql.append(' ').append(column);
            count++;
        }
        /*
         * Append columns required for all relations to other tables.
         * A column appended here may duplicate a columns appended in above loop
         * if the same column is used both as primary key and foreigner key.
         */
        if (importedKeys != null) {
            final int n = importedKeys.length;
            associationNames    = new String[n];
            importedFeatures    = new Features[n];
            foreignerKeyIndices = new int[n + 1];
            foreignerKeyIndices[0] = count;
            for (int i=0; i<n;) {
                final Relation dependency = importedKeys[i];
                associationNames[i] = dependency.getPropertyName();
                importedFeatures[i] = dependency.getSearchTable().features(connection, dependency);
                for (final String column : dependency.getForeignerKeys()) {
                    if (count != 0) sql.append(',');
                    sql.append(' ').append(column);
                    count++;
                }
                foreignerKeyIndices[++i] = count;
            }
        } else {
            associationNames    = null;
            importedFeatures    = null;
            foreignerKeyIndices = null;
        }
        /*
         * Create a Statement if we don't need any condition, or a PreparedStatement if we need to add
         * a "WHERE" clause. In the later case, we will cache the features already created if there is
         * a possibility that many rows reference the same feature instance.
         */
        sql.append(" FROM ").appendIdentifier(table.schema, table.table);
        if (componentOf == null) {
            statement = null;
            instances = null;       // A future SIS version could use the map opportunistically if it exists.
            keyComponentClass = null;
            result = connection.createStatement().executeQuery(sql.toString());
        } else {
            String separator = " WHERE ";
            for (String primaryKey : componentOf.getSearchColumns()) {
                sql.append(separator).append(primaryKey).append("=?");
                separator = " AND ";
            }
            statement = connection.prepareStatement(sql.toString());
            /*
             * Following assumes that the foreigner key references the primary key of this table,
             * in which case 'table.primaryKeyClass' should never be null. This assumption may not
             * hold if the relation has been defined by DatabaseMetaData.getCrossReference(…) instead.
             */
            if (componentOf.isFullKey()) {
                instances = table.instanceForPrimaryKeys();
                keyComponentClass = table.primaryKeyClass.getComponentType();
            } else {
                instances = new WeakValueHashMap<>(Object.class);       // Can not share the table cache.
                keyComponentClass = Object.class;
            }
        }
    }

    /**
     * Returns an array of the given length capable to hold the identifier,
     * or {@code null} if there is no need for an array.
     */
    private Object identifierArray(final int columnCount) {
        return (columnCount > 1) ? Array.newInstance(keyComponentClass, columnCount) : null;
    }

    /**
     * Declares that this iterator never returns {@code null} elements.
     */
    @Override
    public int characteristics() {
        return NONNULL;
    }

    /**
     * Returns the estimated number of features, or {@link Long#MAX_VALUE} if unknown.
     */
    @Override
    public long estimateSize() {
        return (estimatedSize > 0) ? estimatedSize : Long.MAX_VALUE;
    }

    /**
     * Current version does not support split.
     *
     * @return always {@code null}.
     */
    @Override
    public Spliterator<Feature> trySplit() {
        return null;
    }

    /**
     * Gives the next feature to the given consumer.
     */
    @Override
    public boolean tryAdvance(final Consumer<? super Feature> action) {
        try {
            return fetch(action, false);
        } catch (SQLException e) {
            throw new BackingStoreException(e);
        }
    }

    /**
     * Gives all remaining features to the given consumer.
     */
    @Override
    public void forEachRemaining(final Consumer<? super Feature> action) {
        try {
            fetch(action, true);
        } catch (SQLException e) {
            throw new BackingStoreException(e);
        }
    }

    /**
     * Gives at least the next feature to the given consumer.
     * Gives all remaining features if {@code all} is {@code true}.
     */
    private boolean fetch(final Consumer<? super Feature> action, final boolean all) throws SQLException {
        while (result.next()) {
            final Feature feature = featureType.newInstance();
            for (int i=0; i < attributeNames.length; i++) {
                final Object value = result.getObject(i+1);
                if (!result.wasNull()) {
                    feature.setPropertyValue(attributeNames[i], value);
                }
            }
            if (importedFeatures != null) {
                for (int i=0; i < importedFeatures.length; i++) {
                    final Features dependency = importedFeatures[i];
                    int columnIndice = foreignerKeyIndices[i];
                    final int columnCount = foreignerKeyIndices[i+1] - columnIndice;
                    /*
                     * If the foreigner key uses only one column, we will store the foreigner key value
                     * in the 'key' variable without creating array. But if the foreigner key uses more
                     * than one column, then we need to create an array holding all values.
                     */
                    final Object keys = dependency.identifierArray(columnCount);
                    Object key = null;
                    for (int p=0; p < columnCount;) {
                        key = result.getObject(++columnIndice);
                        if (keys != null) Array.set(keys, p, key);
                        dependency.statement.setObject(++p, key);
                    }
                    if (keys != null) key = keys;
                    final Object value = dependency.fetchReferenced(key);
                    feature.setPropertyValue(associationNames[i], value);
                }
            }
            action.accept(feature);
            if (!all) return true;
        }
        return false;
    }

    /**
     * Executes the current {@link #statement} and stores all features in a list.
     * Returns {@code null} if there is no feature, or returns the feature instance
     * if there is only one such instance, or returns a list of features otherwise.
     */
    private Object fetchReferenced(final Object key) throws SQLException {
        if (key != null) {
            Object existing = instances.get(key);
            if (existing != null) {
                return existing;
            }
        }
        final List<Feature> features = new ArrayList<>();
        try (ResultSet r = statement.executeQuery()) {
            result = r;
            fetch(features::add, true);
        } finally {
            result = null;
        }
        Object feature;
        switch (features.size()) {
            case 0:  feature = null; break;
            case 1:  feature = features.get(0); break;
            default: feature = features; break;
        }
        if (key != null) {
            @SuppressWarnings("unchecked")          // Check is performed by putIfAbsent(…).
            final Object previous = ((WeakValueHashMap) instances).putIfAbsent(key, feature);
            if (previous != null) {
                feature = previous;
            }
        }
        return feature;
    }

    /**
     * Closes the (pooled) connection, including the statements of all dependencies.
     */
    private void close() throws SQLException {
        /*
         * Only one of 'statement' and 'result' should be non-null. The connection should be closed
         * by the 'Features' instance having a non-null 'result' because it is the main one created
         * by 'Table.features(boolean)' method. The other 'Features' instances are dependencies.
         */
        if (statement != null) {
            statement.close();
        }
        final ResultSet r = result;
        if (r != null) {
            result = null;
            final Statement s = r.getStatement();
            try (Connection c = s.getConnection()) {
                r.close();      // Implied by s.close() according JDBC javadoc, but we are paranoiac.
                s.close();
                for (final Features dependency : importedFeatures) {
                    dependency.close();
                }
            }
        }
    }

    /**
     * Closes the (pooled) connection, including the statements of all dependencies.
     * This is a handler to be invoked by {@link java.util.stream.Stream#close()}.
     */
    @Override
    public void run() {
        try {
            close();
        } catch (SQLException e) {
            throw new BackingStoreException(e);
        }
    }
}