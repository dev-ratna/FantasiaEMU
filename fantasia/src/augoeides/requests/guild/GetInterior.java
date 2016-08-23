package augoeides.requests.guild;

import augoeides.db.objects.Item;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import net.sf.json.JSONObject;

public class GetInterior implements IRequest {
   public GetInterior() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      JSONObject interior = new JSONObject();
      JSONObject items = new JSONObject();

      for(int i = 1; i < params.length; ++i) {
         String itemId = params[1];
         Item item = (Item)world.items.get(Integer.valueOf(Integer.parseInt(itemId)));
         items.put(itemId, Item.getItemJSON(item));
      }

      interior.put("cmd", "interior");
      interior.put("items", items);
      world.send(interior, user);
   }
}
