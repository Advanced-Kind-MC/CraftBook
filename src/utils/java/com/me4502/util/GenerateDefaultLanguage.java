package com.me4502.util;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.common.LanguageManager;
import com.sk89q.craftbook.util.developer.ExternalUtilityBase;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class GenerateDefaultLanguage extends ExternalUtilityBase {

    @Override
    public void generate () {

        File language = new File(CraftBookPlugin.inst().getDataFolder(), "en_US.yml");

        if(!language.exists()) {
            try {
                language.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            language.delete();
            try {
                language.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YAMLProcessor proc = new YAMLProcessor(language, true, YAMLFormat.EXTENDED);
        try {
            proc.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        proc.setWriteDefaults(true);

        new LanguageManager();

        for(Entry<String, String> map : LanguageManager.defaultMessages.entrySet()) {
            proc.getString(map.getKey(), map.getValue());
        }

        proc.save();
    }
}
