package org.eclipse.che.api.workspace.server.env.spi;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.workspace.Environment;

import java.util.List;
import java.util.Set;

/**
 * Facade for operation with environment.
 * Implements management of environments of a specific type, e.g. docker containers, compose, etc.
 *
 * @author Alexander Garagatyi
 */
public interface EnvironmentManager {

    /**
     * Returns set of environment types supported by an implementation of EnvironmentManager
     */
    Set<String> getSupportedTypes();

    /**
     * Starts environment
     *
     * @param env environment to start
     * @param workspaceId id of workspace provided environment belongs to
     */
    void startEnv(String workspaceId, Environment env);

    /**
     * Starts new machine in existing environment.
     *
     * @throws ForbiddenException when implementation doesn't support start of machine in running env
     * @return started machine
     */
    Machine startMachine(String workspaceId) throws ForbiddenException;

    /**
     * Returns environment as representation of list of {@link org.eclipse.che.api.core.model.machine.Machine}
     *
     * @param workspaceId id of workspace to what requested environment belongs
     * @throws NotFoundException when environment of specified workspace is not found
     */
    List<Machine> getMachines(String workspaceId) throws NotFoundException;

    /**
     * Stop environment of workspace with specified id
     *
     * @param workspaceId id of workspace which environment should be stopped
     * @throws NotFoundException when environment of specified workspace is not found
     */
    void stopEnv(String workspaceId) throws NotFoundException;
}
