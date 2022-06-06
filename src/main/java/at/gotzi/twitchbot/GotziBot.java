package at.gotzi.twitchbot;

import at.gotzi.api.GotziRunnable;
import at.gotzi.api.ano.Comment;
import at.gotzi.twitchbot.command.CommandManager;
import at.gotzi.twitchbot.command.system.MessageCommand;
import at.gotzi.twitchbot.command.system.StopCommand;
import at.gotzi.twitchbot.command.twitch.VersionCommand;
import at.gotzi.twitchbot.core.BotSequence;

import java.io.IOException;

public class GotziBot extends BotSequence {
    public static Config config;

    public static void main(String[] args) throws IOException {
        Config config = new Config(args);
        config.load();
        GotziBot.config = config;

        GotziBot gotziBot = new GotziBot();
        gotziBot.setUsername("GotziBot");
        gotziBot.setOauth_Key(config.getConfigValues().get("OAUTH"));
        gotziBot.startSystem();
    }

    public static String VERSION = "v1.0_alpha";

    private final CommandManager commandManager;

    @Comment.Constructor
    public GotziBot() throws IOException {
        commandManager = new CommandManager(this);
        registerCommands();
    }

    private void registerCommands() {
        //system commands
        commandManager.getConsoleHandler().registerCommand(new StopCommand(this));
        commandManager.getConsoleHandler().registerCommand(new MessageCommand(this));

        //twitch commands
        commandManager.getTwitchChatHandler().registerCommand(new VersionCommand(this));
    }

    private void startSystem() {
        connect();
        setMainChannel(config.getConfigValues().get("MAIN_CHANNEL"));

        new GotziRunnable() {
            @Override
            public void run() {
                start();
            }
        }.runTaskAsync();

        initActions();
    }

    public void initActions() {
        getMessageActions().add(messageContext -> {
            commandManager.getTwitchChatHandler().executeCommand(messageContext.message(), new Object[]{messageContext.user(), messageContext.channel()});
        });
    }

    @Override
    public void stop() {
        try {
            super.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
                System.exit(0);
        }
    }

    @Comment.Getter
    public CommandManager getCommandManager() {
        return commandManager;
    }
}
