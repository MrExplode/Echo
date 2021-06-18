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

package hu.mineside.echo.command.impl.staff;

import hu.mineside.echo.Constants;
import hu.mineside.echo.GuildType;
import hu.mineside.echo.command.Command;
import hu.mineside.echo.permission.BotPermission;
import lombok.val;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static hu.mineside.echo.Echo.embed;

public class InfoCommand extends Command {

    public InfoCommand() {
        super("info", "Info", "Információk a playerről", BotPermission.MODERATOR, GuildType.PUBLIC, GuildType.STAFF, GuildType.DEV);
    }

    @Override
    public void onCommand(Message message, String command, List<String> args) {
        if (args.size() < 1) {
            message.getChannel().sendMessage(embed().setTitle("Helytelen parancs!").setDescription("kick <player> [indok]").setColor(Constants.Colors.WARNING).build()).queue();
            return;
        }
        if (message.getMentionedMembers().size() != 1) {
            message.getChannel().sendMessage(embed().setTitle("Tageld meg akiről információt akarsz!").setColor(Constants.Colors.WARNING).build()).queue();
            return;
        }
        Member member = message.getMentionedMembers().get(0);
        User user = member.getUser();
        val embed = embed();
        embed.setAuthor(member.getEffectiveName() + " információi", null, user.getEffectiveAvatarUrl());
        embed.setImage(user.getEffectiveAvatarUrl());
        embed.addField("Név", user.getAsTag(), true);
        String role = member.getRoles().size() == 0 ? "Játékos" : member.getRoles().get(0).getName();
        embed.addField("Rang", role, true);
        embed.addField("Regisztráció ideje", user.getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm:ss")), true);
        embed.addField("Avatar", "", false);

        message.getChannel().sendMessage(embed.build()).queue();
    }
}
