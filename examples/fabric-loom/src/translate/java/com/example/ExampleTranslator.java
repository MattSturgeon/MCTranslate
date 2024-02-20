package com.example;

import dev.mattsturgeon.assets.Assets;
import dev.mattsturgeon.dev.mattsturgeon.lang.Language;
import dev.mattsturgeon.dev.mattsturgeon.minecraft.LanguageInfo;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExampleTranslator {
    public static void main(String... args) {
        // Declare argument specs
        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        ArgumentAcceptingOptionSpec<String> langSpec = optionParser
                .accepts("lang")
                .withRequiredArg()
                .defaultsTo("en_us");
        ArgumentAcceptingOptionSpec<String> assetIndexSpec = optionParser
                .accepts("assetIndex")
                .withRequiredArg();
        ArgumentAcceptingOptionSpec<File> mcJarSpec = optionParser
                .accepts("mcJar")
                .withRequiredArg()
                .ofType(File.class);
        ArgumentAcceptingOptionSpec<File> assetsDirSpec = optionParser
                .accepts("assetsDir")
                .withRequiredArg()
                .ofType(File.class);
        ArgumentAcceptingOptionSpec<File> modAssetsDirSpec = optionParser
                .accepts("modAssetsDir")
                .withRequiredArg()
                .ofType(File.class);

        // Parse arguments
        OptionSet options = optionParser.parse(args);
        String lang = options.valueOf(langSpec);
        File mcJar = options.valueOf(mcJarSpec);
        File assetDir = options.valueOf(assetsDirSpec);
        String assetIndex = options.valueOf(assetIndexSpec);
        File modAssetsDir = options.valueOf(modAssetsDirSpec);

        // Each argument is optional, load using Assets factory methods if present
        List<Assets> assetsList = new ArrayList<>();
        Optional.ofNullable(mcJar).ifPresent(it -> {
            System.out.println("Loading jar file: " + it.getAbsolutePath());
            assetsList.add(Assets.fromZipFile(it));
        });
        Optional.ofNullable(assetDir).ifPresent(it -> assetsList.add(Assets.fromMinecraftAssets(it, assetIndex)));
        Optional.ofNullable(modAssetsDir).ifPresent(it -> assetsList.add(Assets.fromDirectory(it)));

        // Combine Assets and use them to print stuff
        assetsList.stream().reduce(Assets::plus).ifPresent(assets -> {
            Language language = assets.getLanguage(lang);
            if (language == null) {
                System.err.printf("Language %s not found!%n", lang);
                System.exit(1);
            }

            // Print language info
            LanguageInfo info = language.getInfo();
            if (info == null) {
                System.out.println("Language " + lang);
            } else {
                System.out.printf("Language %s: %s (%s)%n", lang, info.getName(), info.getRegion());
            }

            // Print some translations
            System.out.println("Done: " + language.get("gui.done"));
            System.out.println("Custom: " + language.get("modid.custom"));
        });
    }
}
