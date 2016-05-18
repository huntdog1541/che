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
package org.eclipse.che.api.user.server;

import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for {@link UserManager}
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Listeners(MockitoTestNGListener.class)
public class UserManagerTest {

    @Mock
    private UserDao            userDao;
    @Mock
    private ProfileManager     profileManager;
    @Mock
    private PreferencesManager preferencesManager;
    @InjectMocks
    private UserManager        manager;

    @Test
    public void shouldCreateProfileAndPreferencesOnUserCreation() throws Exception {
        final UserImpl user = new UserImpl(null, "test@email.com", "testName", null, null);
        manager.create(user, false);

        verify(profileManager).create(any(Profile.class));
        verify(preferencesManager).save(anyString(), anyMapOf(String.class, String.class));
    }

    @Test
    public void shouldGeneratedPasswordWhenCreatingUserAndItIsMissing() throws Exception {
        final User user = new UserImpl(null, "test@email.com", "testName", null, null);
        manager.create(user, false);

        final ArgumentCaptor<UserImpl> userCaptor = new ArgumentCaptor<>();
        verify(userDao).create(userCaptor.capture());
        assertNotNull(userCaptor.getValue().getPassword());
    }
}
