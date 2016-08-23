package augoeides.requests.customfunctions;

import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import net.sf.json.JSONObject;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;

public class Rebirth
  implements IRequest
{
  public void process(String[] params, User user, World world, Room room)
    throws RequestException
  {
    Integer playerLevel = Integer.valueOf(Integer.parseInt(params[0]));
    Integer rebirthCount = (Integer)user.properties.get("rebirth");
    int maxLevel = 75;
    if ((playerLevel.intValue() == maxLevel) && (((Integer)user.properties.get("level")).intValue() == maxLevel)) {
      user.properties.put("rebirth", Integer.valueOf(rebirthCount.intValue() + 1));
      world.users.levelUp(user, 1);
      world.db.jdbc.run("UPDATE users SET Rebirth = (Rebirth + 1) WHERE id = ?", new Object[] { user.properties.get("dbId") });
      JSONObject UpdateRebirth = new JSONObject();
      UpdateRebirth.put("cmd", "updateRebirth");
      UpdateRebirth.put("intRebirth", (Integer)user.properties.get("rebirth"));
      world.send(UpdateRebirth, user);
      world.users.dropItem(user, 4, 1);
    } else if ((playerLevel.intValue() == maxLevel) && (((Integer)user.properties.get("level")).intValue() != maxLevel)) {
      world.send(new String[] { "server", "no" }, user);
      world.users.kick(user);
    } else {
      world.send(new String[] { "server", "Your level is not " + maxLevel + ", please go level up and rebirth when you reach the maximum level!" }, user);
    }
  }
}