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

package hu.mineside.echo.command;

import hu.mineside.echo.GuildType;
import hu.mineside.echo.permission.BotPermission;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public abstract class Command {
    private final String label;
    private final String name;
    private final String description;
    private final BotPermission permission;
    private final List<GuildType> guildTypes;

    public Command(String label, String name, String description, BotPermission permission, GuildType... guildType) {
        this.label = label;
        this.name = name;
        this.description = description;
        this.permission = permission;
        this.guildTypes = new ArrayList<>(Arrays.asList(guildType));
        if (guildTypes.isEmpty())
            guildTypes.add(GuildType.PUBLIC);
    }

    public abstract void onCommand(Message message, String command, List<String> args);
}
