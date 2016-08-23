package augoeides.requests;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class TrapDoor implements IRequest {
   public TrapDoor() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      world.sendToRoom(new String[]{"trap door", params[0]}, user, room);
   }
}
