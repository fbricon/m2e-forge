/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Fred Bricon (Red Hat, Inc.) - initial API and implementation
 ************************************************************************************/
package org.jboss.tools.forge.m2e.tests;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.jdt.internal.launch.MavenRuntimeClasspathProvider;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;

@SuppressWarnings("restriction")
public class ForgeAddonClassifierClasspathProviderTest extends AbstractMavenProjectTestCase {

	public void testForgeAddonClassifier() throws Exception {
		IProject[] projects = importProjects("projects/runtimeclasspath/",
				new String[] { "p01/pom.xml", "addon/pom.xml" },
				new ResolverConfiguration());
		waitForJobsToComplete();
		IProject p01 = projects[0];

		workspace.build(IncrementalProjectBuilder.FULL_BUILD, monitor);

		MavenRuntimeClasspathProvider classpathProvider = new MavenRuntimeClasspathProvider();

		/* check runtime classpath */{
			ILaunchConfiguration configuration = DebugPlugin.getDefault()
					.getLaunchManager()
					.getLaunchConfiguration(p01.getFile("P01-runtime.launch"));
			IRuntimeClasspathEntry[] unresolvedClasspath = classpathProvider
					.computeUnresolvedClasspath(configuration);
			IRuntimeClasspathEntry[] resolvedClasspath = classpathProvider
					.resolveClasspath(unresolvedClasspath, configuration);
			IRuntimeClasspathEntry[] userClasspath = getUserClasspathEntries(resolvedClasspath);

			assertEquals(
					"Invalid runtime classpath :"
							+ Arrays.asList(userClasspath).toString(), 2,
					userClasspath.length);
			assertEquals(new Path("/p01/target/classes"),
					userClasspath[0].getPath());
			assertEquals(new Path("/addon/target/classes"),
					userClasspath[1].getPath());
		}

		/* check test classpath */{
			ILaunchConfiguration configuration = DebugPlugin.getDefault()
					.getLaunchManager()
					.getLaunchConfiguration(p01.getFile("P01-test.launch"));
			IRuntimeClasspathEntry[] unresolvedClasspath = classpathProvider
					.computeUnresolvedClasspath(configuration);
			IRuntimeClasspathEntry[] resolvedClasspath = classpathProvider
					.resolveClasspath(unresolvedClasspath, configuration);
			IRuntimeClasspathEntry[] userClasspath = getUserClasspathEntries(resolvedClasspath);

			assertEquals(
					"Invalid test classpath :"
							+ Arrays.asList(userClasspath).toString(), 3,
					userClasspath.length);
			assertEquals(new Path("/p01/target/test-classes"),
					userClasspath[0].getPath());
			assertEquals(new Path("/p01/target/classes"),
					userClasspath[1].getPath());
			assertEquals(new Path("/addon/target/classes"),
					userClasspath[2].getPath());
		}
	}

	private IRuntimeClasspathEntry[] getUserClasspathEntries(
			IRuntimeClasspathEntry[] entries) {
		ArrayList<IRuntimeClasspathEntry> result = new ArrayList<IRuntimeClasspathEntry>();
		for (IRuntimeClasspathEntry entry : entries) {
			if (IRuntimeClasspathEntry.USER_CLASSES == entry
					.getClasspathProperty()) {
				result.add(entry);
			}
		}
		return result.toArray(new IRuntimeClasspathEntry[result.size()]);
	}
}
