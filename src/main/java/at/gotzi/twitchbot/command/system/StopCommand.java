package at.gotzi.twitchbot.command.system;

import at.gotzi.api.command.GCommand;
import at.gotzi.twitchbot.GotziBot;

public class StopCommand extends GCommand {


    public StopCommand(GotziBot gotziBot) {
        super("stop", gotziCommandContext -> {
            gotziBot.stop();
        });
    }
}
