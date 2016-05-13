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
package org.eclipse.che.api.factory.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.shared.dto.Factory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * REST Service providing factories based on parameters.
 * New resolvers can be plugged to add and leverage features
 * @author Florent Benoit
 */
@Api(value = "/factory/parameters",
     description = "Factory service handling creation of factories using parameters")
@Path("/factory/parameters")
public class ParametersFactoryService {

    /**
     * If there is no resolver
     */
    public static final String NO_RESOLVER_AVAILABLE = "Cannot build factory with any of the provided parameters.";

    /**
     * If there is no parameter.
     */
    public static final String NO_PARAMETERS = "Missing parameters";

    /**
     * Validate query parameter. If true, factory will be validated
     */
    public static final String VALIDATE_QUERY_PARAMETER = "validate";

    /**
     * Set of resolvers for factories.
     */
    @Inject
    private Set<FactoryParametersResolver> factoryParametersResolvers;

    /**
     * Handling acceptance and validate factory content.
     */
    @Inject
    private FactoryAcceptValidator acceptValidator;

    /**
     * Generator of links for factory.
     */
    @Inject
    private LinksHelper linksHelper;


    /**
     * Build a factory for provided parameters
     *
     * @param parameters
     *         map of key/values used to build factory.
     * @param uriInfo
     *         url context
     * @return a factory instance if found a matching resolver
     * @throws NotFoundException
     *         when no resolver can be used
     * @throws ServerException
     *         when any server errors occurs
     * @throws BadRequestException
     *         when the factory is invalid e.g. is expired
     */
    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Create factory by providing map of parameters",
                  notes = "Get JSON with factory information.")
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 400, message = "Failed to validate factory"),
                   @ApiResponse(code = 500, message = "Internal server error")})
    public Factory getFactory(
            @ApiParam(value = "Parameters provided to create factories")
            final Map<String, String> parameters,
            @ApiParam(value = "Whether or not to validate values like it is done when accepting a Factory",
                      allowableValues = "true,false",
                      defaultValue = "false")
            @DefaultValue("false")
            @QueryParam(VALIDATE_QUERY_PARAMETER)
            final Boolean validate,
            @Context
            final UriInfo uriInfo) throws NotFoundException, ServerException, BadRequestException {

        // Check parameter
        if (parameters == null) {
            throw new BadRequestException(NO_PARAMETERS);
        }

        // search matching resolver
        Optional<FactoryParametersResolver> factoryParametersResolverOptional = this.factoryParametersResolvers.stream().filter((resolver -> resolver.accept(parameters))).findFirst();

        // no match
        if (!factoryParametersResolverOptional.isPresent()) {
            throw new NotFoundException(NO_RESOLVER_AVAILABLE);
        }

        // create factory from matching resolver
        final Factory factory = factoryParametersResolverOptional.get().createFactory(parameters);

        // Apply links
        try {
            factory.setLinks(linksHelper.createLinks(factory, uriInfo, null));
        } catch (UnsupportedEncodingException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }

        // time to validate the factory
        if (validate) {
            acceptValidator.validateOnAccept(factory);
        }

        return factory;
    }
}
