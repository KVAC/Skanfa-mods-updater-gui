/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phoenix.minecraft.plugins.updater.header;

import java.io.File;

/**
 *
 * @author jdcs_dev
 */
public class Header {

    //URL
    public static String protocol = "http";
    public static String protosepar = "://";

    public static String host = "skanfa.theworkpc.com";

    public static String site = protocol + protosepar + host;

    public static String mountDir = site + "/mods/";

    public static String mods = mountDir + "mods/";
    public static String flans = mountDir + "flan/";
    //URL

    //SYSTEM
    public static String OS = (System.getProperty("os.name")).toUpperCase();
    public static String workingDirectory;

    public static File minecarftDir;
    
    public static File minecarftModsDir;
    public static File minecarftFlansDir;

}
