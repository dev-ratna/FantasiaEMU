package augoeides.requests.party;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class PartyDeclineSummon implements IRequest {
   public PartyDeclineSummon() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      User client = world.zone.getUserByName(params[1].toLowerCase());
      if(client != null) {
         world.send(new String[]{"server", client.getName() + " declined your summon."}, client);
      }
   }
}
