package at.gotzi.twitchbot.command.system;

import at.gotzi.api.command.GArgument;
import at.gotzi.api.command.GArgumentValue;
import at.gotzi.api.command.GCommand;
import at.gotzi.twitchbot.GotziBot;
import at.gotzi.twitchbot.api.chat.Channel;

public class MessageCommand extends GCommand {


    public MessageCommand(GotziBot gotziBot) {
        super("message");
        build(gotziBot);
    }

    private void build(GotziBot gotziBot) {
        addArgument(new GArgumentValue(0, new GArgument[]{new GArgumentValue(1, gotziCommandContext -> {
            System.out.println(gotziCommandContext.args()[1] + " " + gotziCommandContext.args()[0]);
            gotziBot.sendMessage(gotziCommandContext.args()[1], Channel.getChannel(gotziCommandContext.args()[0], gotziBot));
        })}, null));
    }
}