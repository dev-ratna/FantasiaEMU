package augoeides.requests;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class EmoteAction implements IRequest {
   public EmoteAction() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      world.sendToRoomButOne(new String[]{"emotea", params[0], Integer.toString(user.getUserId())}, user, room);
   }
}
