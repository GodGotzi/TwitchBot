package at.gotzi.twitchbot.api;

import at.gotzi.twitchbot.api.chat.Channel;
import at.gotzi.twitchbot.api.chat.User;
import at.gotzi.twitchbot.api.json.JsonObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

public class ChannelQueries {

	private static final String V5_API_BASE = "https://api.twitch.tv/kraken/channels/";

	public static boolean isSubscriber(Channel channel, User user, String oAuthToken, String clientId) {
		int[] ids = UserQueries.getUserId(clientId, user.toString(), channel.toString().substring(1));
		int userId = ids[0], channelId = ids[1];
		String requestUri = V5_API_BASE + channelId + "/subscriptions/" + userId;
		
		try {
			URL request = new URL(requestUri);
			URLConnection conn = request.openConnection();
			//conn.setRequestMethod(
			conn.setRequestProperty("Authorization", "OAuth " + oAuthToken);
			conn.setRequestProperty("Client-ID", clientId);
			conn.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
			
			Reader response = new InputStreamReader(conn.getInputStream());
			JsonObject responseObj = JsonObject.readFrom(response);
			return responseObj.names().contains("_id"); //id signalizes that subscription exists; underscore intentional
		} catch (FileNotFoundException e) { //No subscription. For some reason this happens instead of a JSON object being sent.
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	
}
