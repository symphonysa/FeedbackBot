package mongo;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.SectorRoom;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.symphonyoss.symphony.clients.model.SymMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Updates.combine;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDBClient {
    private MongoCollection<SectorRoom> sectoRoomCollection;

    public MongoDBClient(String mongoURL) {
        MongoClientURI connectionString = new MongoClientURI(mongoURL);
        MongoClient mongoClient = new MongoClient(connectionString);
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoDatabase database = mongoClient.getDatabase("FeedbackBot");

        database = database.withCodecRegistry(pojoCodecRegistry);
        sectoRoomCollection = database.getCollection("SectorRoom", SectorRoom.class);

    }

    public SectorRoom getSectorRoom(String sector){
        return sectoRoomCollection.find(eq("sector",sector)).first();
    }

    public List<SectorRoom> getAllSectorRooms(){
        List sectorRooms = new ArrayList();
        sectoRoomCollection.find().forEach(new Block<SectorRoom>() {

            @Override
            public void apply(final SectorRoom researchInterest) {
                sectorRooms.add(researchInterest);
            }
        });
        return sectorRooms;
    }

}
