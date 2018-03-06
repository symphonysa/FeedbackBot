package bot;

import config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.exceptions.UsersClientException;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.*;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;

import java.util.HashSet;
import java.util.Set;

public class ChatBot implements ChatListener, ChatServiceListener {

    private static ChatBot instance;
    private final Logger logger = LoggerFactory.getLogger(ChatBot.class);
    private SymphonyClient symClient;
    private BotConfig config;


    protected ChatBot(SymphonyClient symClient, BotConfig config) {
        this.symClient=symClient;
        this.config = config;
        init();


    }

    public static ChatBot getInstance(SymphonyClient symClient, BotConfig config){
        if(instance==null){
            instance = new ChatBot(symClient,config);
        }
        return instance;
    }

    private void init() {


        symClient.getChatService().addListener(this);

        Chat chat = new Chat();
        chat.setLocalUser(symClient.getLocalUser());
        Set<SymUser> recipients = new HashSet<>();
        try {
            SymUser recipient = symClient.getUsersClient().getUserFromEmail(config.getUserEmailAddress());
            recipients.add(recipient);
            chat.setRemoteUsers(recipients);
            symClient.getChatService().addChat(chat);
            SymMessage reporterMessage = new SymMessage();
            reporterMessage.setMessageText("Test message");
            symClient.getMessagesClient().sendMessage(chat.getStream(), reporterMessage);
        } catch (UsersClientException e) {
            logger.error("Failed to find user", e);
        }catch (MessagesException e) {
            logger.error("Failed to send message", e);
        }

    }


    public void onChatMessage(SymMessage message) {
        if (message == null)
            return;
        logger.debug("TS: {}\nFrom ID: {}\nSymMessage: {}\nSymMessage Type: {}",
                message.getTimestamp(),
                message.getFromUserId(),
                message.getMessage(),
                message.getMessageType());
        SymMessage message2;

        if (message.getMessageText().toLowerCase().contains("test")) {


            message2 = new SymMessage();

            message2.setMessage("<messageML><div><b><i>Message Received.</i></b></div></messageML>");
            try {
                symClient.getMessagesClient().sendMessage(message.getStream(), message2);
            } catch (MessagesException e) {
                logger.error("Failed to send message", e);
            }

        }



    }



    @Override
    public void onNewChat(Chat chat) {

        chat.addListener(this);

        logger.debug("New chat session detected on stream {} with {}", chat.getStream().getStreamId(), chat.getRemoteUsers());
    }

    @Override
    public void onRemovedChat(Chat chat) {

    }
}
