package org.eclipse.che.api.core.model.user;

import java.util.Map;

/**
 * Defines the user's profile model.
 *
 * @author Yevhenii Voevodin
 */
public interface Profile {

    /**
     * Returns the identifier of the user {@link User#getId()}
     * whom this profile belongs to, which must be also considered
     * as profile identifier.
     */
    String getId();

    /**
     * Returns the email of the user {@link User#getEmail()} whom this
     * profile belongs to. The email value is updatable by the {@link User} object.
     */
    String getEmail();

    /**
     * Returns the user profile attributes (e.g. job title).
     */
    Map<String, String> getAttributes();
}
