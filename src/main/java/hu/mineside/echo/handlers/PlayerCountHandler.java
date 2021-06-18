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

package hu.mineside.echo.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hu.mineside.echo.Constants;
import hu.mineside.echo.Echo;
import hu.mineside.echo.utils.Tuple;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.ahornyai.httpclient.HttpMethod;
import me.ahornyai.httpclient.utils.RequestBuilder;
import net.dv8tion.jda.api.entities.Activity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
public class PlayerCountHandler {
    private int playerCount = 0;
    private final List<Tuple<String, Integer>> servers = new CopyOnWriteArrayList<>();

    public PlayerCountHandler() {
        Echo.getInstance().getScheduler().scheduleAtFixedRate(this::updateCount, 0, 15, TimeUnit.SECONDS);
    }

    private void updateCount() {
        new RequestBuilder()
                //todo: https still doesn't work
                .url("http://session.mineside.hu/server/servers.php")
                .method(HttpMethod.GET)
                .addUrlParam("type", Echo.getInstance().getConfig().getType())
                .addUrlParam("secret", Echo.getInstance().getConfig().getSecret())
                .build()
                .run(Constants.HTTP_CLIENT, (response, code) -> {
                    if (code != 200) {
                        log.warn("HTTP error code on playercount fetch: " + code);
                    }
                    JsonArray data = JsonParser.parseString(response).getAsJsonArray();
                    int current = 0;
                    servers.clear();
                    for (JsonElement element : data) {
                        JsonObject server = element.getAsJsonObject();
                        current += server.get("players").getAsInt();
                        servers.add(new Tuple<>(server.get("name").getAsString(), server.get("players").getAsInt()));
                    }
                    //only update on difference
                    if (current != playerCount) {
                        Echo.getInstance().getJda().getPresence().setActivity(Activity.playing("Játékosok: " + current));
                        playerCount = current;
                    }
                }, error -> log.error("Failed to fetch playercount", error), false);
    }
}
