package augoeides.requests;

import augoeides.db.objects.Area;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class GetMapItem implements IRequest {
   public GetMapItem() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      Area area = (Area)world.areas.get(room.getName().split("-")[0]);
      int itemId = Integer.parseInt(params[0]);
      if(!area.items.isEmpty() && area.items.contains(Integer.valueOf(itemId))) {
         world.users.dropItem(user, itemId);
      }

   }
}
