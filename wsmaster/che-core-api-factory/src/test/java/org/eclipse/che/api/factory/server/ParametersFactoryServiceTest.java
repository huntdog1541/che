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

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.jayway.restassured.RestAssured.given;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.valueOf;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static org.eclipse.che.api.factory.server.ParametersFactoryService.NO_RESOLVER_AVAILABLE;
import static org.eclipse.che.api.factory.server.ParametersFactoryService.VALIDATE_QUERY_PARAMETER;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Validate operations performed by the Factory parameters service
 *
 * @author Florent Benoit
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class ParametersFactoryServiceTest {

    /**
     * Instance for creating the DTO objects.
     */
    private static final DtoFactory DTO_FACTORY = DtoFactory.getInstance();

    /**
     * Path of the REST service.
     */
    private final String SERVICE_PATH_PARAMETERS = "/factory/parameters/";

    /**
     * Do not need to check if validator is working or not, so mock it.
     */
    @Mock
    private FactoryAcceptValidator acceptValidator;

    /**
     * Mock for generating links of the factory
     */
    @Mock
    private LinksHelper linksHelper;

    /**
     * Set of all resolvers available for the factory service.
     */
    @Mock
    private Set<FactoryParametersResolver> factoryParametersResolvers;

    /**
     * Use injection to create a new instance of the factory parameters service using previous mocks.
     */
    @InjectMocks
    private ParametersFactoryService parametersFactoryService;


    /**
     * Check that if no resolver is plugged, we have correct error
     */
    @Test
    public void noResolver() throws Exception {
        Set<FactoryParametersResolver> resolvers = new HashSet<>();
        when(factoryParametersResolvers.stream()).thenReturn(resolvers.stream());

        Map<String, String> map = new HashMap<>();
        // when
        Response response = given().contentType(ContentType.JSON).when().body(map).post(SERVICE_PATH_PARAMETERS);

        // then check we have a not found
        assertEquals(response.getStatusCode(), INTERNAL_SERVER_ERROR.getStatusCode());
        assertEquals(response.getBody().prettyPrint(), NO_RESOLVER_AVAILABLE);
    }


    /**
     * Check that if there is a matching resolver, factory is created
     */
    @Test
    public void matchingResolver() throws Exception {
        Set<FactoryParametersResolver> resolvers = new HashSet<>();
        when(factoryParametersResolvers.stream()).thenReturn(resolvers.stream());
        FactoryParametersResolver dummyResolver = mock(FactoryParametersResolver.class);
        resolvers.add(dummyResolver);

        // create factory
        Factory expectFactory = DTO_FACTORY.createDto(Factory.class).withV("4.0").withName("matchingResolverFactory");

        // accept resolver
        when(dummyResolver.accept(anyMap())).thenReturn(TRUE);
        when(dummyResolver.createFactory(anyMap())).thenReturn(expectFactory);

        // when
        Map<String, String> map = new HashMap<>();
        Response response = given().contentType(ContentType.JSON).when().body(map).post(SERVICE_PATH_PARAMETERS);

        // then check we have a not found
        assertEquals(response.getStatusCode(), OK.getStatusCode());
        Factory responseFactory = DTO_FACTORY.createDtoFromJson(response.getBody().asInputStream(), Factory.class);
        assertNotNull(responseFactory);
        assertEquals(responseFactory.getName(), expectFactory.getName());
        assertEquals(responseFactory.getV(), expectFactory.getV());

        // check we call resolvers
        verify(dummyResolver).accept(anyMap());
        verify(dummyResolver).createFactory(anyMap());
    }


    /**
     * Check that if there is no matching resolver, there is error
     */
    @Test
    public void notMatchingResolver() throws Exception {
        Set<FactoryParametersResolver> resolvers = new HashSet<>();
        when(factoryParametersResolvers.stream()).thenReturn(resolvers.stream());

        FactoryParametersResolver dummyResolver = mock(FactoryParametersResolver.class);
        resolvers.add(dummyResolver);
        FactoryParametersResolver fooResolver = mock(FactoryParametersResolver.class);
        resolvers.add(fooResolver);


        // accept resolver
        when(dummyResolver.accept(anyMap())).thenReturn(FALSE);
        when(fooResolver.accept(anyMap())).thenReturn(FALSE);

        // when
        Map<String, String> map = new HashMap<>();
        Response response = given().contentType(ContentType.JSON).when().body(map).post(SERVICE_PATH_PARAMETERS);

        // then check we have a not found
        assertEquals(response.getStatusCode(), INTERNAL_SERVER_ERROR.getStatusCode());

        // check we never call create factories on resolver
        verify(dummyResolver, never()).createFactory(anyMap());
        verify(fooResolver, never()).createFactory(anyMap());
    }

    /**
     * Check that if there is a matching resolver and other not matching, factory is created
     */
    @Test
    public void onlyOneMatchingResolver() throws Exception {
        Set<FactoryParametersResolver> resolvers = new HashSet<>();
        when(factoryParametersResolvers.stream()).thenReturn(resolvers.stream());

        FactoryParametersResolver dummyResolver = mock(FactoryParametersResolver.class);
        resolvers.add(dummyResolver);
        FactoryParametersResolver fooResolver = mock(FactoryParametersResolver.class);
        resolvers.add(fooResolver);

        // create factory
        Factory expectFactory = DTO_FACTORY.createDto(Factory.class).withV("4.0").withName("matchingResolverFactory");

        // accept resolver
        when(dummyResolver.accept(anyMap())).thenReturn(TRUE);
        when(dummyResolver.createFactory(anyMap())).thenReturn(expectFactory);
        when(fooResolver.accept(anyMap())).thenReturn(FALSE);

        // when
        Map<String, String> map = new HashMap<>();
        Response response = given().contentType(ContentType.JSON).when().body(map).post(SERVICE_PATH_PARAMETERS);

        // then check we have a not found
        assertEquals(response.getStatusCode(), OK.getStatusCode());
        Factory responseFactory = DTO_FACTORY.createDtoFromJson(response.getBody().asInputStream(), Factory.class);
        assertNotNull(responseFactory);
        assertEquals(responseFactory.getName(), expectFactory.getName());
        assertEquals(responseFactory.getV(), expectFactory.getV());

        // check we call resolvers
        verify(dummyResolver).accept(anyMap());
        verify(dummyResolver).createFactory(anyMap());
        verify(fooResolver).accept(anyMap());
        verify(fooResolver, never()).createFactory(anyMap());
    }



    /**
     * Check that if there is a matching resolver, that factory is valid
     */
    @Test
    public void checkValidateResolver() throws Exception {
        Set<FactoryParametersResolver> resolvers = new HashSet<>();
        when(factoryParametersResolvers.stream()).thenReturn(resolvers.stream());

        FactoryParametersResolver dummyResolver = mock(FactoryParametersResolver.class);
        resolvers.add(dummyResolver);

        // invalid factory
        String invalidFactoryMessage = "invalid factory";
        doThrow(new BadRequestException(invalidFactoryMessage)).when(acceptValidator).validateOnAccept(any());

        // create factory
        Factory expectFactory = DTO_FACTORY.createDto(Factory.class).withV("4.0").withName("matchingResolverFactory");

        // accept resolver
        when(dummyResolver.accept(anyMap())).thenReturn(TRUE);
        when(dummyResolver.createFactory(anyMap())).thenReturn(expectFactory);

        // when
        Map<String, String> map = new HashMap<>();
        Response response = given().contentType(ContentType.JSON).when().body(map).queryParam(VALIDATE_QUERY_PARAMETER, valueOf(true)).post(SERVICE_PATH_PARAMETERS);

        // then check we have a not found
        assertEquals(response.getStatusCode(), INTERNAL_SERVER_ERROR.getStatusCode());
        assertEquals(response.getBody().prettyPrint(), invalidFactoryMessage);

        // check we call resolvers
        verify(dummyResolver).accept(anyMap());
        verify(dummyResolver).createFactory(anyMap());

        // check we call validator
        verify(acceptValidator).validateOnAccept(any());

    }
}
