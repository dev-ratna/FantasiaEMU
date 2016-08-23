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

public class SwitchGuildColor
  implements IRequest
{
  public void process(String[] var1, User var2, World var3, Room var4)
    throws RequestException
  {
    JSONObject var27 = (JSONObject)var2.properties.get("guildobj");
    if (((Integer)var2.properties.get("guildrank")).intValue() == 3)
    {
      String guildColor = var1[1];
      String guildStringColor = null;
      Integer colorPrice = null;
      int userGold = var3.db.jdbc.queryForInt("SELECT Coins FROM users WHERE id = ? FOR UPDATE", new Object[] { var2.properties.get("dbId") });
      if (guildColor.equals("1"))
      {
        guildStringColor = "GR";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("2"))
      {
        guildStringColor = "BL";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("3"))
      {
        guildStringColor = "PU";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("4"))
      {
        guildStringColor = "GO";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("5"))
      {
        guildStringColor = "BR";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("6"))
      {
        guildStringColor = "DB";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("7"))
      {
        guildStringColor = "PI";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("8"))
      {
        guildStringColor = "BG";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("9"))
      {
        guildStringColor = "CG";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("10"))
      {
        guildStringColor = "RE";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("11"))
      {
        guildStringColor = "DARKERBLUE";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("12"))
      {
        guildStringColor = "LIGHTGREEN";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("13"))
      {
        guildStringColor = "LIGHTBLUEISH";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("14"))
      {
        guildStringColor = "DARKPURPLEISH";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("15"))
      {
        guildStringColor = "BROWNISH";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("16"))
      {
        guildStringColor = "CreamIshColor";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("17"))
      {
        guildStringColor = "VioletIshColor";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("18"))
      {
        guildStringColor = "GreyIshColor";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("19"))
      {
        guildStringColor = "DarkBrownIshColor";
        colorPrice = Integer.valueOf(1500);
      }
      else if (guildColor.equals("20"))
      {
        guildStringColor = "LightRed";
        colorPrice = Integer.valueOf(1500);
      }
      else
      {
        throw new RequestException("That guild color does not exist!");
      }
      Integer userLeft = Integer.valueOf(userGold - colorPrice.intValue());
      if (userGold < colorPrice.intValue()) {
        throw new RequestException("You do not have enough gold to buy that guild color!");
      }
      var3.db.jdbc.beginTransaction();
      var27.put("guildColor", guildStringColor);
      var3.sendGuildUpdate(var27);
      var2.properties.put("gold", userLeft);
      JSONObject guildhall = new JSONObject();
      guildhall.put("cmd", "updateEntities");
      guildhall.put("intGold", userLeft);
      guildhall.put("bitSuccess", Integer.valueOf(1));
      var3.send(guildhall, var2);
      var3.db.jdbc.execute("UPDATE users SET Gold = ? WHERE id = ?", new Object[] { Integer.valueOf(userLeft.intValue()), var2.properties.get("dbId") });
      var3.db.jdbc.run("UPDATE guilds SET GuildColor='" + guildStringColor + "' WHERE Name = ?", new Object[] { var27.get("Name") });
      var3.send(new String[] { "server", "Congratulations! You have successfully updated your guild color!" }, var2);
      var3.db.jdbc.commitTransaction();
    }
    else
    {
      throw new RequestException("You do not have the required permission for this. Please contact the guild leader.");
    }
  }
}
