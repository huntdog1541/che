/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - API
 *   SAP           - initial implementation
 *******************************************************************************/
package org.eclipse.che.git.impl.jgit.ssh;

import com.google.inject.Inject;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.ssh.server.SshServiceClient;
import org.eclipse.che.api.ssh.shared.model.SshPair;
import org.eclipse.che.api.git.GitUrl;

/**
 * Implementation {@link SshKeyProvider} that provides private key
 *
 * @author Igor Vinokur
 */
public class SshKeyProviderImpl implements SshKeyProvider {
    private final SshServiceClient    sshService;

    @Inject
    public SshKeyProviderImpl(SshServiceClient sshService) {
        this.sshService = sshService;
    }

    @Override
    public byte[] getPrivateKey(String url) throws GitException {
        String host = GitUrl.getHost(url);
        SshPair pair;
        try {
            pair = sshService.getPair("git", host);
        } catch (ServerException | NotFoundException e) {
            throw new GitException("Unable get private ssh key", ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY);
        }

        // check keys existence
        String privateKey = pair.getPrivateKey();
        if (privateKey == null) {
            throw new GitException("Unable get private ssh key", ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY);
        }

        return privateKey.getBytes();
    }
}
