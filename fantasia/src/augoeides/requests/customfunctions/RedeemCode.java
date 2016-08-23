package augoeides.requests.customfunctions;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import jdbchelper.QueryResult;
import net.sf.json.JSONObject;

public class RedeemCode implements IRequest {
   public RedeemCode() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
    String codeName = params[0].trim().toLowerCase();
    QueryResult reResult = world.db.jdbc.query("SELECT * FROM redeem_codes WHERE Code = ?", new Object[] { codeName });
    if (reResult.next()) {
      double leftTime = world.db.jdbc.queryForDouble("SELECT TIMESTAMPDIFF(SECOND, NOW(), ?)", new Object[] { reResult.getString("DateExpiry") });
      leftTime = leftTime >= 0 ? leftTime : 0;
      if (leftTime > 0) {
        int userRedeemCount = world.db.jdbc.queryForInt("SELECT COUNT(*) AS rowcount FROM users_redeems WHERE UserID = ? AND RedeemID = ?", new Object[] { user.properties.get("dbId"), Integer.valueOf(reResult.getInt("id")) });
        if (userRedeemCount < 1) {
          if (reResult.getInt("Coins") > 0) {
            JSONObject SkrubLord = new JSONObject();
            SkrubLord.put("cmd", "sellItem");
            SkrubLord.put("intAmount", Integer.valueOf(reResult.getInt("Coins")));
            SkrubLord.put("CharItemID", Integer.valueOf(user.hashCode()));
            SkrubLord.put("bCoins", Integer.valueOf(1));
            world.send(SkrubLord, user);
            world.db.jdbc.run("UPDATE users SET Coins = (Coins + ?) WHERE id=?", new Object[] { Integer.valueOf(reResult.getInt("Coins")), user.properties.get("dbId") });
          }

           if(reResult.getInt("Gold") > 0) {
                  world.users.giveRewards(user, 0, reResult.getInt("Gold"), 0, 0, -1, user.getUserId(), "p");
               }

               if(reResult.getInt("Exp") > 0) {
                  world.users.giveRewards(user, reResult.getInt("Exp"), 0, 0, 0, -1, user.getUserId(), "p");
               }

               if(reResult.getInt("ClassPoints") > 0) {
                  world.users.giveRewards(user, 0, 0, reResult.getInt("ClassPoints"), 0, -1, user.getUserId(), "p");
               }

               if(reResult.getInt("ItemID") > 0) {
                  world.users.dropItem(user, reResult.getInt("ItemID"));
               }

          if (reResult.getInt("UpgradeDays") > 0) {
            int SkrubLord1 = reResult.getInt("UpgradeDays") * 24 * 60;
            QueryResult upgradeExpire = world.db.jdbc.query("SELECT UpgradeExpire FROM users WHERE id = ?", new Object[] { user.properties.get("dbId") });
            if (upgradeExpire.next()) {
              int k = world.db.jdbc.queryForInt("SELECT TIMESTAMPDIFF(MINUTE, NOW(), ?)", new Object[] { upgradeExpire.getString("UpgradeExpire") });
              k = k >= 0 ? k : 0;
              world.db.jdbc.run("UPDATE users SET UpgradeExpire = DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE id = ?", new Object[] { Integer.valueOf(SkrubLord1 + k), user.properties.get("dbId") });
            }

            upgradeExpire.close();
          }

          world.send(new String[] { "server", "From the code, you were granted "+ reResult.getInt("Gold") +" gold coins, "   + reResult.getInt("Coins") + " adventure coins, " + reResult.getInt("Exp") + " experience points, " + reResult.getInt("ClassPoints") + " class points, and " + reResult.getInt("UpgradeDays") + " days of membership! Good luck and enjoy the server!!" }, user);
          world.db.jdbc.run("INSERT INTO users_redeems (RedeemID, UserID, Date) VALUES (?, ?, NOW())", new Object[] { Integer.valueOf(reResult.getInt("id")), user.properties.get("dbId") });
          reResult.close();
        } else {
          reResult.close();
          throw new RequestException("You already redeemed this code!");
        }
      } else {
        reResult.close();
        throw new RequestException("The code you're trying to redeem is already expired!");
      }
    } else {
      reResult.close();
      throw new RequestException("The code you're trying to redeem is invalid!");
    }
  }
}