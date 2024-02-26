package com.example;

import dev.mattsturgeon.assets.Assets;
import dev.mattsturgeon.lang.Language;

import java.io.File;

public class Example {
    public static void main(String... args) {
        File assetsFile = new File("assets");
        Assets assets = Assets.fromMinecraftAssets(assetsFile);
        Language language = assets.getLanguage("en_us");
        if (language == null) {
            System.err.println("Can't find en_us language");
            System.exit(1);
        }
        String text = language.get("some.translation");
        System.out.println("some.translation: " + text);
    }
}
