/*
 *    Copyright (c) 2021 James Ahlborn
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package jde.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A ClassPathEntry that represents the classes in the various modules
 * included with jdk 9+ which can be read using the "jimage" executable.
 */
public class JImageList extends ClassPathEntry {

  private static final String[] KEEP_CLASSES_PREFIXES = {
    "java/",
    "javax/",
    "org/"
  };

  private final File jimageBin;
  private final File modsFile;

  JImageList() {
    File javaHome = new File(System.getProperty("java.home"));
    jimageBin = new File(javaHome, "bin/jimage");
    modsFile = new File(javaHome, "lib/modules");
  }

  @Override
  protected void load() throws IOException {

    if(jimageBin.canExecute() && modsFile.canRead()) {
      try {
        readJImageClasses();
      } catch(Exception e) {
        e.printStackTrace(System.err);
      }
    }

    setLoaded(true);
  }

  private void readJImageClasses() throws Exception {
    Process proc = Runtime.getRuntime().exec(new String[]{
        jimageBin.getAbsolutePath(),
        "list",
        modsFile.getAbsolutePath()});

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(
          new InputStreamReader(proc.getInputStream()));

      String line = null;
      while((line = reader.readLine()) != null) {
        line = line.trim();
        if(!line.endsWith(".class")) {
          continue;
        }

        // strip off class suffix
        line = line.substring(0, line.length() - ".class".length());

        boolean keeper = false;
        for(String prefix : KEEP_CLASSES_PREFIXES) {
          if(line.startsWith(prefix)) {
            keeper = true;
            break;
          }
        }
        if(!keeper) {
          continue;
        }

        // ignore classes like java.bar.Foo$1
        if(isInnerNumberClass(line)) {
          continue;
        }

        line = line.replace('/', '.');
        addClass(line);
      }

    } finally {
      if(reader != null) {
        reader.close();
      }
      proc.destroy();
    }
  }

  private static boolean isInnerNumberClass(String className) {
    int idx = className.lastIndexOf('$');
    if(idx < 0) {
      return false;
    }
    char nestedClassNameChar = className.charAt(idx + 1);
    return Character.isDigit(nestedClassNameChar);
  }
}
