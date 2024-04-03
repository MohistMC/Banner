package com.mohistmc.banner;

import java.util.Locale;

/**
 * @author Mgazul by MohistMC
 * @date 2023/7/26 21:51:45
 */
public class BannerGUI {

    public static void main(String[] s) {
        String languageTag = Locale.getDefault().getLanguage();
        if (languageTag.equals("zh") || languageTag.startsWith("zh-")) {
            System.out.println("请将Banner放入mods文件夹并运行Fabric服务器");
            System.out.println("请将Banner放入mods文件夹并运行Fabric服务器");
            System.out.println("请将Banner放入mods文件夹并运行Fabric服务器");
        } else {
            System.out.println("Please put Banner into mods folder and run Fabric server");
            System.out.println("Please put Banner into mods folder and run Fabric server");
            System.out.println("Please put Banner into mods folder and run Fabric server");
        }
    }
}
