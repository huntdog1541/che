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
package org.eclipse.che.plugin.java.plain.server.generator;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.jdt.core.launching.JREContainerInitializer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_OUTPUT_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.PLAIN_JAVA_PROJECT_ID;

/**
 * Generates new project which contains file with default content.
 *
 * @author Valeriy Svydenko
 */
public class PlainJavaProjectGenerator implements CreateProjectHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PlainJavaProjectGenerator.class);

    private static final String FILE_NAME    = "Main.java";
    private static final String PACKAGE_NAME = "/com/company";

    @Override
    public void onCreateProject(FolderEntry baseFolder,
                                Map<String, AttributeValue> attributes,
                                Map<String, String> options) throws ForbiddenException, ConflictException, ServerException {

        String sourceFolderValue;
        if (attributes.containsKey(SOURCE_FOLDER) && !attributes.get(SOURCE_FOLDER).isEmpty()) {
            sourceFolderValue = attributes.get(SOURCE_FOLDER).getString();
        } else {
            sourceFolderValue = DEFAULT_SOURCE_FOLDER_VALUE;
        }

        baseFolder.createFolder(DEFAULT_OUTPUT_FOLDER_VALUE);
        FolderEntry sourceFolder = baseFolder.createFolder(sourceFolderValue);
        FolderEntry defaultPackage = sourceFolder.createFolder(PACKAGE_NAME);

        defaultPackage.createFile(FILE_NAME, getClass().getClassLoader().getResourceAsStream("files/main_class_content"));

        buildClasspath(baseFolder, sourceFolderValue);
    }

    @Override
    public String getProjectType() {
        return PLAIN_JAVA_PROJECT_ID;
    }

    private void buildClasspath(FolderEntry baseFolder, String sourceFolderValue) throws ServerException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(baseFolder.getPath().toString());
        IJavaProject javaProject = JavaCore.create(project);

        List<IClasspathEntry> classpathEntries = new ArrayList<>();
        //create classpath container for default JRE
        IClasspathEntry jreContainer = JavaCore.newContainerEntry(new Path(JREContainerInitializer.JRE_CONTAINER));
        classpathEntries.add(jreContainer);

        //by default in simple java project sources placed in 'src' folder
        IFolder src = javaProject.getProject().getFolder(sourceFolderValue);
        //if 'src' folder exist add this folder as source classpath entry
        if (src.exists()) {
            IClasspathEntry sourceEntry = JavaCore.newSourceEntry(src.getFullPath());
            classpathEntries.add(sourceEntry);
        }

        try {
            javaProject.setRawClasspath(classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]), null);
        } catch (JavaModelException e) {
            LOG.warn("Can't set classpath for: " + javaProject.getProject().getFullPath().toOSString(), e);
            throw new ServerException(e);
        }
    }
}
