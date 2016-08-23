package augoeides.requests;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class EmoteMessage implements IRequest {
   public EmoteMessage() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      world.sendToRoom(new String[]{"em", user.getName(), params[0]}, user, room);
   }
}
