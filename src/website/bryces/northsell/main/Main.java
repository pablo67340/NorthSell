package website.bryces.northsell.main;

import org.getback4j.getback.api.WebsitePlugin;
import org.getback4j.getback.commands.Help;
import org.getback4j.getback.data.SQLSaver;
import org.getback4j.getback.main.GetBack;

public class Main extends WebsitePlugin{
	
	private SQLSaver saver;
	
	private static Main INSTANCE;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		// Save the default config, anchor from child
		saveDefaultConfig(this.getClass());
		String sqlHost = getConfig().getString("sqlHost");
		String sqlUser = getConfig().getString("sqlUser");
		String sqlPassword = getConfig().getString("sqlPassword");
		String sqlDatabase = getConfig().getString("sqlDatabase");
		int sqlPort = getConfig().getInt("sqlPort");
		
		saver = new SQLSaver(sqlHost, sqlDatabase, sqlUser, sqlPassword, sqlPort);
		
		this.getApiLoader().registerAPIClass(user.class);
		
		// Registers new command here
		GetBack.getInstance().addCommand("help", new Help());
		
	}
	
	public SQLSaver getSQLSaver() {
		return saver;
	}
	
	public static Main getInstance() {
		return INSTANCE;
	}

}
