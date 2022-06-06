package at.gotzi.twitchbot.command;

import at.gotzi.api.command.GCommand;
import at.gotzi.twitchbot.GotziBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CommandLoader {

    private final BufferedReader bufferedReader;
    private final GotziBot gotziBot;

    public CommandLoader(File file, GotziBot gotziBot) throws FileNotFoundException {
        bufferedReader = new BufferedReader(new FileReader(file));
        this.gotziBot = gotziBot;
    }

    public List<GCommand> load() {
        final List<GCommand> gotziCommands = new ArrayList<>();

        for (String line : bufferedReader.lines().toList()) {
            String label = line.split("=")[0];
            String rawMessage = line.split("=")[1];
            GCommand gotziCommand = new GCommand(label, gotziCommandContext -> {
                gotziBot.sendRawMessage(rawMessage.replace("{MAINCHANNEL}", gotziBot.getMainChannel().toString()));
            });

            gotziCommands.add(gotziCommand);
        }

        return gotziCommands;
    }













}
