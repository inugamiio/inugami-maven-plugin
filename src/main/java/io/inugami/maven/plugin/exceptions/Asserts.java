/* --------------------------------------------------------------------
 *  Inugami  
 * --------------------------------------------------------------------
 * 
 * This program is free software: you can redistribute it and/or modify  
 * it under the terms of the GNU General Public License as published by  
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.inugami.maven.plugin.exceptions;

/**
 * Asserts
 * 
 * @author patrick_guillerm
 * @since 22 juil. 2016
 */
public final class Asserts {

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    private Asserts() {

    }

    // -------------------------------------------------------------------------
    // IS TRUE
    // -------------------------------------------------------------------------
    public static void isTrue(final boolean expression) {
        isTrue("this expression must be true", expression);
    }

    public static void isTrue(final String message, final boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    // -------------------------------------------------------------------------
    // IS FALSE
    // -------------------------------------------------------------------------
    public static void isFalse(final boolean expression) {
        isFalse("this expression must be true", expression);
    }

    public static void isFalse(final String message, final boolean expression) {
        if (expression) {
            throw new IllegalArgumentException(message);
        }
    }

    // -------------------------------------------------------------------------
    // IS NULL
    // -------------------------------------------------------------------------
    public static void isNull(final Object... objects) {
        isNull("objects arguments must be null", objects);
    }

    public static void isNull(final String message, final Object... values) {
        if (values != null) {
            throw new IllegalArgumentException(message);
        }
    }

    // -------------------------------------------------------------------------
    // NOT NULL
    // -------------------------------------------------------------------------
    public static void notNull(final Object... objects) {
        notNull("this argument is required; it must not be null", objects);
    }

    public static void notNull(final String message, final Object... values) {
        if (values == null) {
            throw new IllegalArgumentException(message);
        }
        for (final Object item : values) {
            if (item == null) {
                throw new IllegalArgumentException(message);
            }
        }
    }

    // -------------------------------------------------------------------------
    // NOT EMPTY
    // -------------------------------------------------------------------------
    public static void notEmpty(final String message, final String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    // -------------------------------------------------------------------------
    // EQUALS
    // -------------------------------------------------------------------------
    public static void equals(final Object ref, final Object value) {
        equals(null, ref, value);
    }

    @SuppressWarnings("squid:S1201")
    public static void equals(final String message, final Object ref, final Object value) {
        boolean result = ref == value;

        if (ref != null) {
            result = ref.equals(value);
        }

        if (!result) {
            final String msg = message == null ? "objects must be equals!" : message;
            throw new IllegalArgumentException(msg);
        }
    }
}
