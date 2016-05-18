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


import com.google.gson.reflect.TypeToken;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;

import java.util.List;

/**
 * author Alexander Garagatyi
 */
public class MachineConfigsParser {
    public MachineConfigs parse(String configJson) throws ServerException {
        try {
            return new MachineConfigsImpl(JsonHelper.fromJson(configJson, List.class, new TypeToken<List<MachineConfig>>() {}.getType()));
        } catch (JsonParseException e) {
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }
}
