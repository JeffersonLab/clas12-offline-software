package org.jlab.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static ArrayList<String> filesInFolder(File folder, String ext)
    {
        ArrayList<String> file_list = new ArrayList<String>();
        for (File f : folder.listFiles())
        {
            if (f.isDirectory())
            {
                file_list.addAll(FileUtils.filesInFolder(f, ext));
            }
            else
            {
                String[] file_split = f.getName().split("[.]+");
                String file_ext = file_split[file_split.length - 1];
                if (file_ext.equalsIgnoreCase(ext))
                {
                    file_list.add(f.getAbsolutePath());
                }
            }
        }
        return file_list;
    }
    
    public static List<String> dirListStartsWith(File folder, String starts)
    {
        List<String> file_list = new ArrayList<String>();
        for (File f : folder.listFiles())
        {
            if (f.isDirectory())
            {
                //file_list.addAll(FileUtils.dirListStartsWith(folder, starts));
            }
            else
            {          
                if (f.getName().startsWith(starts)==true)
                {
                    file_list.add(f.getAbsolutePath());
                }
            }
        }
        return file_list;
    }
    
    public static List<String> dirListStartsWith(String dir, String starts){
        File f = new File(dir);
        if(f==null) return new ArrayList<String>();
        return FileUtils.dirListStartsWith(f, starts);
    }
    
    public static ArrayList<String> filesInFolder(File folder, String ext, ArrayList<String> ignore_prefixes)
    {
        ArrayList<String> file_list = new ArrayList<String>();
        for (File f : folder.listFiles())
        {
            if (f.isDirectory())
            {
                file_list.addAll(FileUtils.filesInFolder(f, ext));
            }
            else
            {
                String[] file_split = f.getName().split("[.]+");
                String file_ext = file_split[file_split.length - 1];
                if (file_ext.equalsIgnoreCase(ext))
                {
                	boolean ignore = false;
                	for (String p : ignore_prefixes)
                	{
                		if (f.getName().startsWith(p))
                		{
                			ignore = true;
                			break;
                		}
                	}
                	if (!ignore)
                	{
                		file_list.add(f.getAbsolutePath());
                	}
                }
            }
        }
        return file_list;
    }
}
