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
package org.eclipse.che.plugin.java.plain.client.wizard.selector;

/**
 * Delegate which handles result of the node selection.
 *
 * @author Valeriy Svydenko
 */
public interface SelectionDelegate {

    /**
     * Fires when some node was selected.
     *
     * @param path
     *         path to the project
     */
    void onNodeSelected(String path);
}
