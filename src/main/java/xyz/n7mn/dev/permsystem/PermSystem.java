package xyz.n7mn.dev.permsystem;

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

        try {
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            con = DriverManager.getConnection("jdbc:mysql://" + getConfig().getString("mysqlServer") + ":" + getConfig().getInt("mysqlPort") + "/" + getConfig().getString("mysqlDatabase") + getConfig().getString("mysqlOption"), getConfig().getString("mysqlUsername"), getConfig().getString("mysqlPassword"));
            con.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            getPluginLoader().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new PermListener(this, con), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
