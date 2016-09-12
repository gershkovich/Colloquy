/*
 * Copyright (c) 2016. Tatyana Gershkovich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.colloquy.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Peter Gershkovich on 12/3/15.
 */
public class Unzip
{

    /**
     * Unzip it
     *
     * @param zipFile   input zip file
     * @param outputDid zip file output directory
     */

    public static void unZipIt(String zipFile, String outputDid)
    {

        byte[] buffer = new byte[1024];

        try
        {
            //create output directory is not exists
            File folder = new File(outputDid);
            if (!folder.exists())
            {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null)
            {

                String fileName = ze.getName();
                File newFile = new File(outputDid + File.separator + fileName);

                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0)
                {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            System.out.println("Finished file extraction.");

        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }


    public static void unZipFilesFromTo(String fromDirectory, String toDirectory, String extention) throws IOException
    {

        Path path = FileSystems.getDefault().getPath(fromDirectory);

        Path dir =  FileSystems.getDefault().getPath(toDirectory);


        if (!Files.isDirectory(dir))
        {
            Files.createDirectory(dir);
        }


        List<Path> result = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.{"+ extention + "}"))
        {
            for (Path entry : stream)
            {
                result.add(entry);
            }
        } catch (DirectoryIteratorException | IOException ex)
        {
            // I/O error encounted during the iteration, the cause is an IOException
            ex.printStackTrace();

        }

        int counter = 0;

        for (Path p : result)
        {

            Unzip.unZipIt(p.toString(), toDirectory +
                    (toDirectory.endsWith(File.separator) ? "" : File.separator)
                    + p.getFileName().toString().replace("." + extention, ""));

            counter++;
        }

        System.out.println("Unzipped: " + counter + " file(s)");


    }


}
