/*
 * Copyright (C) 2001, 2003, 2004 Eric D. Friedman (eric@hfriedman.rdsl.lmi.net)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * Created: Tue Aug 14 19:24:02 2001
 */

package jde.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A class that represents a set of classpath entries for a project.
 * All projects include the entries found in boot.class.path and in
 * the various extension directories.  Classpath entries are shared
 * across projects and so are not loaded/copied more than once.
 *
 * @author Eric D. Friedman
 * @version $Id: ProjectClasses.java,v 1.8 2004/11/25 05:23:20 jslopez Exp $
 */

class ProjectClasses {

  private String classPath;

  // the boot classpath is loaded at startup.
  private static ArrayList<ClassPathEntry> bootClassPathEntries = new ArrayList<ClassPathEntry>();

  static {
    try {
      loadBootClassPathEntries();
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
  }

  // take a shallow clone of the boot classpath as the starting
  // point for the project classpath.
  private List<ClassPathEntry> classPathEntries = (List<ClassPathEntry>) bootClassPathEntries.clone();

  /**
   * Creates a new <code>ProjectClasses</code> instance.
   *
   * @param classPath the project classpath
   * @exception IOException if an error occurs
   */
  ProjectClasses(String classPath) throws IOException {
    this.classPath = classPath;

    StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);

    while (st.hasMoreTokens()) {
      String classPathEntry = st.nextToken();
      File classPathFile = new File(classPathEntry);
      if (classPathFile.exists()) {
        ClassPathEntry cpe = ClassPathEntry.instanceForEntry(classPathFile);
        if (cpe != null) {
          classPathEntries.add(cpe);
        }
      }
    }
  }

  /**
   * Reload classes in a single classpath entry, which may be a class,
   * jar, or zip file.
   *
   * @param classPathEntry Classpath entry to be reloaded.
   * @exception IOException if an error occurs
   */
  void reloadClasses(String classPathEntry) throws IOException {
    File classPathFile = new File(classPathEntry);
    if (classPathFile.exists()) {
      ClassPathEntry cpe = ClassPathEntry.instanceForEntry(classPathFile);
      if (cpe != null) {
        cpe.reload();
      }
    }
  }

  /**
   * Reload all classes in classpath.
   *
   * @exception IOException if an error occurs
   */
  void reloadClasses() throws IOException {
    for (ClassPathEntry cpe : classPathEntries) {
      cpe.reload();
    }
  }

  /**
   * Get the classpath represented by this project.
   *
   * @return a <code>String</code> value
   */
  public String getClassPath() {
    return classPath;
  }

  /**
   * Get fully qualified classnames in this classpath that
   * matching the specified unqualified name.
   *
   * @param unqualifiedName Unqualified name for which matching
   * qualified names are to be found.
   * @return list of qualified names.
   * @exception IOException if an error occurs
   */
  List<String> getClassNames(String unqualifiedName) throws IOException {
    List<String> rv = new ArrayList<String>();
    for (ClassPathEntry cpe : classPathEntries) {
      @SuppressWarnings("unchecked")
      List<String> classNames = (List<String>) cpe.getClassNames(unqualifiedName);
      for (String className : classNames) {
        if (!rv.contains(className)) {
          rv.add(className);
        }
      }
    }
    return rv;
  }

  @Override
  public String toString() {
    return classPathEntries.toString();
  }

  public static void main (String[] args) throws Exception {
    System.out.println(new ProjectClasses(System.getProperty("java.class.path")).getClassNames(args[0]));
  }

  /**
   * loads the boot classpath entries.
   *
   * @exception IOException if an error occurs
   */
  static void loadBootClassPathEntries() throws IOException {
    String bootClassPath = System.getProperty("sun.boot.class.path");
    String extDirs = System.getProperty("java.ext.dirs");

    if (bootClassPath != null) {
      StringTokenizer st = new StringTokenizer(bootClassPath, File.pathSeparator);
      while (st.hasMoreTokens()) {
        File file = new File(st.nextToken());
        addToBootClassPath(file);
      }
    }

    if (extDirs != null) {
      StringTokenizer st = new StringTokenizer(extDirs,File.pathSeparator);

      // Iterate through extension directories
      while (st.hasMoreTokens()) {
        File[] extFiles = new File(st.nextToken()).listFiles();

        if (extFiles != null) {
          // Iterate through files added them to classPath
          for (int i = 0; i < extFiles.length; i++) {
            addToBootClassPath(extFiles[i]);
          }
        }
      }
    }

    // Also try the file listing the built-in classes for JDE 9+
    bootClassPathEntries.add(new ClassList());

    // read class names from java 9+ modules
    bootClassPathEntries.add(new JImageList());
  }

  static void addToBootClassPath(File file) throws IOException {
    if (file.exists()) {
      ClassPathEntry entry = ClassPathEntry.instanceForEntry(file);
      if (null != entry) {
        bootClassPathEntries.add(new ImmutableClassPathEntry(entry));
      }
    }
  }

}

/*
 * $Log: ProjectClasses.java,v $
 * Revision 1.8  2004/11/25 05:23:20  jslopez
 * Fixes getClassNames to avoid adding duplicate entries.
 *
 * Revision 1.7  2004/10/20 06:14:53  paulk
 * Update to support reloading classes from a single classpath entry. Thanks to Martin Schwamberger.
 *
 * Revision 1.6  2003/06/18 10:12:53  paulk
 * Fixed null pointer exception caused by insertion of null entries
 * into classPathEntries for files that are not class files or zip or jar files.
 * Thanks to Boyan Slavoff for reporting this problem.
 *
 * Revision 1.5  2002/02/21 12:24:32  jslopez
 * Adds method getClassPath.
 *
 * Revision 1.4  2001/10/17 04:03:37  paulk
 * Cosmetic changes to fit JDE coding style.
 *
 *
 */
