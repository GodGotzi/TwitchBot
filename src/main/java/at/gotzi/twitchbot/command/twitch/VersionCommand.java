package at.gotzi.twitchbot.command.twitch;

import at.gotzi.api.command.GCommand;
import at.gotzi.twitchbot.GotziBot;
import at.gotzi.twitchbot.api.chat.Channel;
import at.gotzi.twitchbot.api.chat.User;

public class VersionCommand extends GCommand {

    public VersionCommand(GotziBot gotziBot) {
        super("version", gotziCommandContext -> {
            User user = (User) gotziCommandContext.objects()[0];
            Channel channel = (Channel) gotziCommandContext.objects()[1];
            gotziBot.sendMessage("@" + user.toString() +  " Version: " + GotziBot.VERSION, channel);
        });
    }
}