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

package hu.mineside.echo.permission;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Getter
public enum BotPermission {
    //PUBLIC: developer, *ˇ, *, team lead, dc lead, owner
    //STAFF: leader, dev
    OPERATOR("redacted", "redacted", "redacted", "redacted", "redacted", "redacted", "redacted", "redacted"),
    //PUBLIC: admin, admin+, manager
    //STAFF: admin, admin+, manager
    ADMIN("redacted", "redacted", "redacted", "redacted" , "redacted", "redacted"),
    //PUBLIC: helper, moderator, moderator+
    //STAFF: helper, mod, mod+
    MODERATOR("redacted", "redacted", "redacted", "redacted", "redacted", "redacted"),
    //skidder, vip, vip+, nitro booster, media
    VIP("redacted", "redacted", "redacted", "redacted", "redacted"),
    PLAYER();

    private final List<String> roles;

    BotPermission(String... roles) {
        this.roles = Arrays.asList(roles);
    }

    public boolean isStaff() {
        return this == MODERATOR || this == ADMIN || this == OPERATOR;
    }

    @NotNull
    public static BotPermission getById(long id) {
        return getById(String.valueOf(id));
    }

    @NotNull
    public static BotPermission getById(String id) {
        for (BotPermission perm : values()) {
            if (perm.roles.contains(id))
                return perm;
        }
        return PLAYER;
    }

    @NotNull
    public static BotPermission getByMember(Member member) {
        if (member.getRoles().isEmpty())
            return getById("");
        else
            return getById(member.getRoles().get(0).getId());
    }

    public static boolean hasPermission(Member member, BotPermission permission) {
        return getByMember(member).ordinal() <= permission.ordinal();
    }
}
