package augoeides.requests.guild;

import augoeides.db.Database;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Map;
import jdbchelper.JdbcHelper;
import net.sf.json.JSONObject;

public class ChangeGuildColor
  implements IRequest
{
  public void process(String[] params, User user, World world, Room room)
    throws RequestException
  {
    if (((Integer)user.properties.get("guildrank")).intValue() == 3)
    {
      int userCoins = world.db.jdbc.queryForInt("SELECT Coins FROM users WHERE id = ? FOR UPDATE", new Object[] { user.properties.get("dbId") });
      if (userCoins < 2000) {
        throw new RequestException("You do not have enough coins to buy that guild color! Every guild color costs 1000 Coins.");
      }
      JSONObject var27 = (JSONObject)user.properties.get("guildobj");
      int color = Integer.parseInt(params[0]);
      var27.put("Color", Integer.valueOf(color));
      world.db.jdbc.execute("UPDATE users SET Coins=Coins-1000 WHERE id = ?", new Object[] { user.properties.get("dbId") });
      world.db.jdbc.run("UPDATE guilds SET GuildColor = ?, Color = ? WHERE Name = ?", new Object[] { Integer.valueOf(0xFFFFFF & color), Integer.toHexString(0xFFFFFF & color).toUpperCase(), var27.get("Name") });
      world.send(new String[] { "server", "You have successfully updated your guild color!" }, user);
      JSONObject cgc = new JSONObject();
      cgc.put("uid", Integer.valueOf(user.getUserId()));
      cgc.put("cmd", "changeGuildColor");
      cgc.put("intColorGuildName", Integer.valueOf(color));
      world.sendToRoomButOne(cgc, user, room);
    }
    else
    {
      throw new RequestException("You do not have the required permission for this. Please contact the guild leader.");
    }
  }
}
