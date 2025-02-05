/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.devtools;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author westbergl
 */
public class FindAndReplaceFile {
    private static Log log = LogFactory.getLog(FindAndReplaceFile.class);

    /**
     * Copy contents of the file from the src to the dest. If the dest exists, it will be deleted first.
     *
     * @param fSrc The source file
     * @param fDst The destination file.
     * @return true if the file was copied.
     * @throws java.io.IOException
     */
    private static boolean copy(File fSrc, File fDst) {
        if (fSrc.exists() == false) {
            return false;
        }
        if (fDst.exists()) {
            // Delete the file first....
            fDst.delete();
        }

        InputStream oIn = null;
        OutputStream oOut = null;
        try {
            oIn = new FileInputStream(fSrc);
            oOut = new FileOutputStream(fDst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int iLen;
            while ((iLen = oIn.read(buf)) > 0) {
                oOut.write(buf, 0, iLen);
            }
        } catch (FileNotFoundException ex) {
            log.error("Failed to find file : " + ex.getMessage());
        } catch (IOException ex) {
            log.error("Failed to read contents of the file : " + fSrc.getName() + ". " + ex.getMessage());
        } finally {
            closeStreamsQuietly(fSrc, oIn);
            closeStreamsQuietly(fDst, oOut);
        }
        return true;
    }

    private static void closeStreamsQuietly(File file, Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException ex) {
            log.error("Failed to close stream on file " + file.getName() + "." + ex.getMessage());
        }
    }

    /**
     * This method looks in the current directory and all subdirectories for the specified file and replaces it with the
     * copy specified.
     *
     * @param fDirToLook The directory to start looking.
     * @param fFileName The path and name of the file to copy.
     * @return The number of replacements that were made.
     * @throws java.lang.Throwable
     */
    public static int searchAndReplace(File fDirToLook, File fFileName) throws Throwable {
        if (fDirToLook == null) {
            return 0;
        }

        if (fDirToLook.isDirectory()) {
            int iCopies = 0;

            File[] faFilesInDir = fDirToLook.listFiles();
            if ((faFilesInDir != null) && (faFilesInDir.length > 0)) {
                for (File fFile : faFilesInDir) {
                    iCopies += searchAndReplace(fFile, fFileName);
                }
            }

            return iCopies;
        } else if (fDirToLook.isFile()) {
            // System.out.println("Debug: fDirToLook.getCanonicalPath()" + fDirToLook.getCanonicalPath());
            if (fDirToLook.getCanonicalPath().equalsIgnoreCase(fFileName.getCanonicalPath())) {
                return 0; // Do not change the file itself...
            } else if (fDirToLook.getName().equalsIgnoreCase(fFileName.getName())) {
                try {
                    boolean bCopied = copy(fFileName, fDirToLook);
                    if (bCopied) {
                        System.out.println("Replaced file: " + fDirToLook.getCanonicalPath());
                        return 1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    System.out.println("Failed to replace file: " + fDirToLook.getCanonicalPath() + " Error: "
                            + e.getMessage());
                }
            }
        }

        return 0;
    }

    /**
     * Print the usage information for this project.
     */
    public static void printUsage() {
        System.out.println("Usage: gov.hhs.fha.nhinc.devtools.FindAndReplaceFile <dir-to-look> <file>");
        System.out.println("Where:");
        System.out.println("<dir-to-look> is the directory where the tool should start looking for the file.");
        System.out.println("              it will search all sub-directories of this one.");
        System.out.println("<file> the path and location of the file that is to be used for the replacement.");
        System.out.println("       the name of this file is also the name of the file that will be replaced.");
    }

    /**
     * Main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            System.exit(-1);
        }

        String sDirToLook = "";
        String sFileName = "";
        sDirToLook = args[0];
        sFileName = args[1];

        File fDirToLook = new File(sDirToLook);
        File fFileName = new File(sFileName);

        try {
            int iCount = searchAndReplace(fDirToLook, fFileName);
            System.out.println("Replaced " + iCount + " copies of this file.");
            System.exit(0);
        } catch (Throwable t) {
            System.out.println("An unexpected exception occurred.  Error: " + t.getMessage());
            t.printStackTrace();
            System.exit(-1);
        }
    }

}
