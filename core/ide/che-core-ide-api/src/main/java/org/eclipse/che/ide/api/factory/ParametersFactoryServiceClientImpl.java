/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.factory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.json.JsonHelper.toJson;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

/**
 * Implementation of {@link ParametersFactoryServiceClient} service.
 *
 * @author Florent Benoit
 */
@Singleton
public class ParametersFactoryServiceClientImpl implements ParametersFactoryServiceClient {

    /**
     * Path to the factory parameters service
     */
    protected static final String FACTORY_PARAMETERS_PATH = "/api/factory/parameters";

    @Inject
    private AsyncRequestFactory asyncRequestFactory;

    /**
     * Get factory object based on user parameters
     *
     * @param factoryParameters
     *         map containing factory data parameters provided through URL
     * @param validate
     *         indicates whether or not factory should be validated by accept validator
     * @param callback
     */
    @Override
    public void getFactory(@NotNull final Map<String, String> factoryParameters, boolean validate,
                           @NotNull final AsyncRequestCallback<Factory> callback) {

        // Init string with JAX-RS path
        StringBuilder url = new StringBuilder(FACTORY_PARAMETERS_PATH);

        // If validation, needs to add the flag
        if (validate) {
            url.append("?validate=true");
        }
        asyncRequestFactory.createPostRequest(url.toString(), toJson(factoryParameters)).header(ACCEPT, APPLICATION_JSON)
                           .send(callback);
    }
}
