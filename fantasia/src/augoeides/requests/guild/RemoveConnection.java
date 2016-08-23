package augoeides.requests.guild;

import augoeides.aqw.Pad;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import net.sf.json.JSONObject;

public class RemoveConnection implements IRequest {
   public RemoveConnection() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      String curCell = params[1];
      String toCell = params[2];
      String pad = params[3];
      int guildId = ((Integer)user.properties.get("guildid")).intValue();
      int hallId = world.db.jdbc.queryForInt("SELECT id FROM guilds_halls WHERE Cell = ? AND GuildID = ?", new Object[]{curCell, Integer.valueOf(guildId)});
      int otherHallId = world.db.jdbc.queryForInt("SELECT id FROM guilds_halls WHERE Cell = ? AND GuildID = ?", new Object[]{toCell, Integer.valueOf(guildId)});
      world.db.jdbc.run("DELETE FROM guilds_halls_connections WHERE HallID = ? AND Pad = ? AND Cell = ? AND PadPosition = ?", new Object[]{Integer.valueOf(hallId), pad, toCell, Pad.getPad(pad)});
      world.db.jdbc.run("DELETE FROM guilds_halls_connections WHERE HallID = ? AND Pad = ? AND Cell = ? AND PadPosition = ?", new Object[]{Integer.valueOf(otherHallId), Pad.getPair(pad), curCell, Pad.getPad(Pad.getPair(pad))});
      JSONObject guildhall = new JSONObject();
      JSONObject cellB = new JSONObject();
      JSONObject cellA = new JSONObject();
      cellB.put("strFrame", toCell);
      cellB.put("strConnections", world.users.getConnectionsString(otherHallId));
      cellA.put("strFrame", curCell);
      cellA.put("strConnections", world.users.getConnectionsString(hallId));
      guildhall.put("cmd", "guildhall");
      guildhall.put("gCmd", "removeconnection");
      guildhall.put("cellA", cellA);
      guildhall.put("cellB", cellB);
      guildhall.put("doDelete", Boolean.valueOf(true));
      world.sendToRoom(guildhall, user, room);
   }
}
