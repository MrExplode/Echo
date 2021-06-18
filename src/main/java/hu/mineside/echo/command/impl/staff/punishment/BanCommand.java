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

package hu.mineside.echo.command.impl.staff.punishment;

import hu.mineside.echo.Constants;
import hu.mineside.echo.command.Command;
import hu.mineside.echo.permission.BotPermission;
import hu.mineside.echo.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.ArrayList;
import java.util.List;

import static hu.mineside.echo.Echo.embed;

@Slf4j
public class BanCommand extends Command {

    public BanCommand() {
        super("ban", "Ban", "Játékos kitiltása", BotPermission.ADMIN);
    }

    @Override
    public void onCommand(Message message, String command, List<String> args) {
        args = new ArrayList<>(args);
        //preconditions
        if (args.size() < 1) {
            message.getChannel().sendMessage(embed().setTitle("Helytelen parancs!").setDescription("ban <player> [indok]").setColor(Constants.Colors.WARNING).build()).queue();
            return;
        }
        if (message.getMentionedMembers().size() != 1) {
            message.getChannel().sendMessage(embed().setTitle("Tageld meg akit bannolni akarsz!").setColor(Constants.Colors.WARNING).build()).queue();
            return;
        }
        if (!message.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
            message.getChannel().sendMessage(embed().setTitle("Nincs jogom a bannoláshoz!").setColor(Constants.Colors.ERROR).build()).queue();
            return;
        }
        if (!message.getMember().canInteract(message.getMentionedMembers().get(0))) {
            message.getChannel().sendMessage(embed().setTitle("Őt nem bannolhatod!").setColor(Constants.Colors.ERROR).build()).queue();
            return;
        }

        //reason
        String reason = "";
        MessageAction messageAction;
        Member target = message.getMentionedMembers().get(0);
        if (args.size() != 1) {
            args.remove(0);
            reason = String.join(" ", args);
            messageAction = message.getChannel().sendMessage(embed().setTitle(target.getEffectiveName() + " kitiltva!").setDescription("Indok: " + reason).build()) ;
        } else {
            messageAction = message.getChannel().sendMessage(embed().setTitle(target.getEffectiveName() + " kitiltva!").build());
        }
        reason = reason.isEmpty() ? "Nincs megadva" : reason;
        //ban
        message.getGuild().ban(target, 0, message.getMember().getEffectiveName() + ": " + reason).flatMap(__ -> messageAction).queue();
        String finalReason = reason;
        target.getUser().openPrivateChannel().queue(channel -> channel.sendMessage(embed().setTitle("Ki lettél tiltva!").setDescription("Indok: " + finalReason).build()).queue(), error -> {});
        log.info(Utils.combinedName(message.getMember()) + " banned " + target.getEffectiveName() + ": " + reason);
    }
}
