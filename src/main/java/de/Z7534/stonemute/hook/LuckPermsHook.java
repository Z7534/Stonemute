package de.Z7534.stonemute.hook;

import de.Z7534.stonemute.config.StonemuteConfig;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.UUID;

public final class LuckPermsHook {

    private final LuckPerms luckPerms;
    private final StonemuteConfig settings;

    public LuckPermsHook(StonemuteConfig settings) {
        this.luckPerms = LuckPermsProvider.get();
        this.settings = settings;
    }

    /** Thread-sicher: liest nur bereits geladene LuckPerms-Daten, blockiert nie. */
    public boolean hasBypass(UUID uuid) {
        if (luckPerms == null) {
            return false;
        }
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) {
            return false;
        }
        return user.getCachedData()
                .getPermissionData()
                .checkPermission(settings.bypassPermission())
                .asBoolean();
    }
}
