/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.server;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import java.io.IOException;
import java.security.Principal;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.api.permission.server.AuthorizedSubject;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants;

/**
 * Sets subject attribute into session based on keycloak authentication data.
 *
 * @author Max Shaposhnik (mshaposhnik@redhat.com)
 */
@Singleton
public class KeycloakEnvironmentInitalizationFilter extends AbstractKeycloakFilter {

  private final KeycloakUserManager userManager;
  private final RequestTokenExtractor tokenExtractor;
  private final PermissionChecker permissionChecker;
  private final KeycloakSettings keycloakSettings;

  @Inject
  public KeycloakEnvironmentInitalizationFilter(
      KeycloakUserManager userManager,
      RequestTokenExtractor tokenExtractor,
      PermissionChecker permissionChecker,
      KeycloakSettings settings) {
    this.userManager = userManager;
    this.tokenExtractor = tokenExtractor;
    this.permissionChecker = permissionChecker;
    this.keycloakSettings = settings;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    final String token = tokenExtractor.getToken(httpRequest);
    if (shouldSkipAuthentication(httpRequest, token)) {
      filterChain.doFilter(request, response);
      return;
    }

    final HttpSession session = httpRequest.getSession();
    Subject subject = (Subject) session.getAttribute("che_subject");
    if (subject == null || !subject.getToken().equals(token)) {
      Jwt jwtToken = (Jwt) httpRequest.getAttribute("token");
      if (jwtToken == null) {
        throw new ServletException("Cannot detect or instantiate user.");
      }
      Claims claims = (Claims) jwtToken.getBody();

      try {
        String username =
            claims.get(
                keycloakSettings.get().get(KeycloakConstants.USERNAME_CLAIM_SETTING), String.class);
        if (username == null) { // fallback to unique id promised by spec
          // https://openid.net/specs/openid-connect-basic-1_0.html#ClaimStability
          username = claims.getIssuer() + ":" + claims.getSubject();
        }
        User user =
            userManager.getOrCreateUser(
                claims.getSubject(), claims.get("email", String.class), username);
        subject =
            new AuthorizedSubject(
                new SubjectImpl(user.getName(), user.getId(), token, false), permissionChecker);
        session.setAttribute("che_subject", subject);
      } catch (ServerException | ConflictException e) {
        throw new ServletException(
            "Unable to identify user " + claims.getSubject() + " in Che database", e);
      }
    }

    try {
      EnvironmentContext.getCurrent().setSubject(subject);
      filterChain.doFilter(addUserInRequest(httpRequest, subject), response);
    } finally {
      EnvironmentContext.reset();
    }
  }

  private HttpServletRequest addUserInRequest(
      final HttpServletRequest httpRequest, final Subject subject) {
    return new HttpServletRequestWrapper(httpRequest) {
      @Override
      public String getRemoteUser() {
        return subject.getUserName();
      }

      @Override
      public Principal getUserPrincipal() {
        return subject::getUserName;
      }
    };
  }
}
