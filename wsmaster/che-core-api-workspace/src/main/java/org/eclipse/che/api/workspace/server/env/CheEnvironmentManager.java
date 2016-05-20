package org.eclipse.che.api.workspace.server.env;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.workspace.server.env.spi.EnvironmentManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Alexander Garagatyi
 */
public class CheEnvironmentManager implements EnvironmentManager {
    private static Set<String> supportedTypes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("che-docker")));

    private final MachineManager machineManager;

    public CheEnvironmentManager(MachineManager machineManager) {
        this.machineManager = machineManager;
    }

    @Override
    public Set<String> getSupportedTypes() {
        return supportedTypes;
    }

    @Override
    public void startEnv(String workspaceId, Environment env) {
        //todo check that there is no other env for ws
        if (env.getMachineConfigs() != null) {
            // start old env
        } else {
            // start new env
        }
    }

    @Override
    public Machine startMachine(String workspaceId) throws ForbiddenException {
        // todo implement
        return null;
    }

    @Override
    public List<Machine> getMachines(String workspaceId) throws NotFoundException {
        // todo
        return null;
    }

    @Override
    public void stopEnv(String workspaceId) throws NotFoundException {
        // todo
    }


}
