package website.bryces.northsell.main;

import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.getback4j.getback.json.*;
import org.getback4j.getback.runnable.PasswordAuthentication;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 *
 * @author Bryce
 */
public class user {


	public static String owns_content(String[] args) {
		try {
			Map<String, Object> params = paramify(args);
			if (!Main.getInstance().getSQLSaver().getConnection().createStatement()
					.executeQuery("SELECT timestamp FROM script_purchases WHERE userid = '" + params.get("userid")
							+ "' AND scriptid='" + params.get("scriptid") + "'")
					.first()) {
				return JSONObjects.getStatusFailure();
			} else {
				return JSONObjects.getStatusOk();
			}
			// Auto SQL injection patches!
		} catch (NumberFormatException | SQLException e) {
			System.out.println("Error in owns_script: " + e.toString());
			return JSONObjects.getStatusError("Error in owns_script: " + e.toString());
		}
	}

	// public static String get_url(String[] args) {
	// return JSONObjects.getStatusOk(Main.getInstance().getUtils().getUrl());
	// }

	// public static String get_webname(String[] args) {
	// return JSONObjects.getStatusOk(Main.getInstance().getUtils().getWebName());
	// }

	// public static String get_sslmode(String[] args) {
	// return JSONObjects.getStatusOk(Main.getInstance().getUtils().getSSLMode());
	// }

	public static String get_code(String[] args) {
		try {
			Map<String, Object> params = paramify(args);
			ResultSet result = Main.getInstance().getSQLSaver().getConnection().createStatement()
					.executeQuery("SELECT code FROM app_codes WHERE userid = '" + params.get("userid") + "'");
			if (!result.first()) {
				long seconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
				String salt = String.valueOf(params.get("userid")) + seconds + "i-<3-p-u-s-s-y" + seconds;

				byte[] generated = java.security.MessageDigest.getInstance("SHA-1").digest(salt.getBytes());
				String generatedKey = "";
				for (byte b : generated) {
					generatedKey += b;
				}

				System.out.println(String.valueOf(params.get("userid")) + generatedKey);
				Main.getInstance().getSQLSaver().getConnection().createStatement()
						.executeUpdate("INSERT INTO app_codes (userid, code, timestamp) VALUES('" + params.get("userid")
								+ "', '" + generatedKey + "', '" + seconds + "') ON DUPLICATE KEY UPDATE code = '"
								+ generatedKey + "'");
				return JSONObjects.getStatusOk(generatedKey);
			} else {
				return JSONObjects.getStatusOk(result.getString("code"));
			}

			// Auto SQL injection patches!
		} catch (SQLException | NumberFormatException | NoSuchAlgorithmException e) {
			System.out.println("Error in get_code: " + e.toString());

			return JSONObjects.getStatusError("Error in get_code: " + e.toString());
		}
	}

	public static String get(String[] args) throws ParseException {
		JSONParser parser = new JSONParser();
		try {
			Map<String, Object> params = paramify(args);

			ResultSet result = Main.getInstance().getSQLSaver().getConnection().createStatement().executeQuery(
					"SELECT users.id, users.username, concat('S', users.id) as steam, users.timestamp, users.avatar FROM users WHERE users.id = '"
							+ params.get("userid") + "'");
			if (!result.first()) {
				return JSONObjects.getStatusFailure("User not found");
			} else {
				JSONObject data = new JSONObject();
				data.put("id", result.getInt("id"));
				data.put("username", result.getString("username"));
				data.put("timestamp", result.getString("timestamp"));
				data.put("avatar", result.getString("avatar"));
				JSONObject obj = (JSONObject) parser.parse(JSONObjects.getStatusOk(data.toJSONString()));
				// TODO:
				// Add:
				// admin: 0
				// rep: 0
				// class: "Admin"
				return obj.toJSONString();
			}

		} catch (NumberFormatException | SQLException e) {
			System.out.println("Error in get: " + e.toString());
			return JSONObjects.getStatusError("Error in get: " + e.toString());
		}
	}

	public static String register(String[] args) {
		try {
			Map<String, Object> params = paramify(args);
			String firstName = String.valueOf(params.get("fname"));
			String lastName = String.valueOf(params.get("lname"));
			String username = String.valueOf(params.get("username"));
			String password = String.valueOf(params.get("password"));
			String email = String.valueOf(params.get("email"));
			PasswordAuthentication auth = new PasswordAuthentication();
			long seconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
			password = auth.hash(password);

			if (username.matches("/[\\'^£$%&*()}{@#~?><>,|=_+¬-]/")) {
				return JSONObjects.getStatusFailure("Username can only contain a-z 0-9");
			}

			if (username.contains(" ")) {
				return JSONObjects.getStatusFailure("Username cannot contain spaces");
			}

			ResultSet result = Main.getInstance().getSQLSaver().getConnection().createStatement()
					.executeQuery("SELECT email FROM users WHERE email = '" + params.get("email") + "'");
			if (result.first()) {
				return JSONObjects.getStatusFailure("An account with that E-Mail already exists");
			}

			result = Main.getInstance().getSQLSaver().getConnection().createStatement()
					.executeQuery("SELECT username FROM users WHERE username = '" + params.get("username") + "'");
			if (result.first()) {
				return JSONObjects.getStatusFailure("An account with that username already exists");
			}
			UUID uid = UUID.randomUUID();
			Integer rst = Main.getInstance().getSQLSaver().getConnection().createStatement()
					.executeUpdate("INSERT INTO users (username, token, password, avatar, email, timestamp) VALUES ('"
							+ username + "', '" + uid.toString() + "', '" + password + "', 'NULL', '" + email + "', '"
							+ seconds + "')");
			if (rst == 1) {
				JSONObject data = new JSONObject();
				data.put("tfa", false);
				data.put("token", uid.toString());
				data.put("username", username);
				return JSONObjects.getStatusOk(data.toJSONString());
			} else {
				return JSONObjects.getStatusFailure("SQL returned satus 0");
			}

		} catch (NumberFormatException | SQLException e) {
			System.out.println("Error in get: " + e.toString());
			return JSONObjects.getStatusError("Error in get: " + e.toString());
		}
	}

	public static String login(String[] args) {
		try {
			Map<String, Object> params = paramify(args);
			String username = String.valueOf(params.get("username"));
			String password = String.valueOf(params.get("password"));
			PasswordAuthentication auth = new PasswordAuthentication();

			ResultSet result = Main.getInstance().getSQLSaver().getConnection().createStatement().executeQuery(
					"SELECT id,username,password,email FROM users WHERE username = '" + params.get("username") + "';");
			if (!result.first()) {
				System.out.println("Null");
			}
			String password2 = result.getString("password");

			if (auth.authenticate(password, password2)) {
				// Logged in
				UUID uid = UUID.randomUUID();

				Integer rst = Main.getInstance().getSQLSaver().getConnection().createStatement().executeUpdate(
						"UPDATE users SET token = '" + uid.toString() + "' WHERE username = '" + username + "';");

				if (rst == 1) {
					JSONObject data = new JSONObject();
					data.put("tfa", false);
					data.put("token", uid.toString());
					data.put("username", username);
					System.out.println("JSON:" + JSONObjects.getStatusOk(data));
					return JSONObjects.getStatusOk(data);
				} else {
					return JSONObjects.getStatusFailure("SQL returned satus 0");
				}

			} else {
				// Incorrect password
				return JSONObjects.getStatusFailure("Incorrect Password");
			}
		} catch (NumberFormatException | SQLException e) {
			System.out.println("Error in login: " + e.toString());
			return JSONObjects.getStatusError("Error in login: " + e.toString());
		}
	}

	public static String verifylogin(String[] args) {
		try {
			Map<String, Object> params = paramify(args);
			String token = String.valueOf(params.get("_t"));
			System.out.println("Token: " + token);

			ResultSet result = Main.getInstance().getSQLSaver().getConnection().createStatement().executeQuery(
					"SELECT username,token,avatar,timestamp,id FROM users WHERE token = '" + token + "';");
			if (result.first()) {
				System.out.println("Logged in:");
				JSONObject obj = new JSONObject();
				obj.put("tfa", false);
				obj.put("token", token);
				obj.put("username", result.getString("username"));
				obj.put("id", String.valueOf(result.getInt("id")));
				// TODO: Return banned, avatar, isAdmin, etc.
				return JSONObjects.getStatusOk(obj);
			} else {
				System.out.println("No token found");
				return JSONObjects.getStatusFailure();

			}

		} catch (NumberFormatException | SQLException e) {
			System.out.println("Error in login: " + e.toString());
			e.printStackTrace();
			return JSONObjects.getStatusError("Error in login: " + e.toString());
		}
	}

	public static String logout(String[] args) {
		try {
			Map<String, Object> params = paramify(args);
			Integer userid = (Integer) params.get("userid");

			Integer rst = Main.getInstance().getSQLSaver().getConnection().createStatement()
					.executeUpdate("UPDATE users SET token = NULL WHERE id = '" + userid + "';");
			if (rst == 1) {
				return JSONObjects.getStatusOk();
			} else {
				return JSONObjects.getStatusFailure("SQL returned status 0.");
			}
		} catch (NumberFormatException | SQLException e) {
			System.out.println("Error in logout: " + e.toString());
			e.printStackTrace();
			return JSONObjects.getStatusError("Error in login: " + e.toString());
		}
	}

	public static String get_all_stats(String[] args) {
		JSONObject obj = new JSONObject();
		obj.put("activeUsers", 0);
		obj.put("tfUsers", 0);
		obj.put("totalUsers", 1);
		return JSONObjects.getStatusOk(obj.toJSONString());
	}

	// Converts params sent in as String E.g (users=10) to the proper Data Dype
	// variable
	// in the format of Map<String, Object>
	public static Map<String, Object> paramify(String[] args) throws NumberFormatException {
		Map<String, Object> localParams = new HashMap<>();
		for (String arg : args) {
			String param = StringUtils.substringBefore(arg, "=");
			if (param.equalsIgnoreCase("userid")) {
				System.out.println("UserID");
				Integer value = Integer.parseInt(StringUtils.substringAfter(arg, "="));
				localParams.put(param, value);
			} else if (param.equalsIgnoreCase("scriptid")) {
				Integer value = Integer.parseInt(StringUtils.substringAfter(arg, "="));
				localParams.put(param, value);
			} else {
				String value = StringUtils.substringAfter(arg, "=");
				value = value.replace("%20", " ");
				localParams.put(param, value);
			}
		}
		return localParams;
	}

}
