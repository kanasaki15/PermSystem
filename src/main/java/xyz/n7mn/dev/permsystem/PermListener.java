package xyz.n7mn.dev.permsystem;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.sql.*;

class PermListener implements Listener {

    private final Plugin plugin;
    private Connection con;
    private final LuckPerms luckPerms;

    public PermListener(Plugin plugin, Connection con, LuckPerms luckPerms){
        this.plugin = plugin;
        this.con = con;
        this.luckPerms = luckPerms;
    }

    public PermListener(PermSystem plugin, Connection con) {
        this.plugin = plugin;
        this.con = con;
        this.luckPerms = null;
    }

    @EventHandler
    public void PlayerJoinEvent (PlayerJoinEvent e){

        try {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM MinecraftUserList");
            statement.execute();
            statement.close();
        } catch (SQLException ex){
            try {
                con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                con.setAutoCommit(true);
            } catch (SQLException ex2){
                ex2.printStackTrace();
                plugin.getPluginLoader().disablePlugin(plugin);
            }
        }

        String perm = "";
        int userLevel = 0;
        int permLevel = plugin.getConfig().getInt("isOpPermLevel");

        try {
            PreparedStatement statement = con.prepareStatement("" +
                    "SELECT * FROM MinecraftUserList a, RoleRankList b WHERE a.RoleUUID = b.UUID" +
                    " AND MinecraftUUID = ?"
            );
            statement.setString(1, e.getPlayer().getUniqueId().toString());
            ResultSet set = statement.executeQuery();
            if (set.next()){
                perm = set.getString("Name").toLowerCase();
                userLevel = set.getInt("Rank");
            }
            set.close();
            statement.close();
        } catch (Exception ex){
            ex.printStackTrace();
            plugin.getPluginLoader().disablePlugin(plugin);
        }

        try {
            if (luckPerms != null){
                User user = luckPerms.getPlayerAdapter(Player.class).getUser(e.getPlayer());

                user.data().clear();
                user.data().add(Node.builder("group."+perm).build());

                luckPerms.getUserManager().saveUser(user);
                return;
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

        e.getPlayer().setOp(permLevel <= userLevel);
    }

    @EventHandler
    public void PlayerQuitEvent(PlayerQuitEvent e){
        if (e.getPlayer().isOp()){
            e.getPlayer().setOp(false);
        }
    }

}
