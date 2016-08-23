package augoeides.requests;

import augoeides.db.objects.Area;
import augoeides.db.objects.Cell;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.requests.MoveToCell;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class MoveToCellById implements IRequest {
   public MoveToCellById() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      Cell cell = (Cell)((Area)world.areas.get(room.getName().split("-")[0])).cells.get(Integer.valueOf(Integer.parseInt(params[0])));
      if((!cell.getFrame().equals("Enter0") || ((Integer)user.properties.get("pvpteam")).intValue() == 0) && (!cell.getFrame().equals("Enter1") || ((Integer)user.properties.get("pvpteam")).intValue() == 1)) {
         (new MoveToCell()).process(new String[]{cell.getFrame(), cell.getPad()}, user, world, room);
         world.send(new String[]{"mtcid", params[0]}, user);
      }
   }
}
