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
package org.apache.sis.coverage.grid;

import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.apache.sis.referencing.operation.transform.MathTransforms;
import org.apache.sis.referencing.operation.matrix.Matrix3;
import org.apache.sis.referencing.operation.matrix.Matrix4;
import org.apache.sis.test.TestCase;
import org.junit.Test;

import static org.opengis.test.Assert.*;


/**
 * Tests {@link PixelTranslation}.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @version 1.0
 * @since   1.0
 * @module
 */
public final strictfp class PixelTranslationTest extends TestCase {
    /**
     * Returns a transform from center to corner with the given number of dimensions.
     */
    private static MathTransform centerToCorner(final int dimension) {
        return PixelTranslation.translate(MathTransforms.identity(dimension), PixelInCell.CELL_CENTER, PixelInCell.CELL_CORNER);
    }

    /**
     * Returns a transform from center to corner in the two-dimensional case.
     */
    private static MathTransform centerToCorner2D() {
        return PixelTranslation.translate(MathTransforms.identity(2), PixelOrientation.CENTER, PixelOrientation.UPPER_LEFT, 0, 1);
    }

    /**
     * Tests {@link PixelTranslation#translate(MathTransform, PixelInCell, PixelInCell)}.
     */
    @Test
    public void testTranslatePixelInCell() {
        final MathTransform mt = centerToCorner(3);
        assertMatrixEquals("center → corner", new Matrix4(
                1, 0, 0, -0.5,
                0, 1, 0, -0.5,
                0, 0, 1, -0.5,
                0, 0, 0,  1), MathTransforms.getMatrix(mt), STRICT);
    }

    /**
     * Tests {@link PixelTranslation#translate(MathTransform, PixelOrientation, PixelOrientation, int, int)}.
     */
    @Test
    public void testTranslatePixelOrientation() {
        MathTransform mt = centerToCorner2D();
        assertMatrixEquals("center → corner", new Matrix3(
                1, 0, -0.5,
                0, 1, -0.5,
                0, 0,  1), MathTransforms.getMatrix(mt), STRICT);

        mt = PixelTranslation.translate(MathTransforms.identity(3), PixelOrientation.LOWER_LEFT, PixelOrientation.CENTER, 1, 2);
        assertMatrixEquals("center → corner", new Matrix4(
                1, 0, 0,  0.0,
                0, 1, 0, +0.5,
                0, 0, 1, -0.5,
                0, 0, 0,  1), MathTransforms.getMatrix(mt), STRICT);
    }

    /**
     * Verifies consistency of cached transforms when using translations inferred from {@link PixelInCell}.
     */
    @Test
    public void testCache() {
        MathTransform previous = null;
        for (int dimension = 1; dimension <= 5; dimension++) {
            final MathTransform mt = centerToCorner(dimension);
            assertNotSame("Transforms with different number of dimensions.", previous, mt);
            assertSame("Transforms with same number of dimensions", mt, centerToCorner(dimension));
            previous = mt;
        }
    }

    /**
     * Verifies consistency of cached transforms when using translations inferred from {@link PixelOrientation}.
     */
    @Test
    public void testCache2D() {
        assertSame(centerToCorner(2), centerToCorner2D());
        assertSame(PixelTranslation.translate(MathTransforms.identity(2), PixelInCell.CELL_CORNER, PixelInCell.CELL_CENTER),
                   PixelTranslation.translate(MathTransforms.identity(2), PixelOrientation.UPPER_LEFT, PixelOrientation.CENTER, 0, 1));
    }
}