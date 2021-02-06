package com.evilcorp.settings;

import java.util.Optional;

/**
 * Abstraction to represent named
 * software settings.
 */
public interface SoftSettings {
    /**
     * Find setting and return it.
     * Should never throw.
     * @param name setting name. Plain unstructured string.
     * @return setting value in string form.
     */
    Optional<String> setting(String name);
}
