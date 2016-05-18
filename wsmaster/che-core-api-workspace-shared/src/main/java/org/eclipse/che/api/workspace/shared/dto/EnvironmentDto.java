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
package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.dto.shared.DTO;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface EnvironmentDto extends Environment {

    EnvironmentDto withName(String name);

    void setName(String name);

    EnvironmentDto withType(String type);

    void setType(String type);

    EnvironmentDto withConfig(String config);

    void setConfig(String config);

    // TODO what to do with factories?

    // TODO what to do with this method?
//    @DelegateTo(client = @DelegateRule(type = DevMachineResolver.class, method = "getDevMachine"),
//                server = @DelegateRule(type = DevMachineResolver.class, method = "getDevMachine"))
//    MachineConfigDto devMachine();

//    class DevMachineResolver {
//        public static MachineConfigDto getDevMachine(EnvironmentDto environmentDto) {
//            for (MachineConfigDto machineConfigDto : environmentDto.getMachineConfigs()) {
//                if (machineConfigDto.isDev()) {
//                    return machineConfigDto;
//                }
//            }
//            return null;
//        }
//    }
}
