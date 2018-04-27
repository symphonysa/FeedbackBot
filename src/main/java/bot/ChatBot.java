package bot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import config.BotConfig;
import mongo.MongoDBClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.exceptions.UsersClientException;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.*;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.pod.model.Stream;

import java.util.HashSet;
import java.util.Set;

public class ChatBot implements ChatListener, ChatServiceListener {

    private static ChatBot instance;
    private final Logger logger = LoggerFactory.getLogger(ChatBot.class);
    private SymphonyClient symClient;
    private BotConfig config;
    private MongoDBClient mongoDBClient;


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
        mongoDBClient = new MongoDBClient(config.getMongoURL());

    }


    public void onChatMessage(SymMessage message) {
        if (message == null)
            return;
        logger.debug("TS: {}\nFrom ID: {}\nSymMessage: {}\nSymMessage Type: {}",
                message.getTimestamp(),
                message.getFromUserId(),
                message.getMessage(),
                message.getMessageType());

        Stream stream = new Stream();
        stream.setId(mongoDBClient.getSectorRoom("admin").getStreamId());


            try {
                String presentationML = message.getMessage();
                int endoftag = presentationML.indexOf(">");
                String start = presentationML.substring(0, endoftag + 1);
                String resultmessage = start.concat(" Feedback from <span class=\"entity\" data-entity-id=\"mentionAdded\">@" + message.getSymUser().getDisplayName() + "</span>: <br/> ");
                String end = presentationML.substring(endoftag + 1, presentationML.length());
                resultmessage = resultmessage.concat(end);
                String data = message.getEntityData();
                JsonParser jsonParser = new JsonParser();
                JsonElement json = jsonParser.parse(data);
                JsonObject object = json.getAsJsonObject();
                JsonObject mentionValue = jsonParser.parse("{\"type\":\"com.symphony.user.mention\",\"version\":\"1.0\",\"id\":[{\"type\":\"com.symphony.user.userId\",\"value\":\"" + message.getSymUser().getId() + "\"}]}").getAsJsonObject();
                object.add("mentionAdded", mentionValue);
                String resultdata = object.toString();


                SymMessage researchmessage = new SymMessage();
                researchmessage.setMessage(resultmessage);
                researchmessage.setEntityData(resultdata);

                symClient.getMessagesClient().sendMessage(stream, researchmessage);
            } catch (MessagesException e) {
                logger.error("Failed to send message", e);
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
