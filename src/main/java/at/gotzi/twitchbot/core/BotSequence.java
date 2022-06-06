package at.gotzi.twitchbot.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import at.gotzi.api.Action;
import at.gotzi.api.GHelper;
import at.gotzi.api.GLogger;
import at.gotzi.api.GotziRunnable;
import at.gotzi.api.ano.Comment;
import at.gotzi.twitchbot.api.chat.Channel;
import at.gotzi.twitchbot.api.chat.User;

/**
 * The main object to start making your bot
 * @author Leonardo Mariscal
 * @version 1.0-Beta
 */
public abstract class BotSequence {
	
	private String whispers_ip = "";
	private int whispers_port = 443;
	private boolean wen = true;
	private String user;
	private String oauth_key;
	private BufferedWriter writer;
	private BufferedReader reader;

	private Socket socket;
	
	private Channel mainChannel;

	private final ArrayList<Action<MessageContext>> messageActions = new ArrayList<>();
	private final ArrayList<Action<HostContext>> hostActions = new ArrayList<>();
	private final ArrayList<Action<UserJoinContext>> userJoinActions = new ArrayList<>();
	private final ArrayList<Action<MessageContext>> commandActions = new ArrayList<>(); 
	private final ArrayList<Action<MessageContext>> subActions = new ArrayList<>();
	private final ArrayList<Action<UserJoinContext>> userPartsActions = new ArrayList<>();
	private final ArrayList<Action<WhisperContext>> whisperActions = new ArrayList<>();
	
	private boolean stopped = true;
	private String commandTrigger = "!";
	private String clientID = "";

	private static final Logger LOGGER = GHelper.LOGGER;
	
	public final List<Channel> getChannels(){
		return Channel.getChannels();
	}

	public BotSequence(){
	}

	public void connect() {
		wen = false;
		connect("irc.twitch.tv", 6667);
	}

	public synchronized void connect(String ip, int port) {
		if (isRunning()) return;

		try{
			socket = new Socket(ip, port);

			this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			this.writer.write("PASS " + oauth_key + "\r\n");
			this.writer.write("NICK " + user + "\r\n");
			this.writer.flush();

			new GotziRunnable() {

				@Override
				public void run() throws Exception {
					refreshConnection(ip, port);
				}
			}.runRepeatingTaskAsync(30000);

			printSocketIn();


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized void refreshConnection(String ip, int port) throws IOException {
		socket.close();
		socket = new Socket(ip, port);

		this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		printSocketIn();
	}

	private void printSocketIn() throws IOException {
		String line = "";
		do {
			if (line.contains("004")) {
				LOGGER.info("Connected >> " + user + " ~ irc.twitch.tv");
				break;
			} else {
				LOGGER.info(line);
			}
		} while ((line = this.reader.readLine()) != null);
	}

	public final void setUsername(String username) {
		this.user = username;
	}

	public final void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public final String getClientID() {
		if (this.clientID != null)
			return this.clientID;
		else {
			LOGGER.info("You need to give a clientID to use the TwitchAPI");
			return "";
		}
	}

	public final void setOauth_Key (String oauth_key) {
		this.oauth_key = oauth_key;
	}

	public void sendRawMessage(Object message) {
		try {
			this.writer.write(message + " \r\n");
			this.writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info(message.toString());
	}
	
	public void sendMessage(Object message, Channel channel) {
		try {
			this.writer.write("PRIVMSG " + channel + " :" + message.toString() + "\r\n");
			this.writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("> MSG " + channel + " : " + message.toString());
	}

    public static Logger getLOGGER() {
        return LOGGER;
    }
	
    public void setNick(String newNick){
        try {
            this.writer.write("NICK " + newNick + "\r\n");
        }catch (IOException ioe){

        }
    }
	
	public final Channel setMainChannel (String channel) {
		Channel cnl = Channel.getChannel(channel.toLowerCase(), this);
		sendRawMessage("JOIN " + cnl.toString().toLowerCase() + "\r\n");
		this.mainChannel = cnl;
		LOGGER.info("> JOIN " + cnl);
		return cnl;
	}
	
	public final BufferedWriter getWriter () {
		return this.writer;
	}
	
	public final synchronized void start() {
		if (isRunning()) return;
		String line = "";
		stopped = false;
		try {
			while (!socket.isClosed() && !isStopped() && (line = this.reader.readLine()) != null) {
				 if (line.contains("PRIVMSG")) {
			        String str[];
			        str = line.split("!");
			        final User msg_user = User.getUser(str[0].substring(1, str[0].length()));
			        str = line.split(" ");
			        Channel msg_channel;
			        msg_channel = Channel.getChannel(str[2], this);
			        String msg_msg = line.substring((str[0].length() + str[1].length() + str[2].length() + 4), line.length());
			        
					LOGGER.info("> " + msg_channel + " | " + msg_user + " >> " +  msg_msg);
			        
					if (msg_msg.startsWith(commandTrigger))
			        	onCommand(msg_user, msg_channel, msg_msg.substring(1));
			        if (msg_user.toString().equals("jtv") && msg_msg.contains("now hosting")) {
			        	String hoster = msg_msg.split(" ")[0];
			        	onHost(User.getUser(hoster), msg_channel);
			        }
					
			        onMessage(msg_user, msg_channel, msg_msg);
			    } else if (line.contains(" JOIN ")) {
			    	String[] p = line.split(" ");
			    	String[] pd = line.split("!");
			    	
					if (p[1].equals("JOIN"))
			    		userJoins(User.getUser(pd[0].substring(1)), Channel.getChannel(p[2], this));
				} else if (line.contains(" PART ")) {
			    	String[] p = line.split(" ");
			    	String[] pd = line.split("!");
			    	
					if (p[1].equals("PART"))
			    		userParts(User.getUser(pd[0].substring(1)), Channel.getChannel(p[2], this));
				} else if (line.contains(" WHISPER ")) {
					String[] parts = line.split(":");
					final User wsp_user = User.getUser(parts[1].split("!")[0]);
					String message = parts[2];
					
					onWhisper(wsp_user, message);
				}  else if (line.startsWith(":tmi.twitch.tv NOTICE")) {
			    	String[] parts = line.split(" ");
			    	if (line.contains("This room is now in slow mode. You may send messages every")) {
			    		LOGGER.info("> Chat is now in slow mode. You can send messages every " + parts[15] + " sec(s)!");
			    	} else if (line.contains("subscribers-only mode")) {
			    		if (line.contains("This room is no longer"))
			    			LOGGER.info("> The room is no longer Subscribers Only!");
			    		else
			    			LOGGER.info("> The room has been set to Subscribers Only!");
			    	} else {
			    		LOGGER.info(line);
			    	}
			    } else if (line.startsWith(":jtv MODE ")) {
			    	String[] p = line.split(" ");
			    	if (p[3].equals("+o")) {
			    		LOGGER.info("> +o " + p[4]);
			    	} else {
			    		LOGGER.info("> -o " + p[4]);
			    	}
			    } else if (line.toLowerCase().contains("disconnected")) {
				    LOGGER.info( line);
				    this.connect();
			    } else {
			        LOGGER.info("> " + line);
			    }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stop() throws IOException {
		this.stopped = true;
		this.sendRawMessage("Stopping");

		socket.close();
	}

	private synchronized boolean isStopped() {
		return this.stopped;
	}

	public void stopWithMessage(String message) {
		this.stopped = true;
		this.sendMessage(message, mainChannel);
	}

	public boolean isRunning() {
		return !stopped;
	}

	public void whisper(User user, String message) {
		this.sendMessage("/w " + user + " " + message, mainChannel);
	}

	private void onMessage(User user, Channel channel, String message) {
		messageActions.forEach(action -> action.run(new MessageContext(user, channel, message)));	
	}
	
	private void onCommand(User user, Channel channel, String command) {
		commandActions.forEach(action -> action.run(new MessageContext(user, channel, command)));
	}
	
	private void onHost(User hoster, Channel hosted) {
		hostActions.forEach(action -> action.run(new HostContext(hoster, hosted)));
	}
	
	private void userJoins(User user, Channel channel) {
		userJoinActions.forEach(action -> action.run(new UserJoinContext(user, channel)));
	}
	
	private void onSub(User user, Channel channel, String message) {
		subActions.forEach(action -> action.run(new MessageContext(user, channel, message)));
	}
	
	private void userParts(User user, Channel channel) {
		userPartsActions.forEach(action -> action.run(new UserJoinContext(user, channel)));
	}
	
	private  void onWhisper(User user, String message) {
		whisperActions.forEach(action -> action.run(new WhisperContext(user, message)));
	}

	protected final void setWhispersIp(String ip) {
		if (!ip.contains(":")) {
			LOGGER.info("Invaid ip!");
			return;
		}
		
		String[] args = ip.split(":");
		whispers_ip = args[0];
		whispers_port = Integer.parseInt(args[1]);
	}

	protected final void setWhispersIp(String ip, int port) {
		whispers_ip = ip;
		whispers_port = port;
	}

	public void setCommandTrigger(String trigger) {
		this.commandTrigger = trigger;
	}

	@Comment.Getter
	public ArrayList<Action<HostContext>> getHostActions() {
		return hostActions;
	}

	@Comment.Getter
	public ArrayList<Action<MessageContext>> getCommandActions() {
		return commandActions;
	}

	@Comment.Getter
	public ArrayList<Action<MessageContext>> getMessageActions() {
		return messageActions;
	}

	@Comment.Getter
	public ArrayList<Action<MessageContext>> getSubActions() {
		return subActions;
	}
	
	@Comment.Getter
	public ArrayList<Action<UserJoinContext>> getUserJoinActions() {
		return userJoinActions;
	}

	@Comment.Getter
	public ArrayList<Action<UserJoinContext>> getUserPartsActions() {
		return userPartsActions;
	}

	@Comment.Getter
	public ArrayList<Action<WhisperContext>> getWhisperActions() {
		return whisperActions;
	}

	@Comment.Getter
	public Channel getMainChannel() {
		return mainChannel;
	}

	public static record MessageContext(User user, Channel channel, String message) { }
	public static record HostContext(User hoster, Channel hosted) {}
	public static record UserJoinContext(User user, Channel channel) {}
	public static record WhisperContext(User user, String message) {}
}