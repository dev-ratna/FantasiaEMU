package augoeides.requests;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class DragonBuff implements IRequest {
   public DragonBuff() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      world.send(new String[]{"Dragon Buff"}, user);
   }
}
