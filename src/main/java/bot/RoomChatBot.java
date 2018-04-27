package bot;

import config.BotConfig;
import model.SectorRoom;
import mongo.MongoDBClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.events.*;
import org.symphonyoss.client.exceptions.MessagesException;
import org.symphonyoss.client.exceptions.SymException;
import org.symphonyoss.client.exceptions.UsersClientException;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.Room;
import org.symphonyoss.client.services.RoomEventListener;
import org.symphonyoss.client.services.RoomService;
import org.symphonyoss.client.services.RoomServiceEventListener;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.pod.model.MemberInfo;
import org.symphonyoss.symphony.pod.model.MembershipList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoomChatBot implements RoomServiceEventListener, RoomEventListener {

    private static RoomChatBot instance;
    private final Logger logger = LoggerFactory.getLogger(RoomChatBot.class);
    private SymphonyClient symClient;
    private RoomService roomService;
    private BotConfig config;
    private MongoDBClient mongoDBClient;

    protected RoomChatBot(SymphonyClient symClient, BotConfig config) {
        this.symClient=symClient;
        this.config = config;
        init();


    }

    public static RoomChatBot getInstance(SymphonyClient symClient, BotConfig config){
        if(instance==null){
            instance = new RoomChatBot(symClient,config);
        }
        return instance;
    }

    private void init() {

        roomService = symClient.getRoomService();
        roomService.addRoomServiceEventListener(this);
        mongoDBClient = new MongoDBClient(config.getMongoURL());

    }

    @Override
    public void onRoomMessage(SymMessage message) {


    }

    @Override
    public void onNewRoom(Room room) {
        room.addEventListener(this);
    }

    @Override
    public void onMessage(SymMessage message) {
        if (message == null)
            return;
        logger.debug("TS: {}\nFrom ID: {}\nSymMessage: {}\nSymMessage Type: {}",
                message.getTimestamp(),
                message.getFromUserId(),
                message.getMessage(),
                message.getMessageType());
        SymMessage message2;
        String streamId = mongoDBClient.getSectorRoom("admin").getStreamId();
        if (message.getStream().getStreamId().equals(streamId) && message.getMessageText().toLowerCase().contains("feedbackrequest")) {


            message2 = new SymMessage();

            message2.setMessage("<messageML><div><b><i>Feedback request</i></b><br/>"+message.getMessageText()+"</div></messageML>");
            message2.setEntityData(message.getEntityData());
            List<SectorRoom> sectorRoomList = mongoDBClient.getAllSectorRooms();

            for (SectorRoom sectorRoom: sectorRoomList) {
                if(!sectorRoom.getSector().equals("admin")){
                    MembershipList memberList;
                    try {
                        memberList = symClient.getRoomMembershipClient().getRoomMembership(message.getStreamId());
                        for (MemberInfo user : memberList) {
                            SymUser roomMember = symClient.getUsersClient().getUserFromId(user.getId());
                            if(!roomMember.getEmailAddress().equals(config.getBotEmailAddress())){
                            Chat chat = new Chat();
                            chat.setLocalUser(symClient.getLocalUser());
                            Set<SymUser> recipients = new HashSet<>();
                            recipients.add(roomMember);
                            chat.setRemoteUsers(recipients);
                            symClient.getChatService().addChat(chat);
                            symClient.getMessagesClient().sendMessage(chat.getStream(), message2);
                            }
                        }
                    } catch (SymException e) {
                        e.printStackTrace();
                    }
                }
            }


        }
    }

    @Override
    public void onSymRoomDeactivated(SymRoomDeactivated symRoomDeactivated) {

    }

    @Override
    public void onSymRoomMemberDemotedFromOwner(SymRoomMemberDemotedFromOwner symRoomMemberDemotedFromOwner) {

    }

    @Override
    public void onSymRoomMemberPromotedToOwner(SymRoomMemberPromotedToOwner symRoomMemberPromotedToOwner) {

    }

    @Override
    public void onSymRoomReactivated(SymRoomReactivated symRoomReactivated) {

    }

    @Override
    public void onSymRoomUpdated(SymRoomUpdated symRoomUpdated) {

    }

    @Override
    public void onSymUserJoinedRoom(SymUserJoinedRoom symUserJoinedRoom) {

    }

    @Override
    public void onSymUserLeftRoom(SymUserLeftRoom symUserLeftRoom) {

    }

    @Override
    public void onSymRoomCreated(SymRoomCreated symRoomCreated) {

    }

}
