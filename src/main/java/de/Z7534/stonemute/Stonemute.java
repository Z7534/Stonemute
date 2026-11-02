package de.Z7534.stonemute;

import de.Z7534.stonemute.config.StonemuteConfig;
import de.Z7534.stonemute.hook.LiteBansHook;
import de.Z7534.stonemute.hook.LuckPermsHook;
import de.Z7534.stonemute.hook.VoiceChatHook;
import de.Z7534.stonemute.listener.PlayerConnectionListener;
import de.Z7534.stonemute.mute.MuteService;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public final class Stonemute extends JavaPlugin implements CommandExecutor, TabCompleter {

    private static Stonemute instance;
    private StonemuteConfig settings;
    private MuteService muteService;
    private LiteBansHook liteBansHook;

    public static Stonemute getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (getServer().getPluginManager().getPlugin("voicechat") == null) {
            getLogger().severe("SimpleVoiceChat (voicechat) nicht gefunden - Stonemute wird deaktiviert.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (getServer().getPluginManager().getPlugin("LiteBans") == null) {
            getLogger().severe("LiteBans nicht gefunden - Stonemute wird deaktiviert.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        this.settings = new StonemuteConfig(this);

        BukkitVoicechatService voicechatService =
                getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (voicechatService == null) {
            getLogger().severe("SimpleVoiceChat-Service nicht gefunden - Stonemute wird deaktiviert.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        LuckPermsHook luckPermsHook = null;
        try {
            luckPermsHook = new LuckPermsHook(settings);
        } catch (Throwable e) {
            getLogger().warning("LuckPerms-API nicht verfuegbar oder nicht geladen: " + e.getMessage());
        }

        muteService = new MuteService(this, settings);

        liteBansHook = new LiteBansHook(this, muteService);
        liteBansHook.register();

        voicechatService.registerPlugin(new VoiceChatHook(this, settings, muteService, luckPermsHook));

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(muteService), this);

        muteService.start();

        if (getCommand("stonemute") != null) {
            getCommand("stonemute").setExecutor(this);
            getCommand("stonemute").setTabCompleter(this);
        }

        getLogger().info("Stonemute aktiv: LiteBans-Mutes werden auf SimpleVoiceChat uebertragen.");
    }

    @Override
    public void onDisable() {
        if (liteBansHook != null) {
            liteBansHook.unregister();
        }
        if (muteService != null) {
            muteService.shutdown();
        }
        getLogger().info("Stonemute deaktiviert.");
    }

    public void reload() {
        reloadConfig();
        this.settings = new StonemuteConfig(this);
        if (muteService != null) {
            muteService.updateSettings(this.settings);
            muteService.refreshAll();
        }
        getLogger().info("Stonemute-Konfiguration neu geladen.");
    }

    public StonemuteConfig getSettings() {
        return settings;
    }

    public MuteService getMuteService() {
        return muteService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("stonemute.admin")) {
                sender.sendMessage("§cKeine Berechtigung.");
                return true;
            }
            reload();
            sender.sendMessage("§aStonemute-Konfiguration wurde neu geladen.");
            return true;
        }
        sender.sendMessage("§eStonemute v" + getDescription().getVersion() + " von " + String.join(", ", getDescription().getAuthors()));
        sender.sendMessage("§7Nutze §f/stonemute reload §7um die Konfiguration neu zu laden.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("stonemute.admin")) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                return List.of("reload");
            }
        }
        return Collections.emptyList();
    }
}
