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
package org.eclipse.che.filters;

import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;

/**
 * The  class contains commons business logic for all environment workspace id initialization filters. The filters are necessary to set
 * workspace meta information to environment context.
 *
 * @author Dmitry Shnurenko
 */
public abstract class AbstractEnvironmentInitializationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
                                                                                                                 ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest)request;
        Subject subject = new SubjectImpl("che", "che", "dummy_token", false);
        HttpSession session = httpRequest.getSession();
        session.setAttribute("codenvy_user", subject);

        final EnvironmentContext environmentContext = EnvironmentContext.getCurrent();

        try {
            environmentContext.setSubject(subject);
            environmentContext.setWorkspaceId(getWorkspaceId(request));

            filterChain.doFilter(addUserInRequest(httpRequest, subject), response);
        } finally {
            EnvironmentContext.reset();
        }
    }

    /**
     * Extracts workspace id from request.
     *
     * @param request
     *         request which contains workspace id
     * @return workspace id
     */
    protected abstract String getWorkspaceId(ServletRequest request);

    private HttpServletRequest addUserInRequest(final HttpServletRequest httpRequest, final Subject subject) {
        return new HttpServletRequestWrapper(httpRequest) {
            @Override
            public String getRemoteUser() {
                return subject.getUserName();
            }

            @Override
            public Principal getUserPrincipal() {
                return new Principal() {
                    @Override
                    public String getName() {
                        return subject.getUserName();
                    }
                };
            }
        };
    }

    @Override
    public void destroy() {
    }
}
