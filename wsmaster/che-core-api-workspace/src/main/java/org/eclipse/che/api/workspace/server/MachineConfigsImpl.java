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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * author Alexander Garagatyi
 */
public class MachineConfigsImpl implements MachineConfigs {
    private List<MachineConfigImpl> machineConfigs;

    public MachineConfigsImpl(List<MachineConfig> machineConfigs) {
        Objects.requireNonNull(machineConfigs);
        this.machineConfigs = machineConfigs.stream()
                                       .map(MachineConfigImpl::new)
                                       .collect(Collectors.toList());
    }

    @Override
    public List<? extends MachineConfig> getMachineConfigs() {
        return machineConfigs;
    }
}
