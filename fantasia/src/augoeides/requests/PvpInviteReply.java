package augoeides.requests;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import net.sf.json.JSONObject;

public class PvpInviteReply implements IRequest {
   public PvpInviteReply() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      JSONObject PVPQ = new JSONObject();
      PVPQ.put("cmd", "PVPQ");
      PVPQ.put("bitSuccess", Integer.valueOf(0));
      if(params[0].equals("1")) {
         Room warzoneRoom = (Room)user.properties.get("roomqueued");
         world.rooms.joinRoom(warzoneRoom, user, "Enter" + user.properties.get("pvpteam"), "Spawn");
      }

      world.send(PVPQ, user);
   }
}
