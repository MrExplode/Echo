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

package hu.mineside.echo.listeners;

import hu.mineside.echo.Constants;
import hu.mineside.echo.permission.BotPermission;
import hu.mineside.echo.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.concurrent.TimeUnit;

import static hu.mineside.echo.Echo.embed;

@Slf4j
public class MemeListener {
    private static final String MEME_CHANNEL_ID = "redacted";
    private static final long reactTime = 60 * 1000;
    private long lastMemeTime = 0;

    @SubscribeEvent
    public void onMessage(GuildMessageReceivedEvent e) {
        if (e.getMessage().getChannel().getId().equals(MEME_CHANNEL_ID) && !e.getMessage().getAuthor().isBot()) {
            if (!e.getMessage().getAttachments().isEmpty()) {
                lastMemeTime = System.currentTimeMillis();
            } else {
                Message referenced = e.getMessage().getReferencedMessage();
                boolean replying = referenced != null && !referenced.getAttachments().isEmpty();
                if (!BotPermission.getByMember(e.getMember()).isStaff() && !replying && System.currentTimeMillis() - lastMemeTime > reactTime) {
                    MessageEmbed embed = embed().setColor(Constants.Colors.WARNING).setDescription("Ide csak képeket küldhetsz!").build();
                    e.getMessage().delete().flatMap(__ -> e.getChannel().sendMessage(embed)).delay(5, TimeUnit.SECONDS).flatMap(Message::delete).queue();
                    //log.info("Blocked message in meme channel from " + Utils.combinedName(e.getMember()));
                }
            }
        }
    }
}
