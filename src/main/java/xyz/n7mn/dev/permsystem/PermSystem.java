package xyz.n7mn.dev.permsystem;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class PermSystem extends JavaPlugin {

    private Connection con;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        LuckPerms api = null;
        Plugin[] plugins = getServer().getPluginManager().getPlugins();
        for (Plugin plugin : plugins){
            System.out.println(plugin.getName());
            if (!plugin.getName().equals("LuckPerms")){
                continue;
            }

            System.out.println("？");
            try {
                RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
                if (provider != null) {
                    api = provider.getProvider();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            con = DriverManager.getConnection("jdbc:mysql://" + getConfig().getString("mysqlServer") + ":" + getConfig().getInt("mysqlPort") + "/" + getConfig().getString("mysqlDatabase") + getConfig().getString("mysqlOption"), getConfig().getString("mysqlUsername"), getConfig().getString("mysqlPassword"));
            con.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            getPluginLoader().disablePlugin(this);
        }

        getServer().getPluginManager().registerEvents(new PermListener(this, con), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        new Thread(()->{
            try {
                if (con != null){
                    con.close();
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
        }).start();
    }
}
