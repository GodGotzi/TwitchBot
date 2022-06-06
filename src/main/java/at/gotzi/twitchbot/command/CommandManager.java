package at.gotzi.twitchbot.command;

import at.gotzi.api.ano.Comment;
import at.gotzi.api.command.CommandHandler;
import at.gotzi.twitchbot.GotziBot;

import java.io.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CommandManager {

    private final GotziBot gotziBot;

    private CommandHandler consoleHandler;
    private CommandHandler twitchChatHandler;

    @Comment.Constructor
    public CommandManager(GotziBot gotziBot) throws IOException {
        this.gotziBot = gotziBot;
        initCommandHandlers();
    }

    private void initCommandHandlers() throws IOException {
        Scanner scanner = new Scanner(System.in);
        consoleHandler = new CommandHandler('/');
        consoleHandler.scanLoop(scanner::nextLine);

        twitchChatHandler = new CommandHandler('!');
        preLoadCommands();
    }

    private void preLoadCommands() throws IOException {
        File file = new File(GotziBot.config.getPreCommandLoadFile());
        if (!file.exists()) file.createNewFile();
        CommandLoader commandLoader = new CommandLoader(file, gotziBot);
        commandLoader.load().forEach(twitchChatHandler::registerCommand);
    }

    @Comment.Getter
    public CommandHandler getConsoleHandler() {
        return consoleHandler;
    }

    @Comment.Getter
    public CommandHandler getTwitchChatHandler() {
        return twitchChatHandler;
    }

    @Comment.Getter
    public GotziBot getTwitchBot() {
        return gotziBot;
    }
}
