package augoeides.requests;

import augoeides.db.objects.Area;
import augoeides.db.objects.Quest;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Set;

public class AcceptQuest implements IRequest {
   public AcceptQuest() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      Set quests = (Set)user.properties.get("quests");
      int questId = Integer.parseInt(params[0]);
      quests.add(Integer.valueOf(questId));
      Quest quest = (Quest)world.quests.get(Integer.valueOf(questId));
      if(!quest.locations.isEmpty()) {
         int mapId = ((Area)world.areas.get(room.getName().split("-")[0])).getId();
         if(!quest.locations.contains(Integer.valueOf(mapId))) {
            world.users.log(user, "Invalid Quest Accept", "Quest accept triggered at different location.");
         }
      }

   }
}
