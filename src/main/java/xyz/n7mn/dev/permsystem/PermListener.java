package xyz.n7mn.dev.permsystem;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.sql.*;

class PermListener implements Listener {

    private final Plugin plugin;
    private Connection con;

    public PermListener(Plugin plugin, Connection con){
        this.plugin = plugin;
        this.con = con;
    }

    @EventHandler
    public void PlayerJoinEvent (PlayerJoinEvent e){

        new Thread(()->{
            try {
                PreparedStatement statement = con.prepareStatement("SELECT * FROM MinecraftUserList");
                statement.execute();
                statement.close();
            } catch (SQLException ex){
                try {
                    con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                    con.setAutoCommit(true);
                } catch (SQLException ex2){
                    plugin.getPluginLoader().disablePlugin(plugin);
                }
            }

            String perm = "";

            try {
                PreparedStatement statement = con.prepareStatement("SELECT * FROM MinecraftUserList WHERE MinecraftUUID = ?");
                statement.setString(1, e.getPlayer().getUniqueId().toString());
                ResultSet set = statement.executeQuery();
                if (set.next()){
                    perm = set.getString("Role").toLowerCase();
                }
                set.close();
                statement.close();
            } catch (Exception ex){
                ex.printStackTrace();
                plugin.getPluginLoader().disablePlugin(plugin);
            }

            if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null){
                try {
                    RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
                    if (provider != null) {
                        LuckPerms api = provider.getProvider();

                        User user = api.getPlayerAdapter(Player.class).getUser(e.getPlayer());

                        user.data().clear();
                        user.data().add(Node.builder("group."+perm).build());

                        api.getUserManager().saveUser(user);
                        return;
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            int permLevel = -1;
            int userLevel = -1;

            if (plugin.getConfig().getString("isOp").equals("admin")){
                permLevel = 5;
            }
            if (plugin.getConfig().getString("isOp").equals("moderator")){
                permLevel = 4;
            }
            if (plugin.getConfig().getString("isOp").equals("developer")){
                permLevel = 3;
            }
            if (plugin.getConfig().getString("isOp").equals("mapper")){
                permLevel = 2;
            }
            if (plugin.getConfig().getString("isOp").equals("Authenticated")){
                permLevel = 1;
            }

            if (perm.equals("admin")){
                userLevel = 5;
            }
            if (perm.equals("moderator")){
                userLevel = 4;
            }
            if (perm.equals("developer")){
                userLevel = 3;
            }
            if (perm.equals("mapper")){
                userLevel = 2;
            }
            if (perm.equals("authenticated")){
                userLevel = 1;
            }

            e.getPlayer().setOp(userLevel >= permLevel);
        }).start();

    }

    @EventHandler
    public void PlayerQuitEvent(PlayerQuitEvent e){
        if (e.getPlayer().isOp()){
            e.getPlayer().setOp(false);
        }
    }

}
