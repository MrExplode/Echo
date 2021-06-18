/*
 *     Echo - Discord bot
 *     Copyright (C) 2021  SunStorm (aka. MrExplode)
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package hu.mineside.echo.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
@UtilityClass
public class JsonLoader {
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Nullable
    public static <T> T loadOrDefault(String name, Class<T> configType) {
        File configFile = new File(name);
        if (configFile.exists())
            return loadConfig(name, configType);

        T config;

        try {
            config = configType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Failed to instantiate class", e);
            return null;
        }

        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(configFile));
            gson.toJson(config, writer);
            writer.close();
        } catch (IOException e) {
            log.error("Failed to create new config file", e);
        }

        return config;
    }

    @Nullable
    public static <T> T loadConfig(String name, Class<T> configType) {
        try {
            File configFile = new File(name);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
            T asd = gson.fromJson(reader, configType);
            reader.close();

            return asd;
        } catch (IOException e) {
            log.error("Failed to load config file", e);
            return null;
        }
    }
}
