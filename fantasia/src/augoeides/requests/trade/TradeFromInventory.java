package augoeides.requests.trade;

import augoeides.aqw.Rank;
import augoeides.db.objects.Enhancement;
import augoeides.db.objects.Item;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.requests.trade.TradeCancel;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.Map;
import jdbchelper.JdbcException;
import jdbchelper.QueryResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TradeFromInventory implements IRequest {
   public TradeFromInventory() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      int userDbId = ((Integer)user.properties.get("dbId")).intValue();
      int itemId = Integer.parseInt(params[0]);
      int charItemId = Integer.parseInt(params[1]);
      int quantity = Integer.parseInt(params[3]);
      Item item = (Item)world.items.get(Integer.valueOf(itemId));
      User client = SmartFoxServer.getInstance().getUserById(Integer.valueOf(Integer.parseInt(params[2])));
      if(client == null) {
         (new TradeCancel()).process(new String[]{Integer.toString(-1)}, user, world, room);
         throw new RequestException("Trade has been canceled due to other player can\'t be found!");
      } else if(user.getName().equals(client.getName())) {
         (new TradeCancel()).process(new String[]{Integer.toString(-1)}, user, world, room);
         throw new RequestException(client.getName() + " has canceled the trade.");
      } else if(item == null) {
         (new TradeCancel()).process(new String[]{Integer.toString(client.getUserId())}, user, world, room);
         throw new RequestException("Item could not be found.");
      } else if(client.getUserId() != ((Integer)user.properties.get("tradetgt")).intValue()) {
         (new TradeCancel()).process(new String[]{Integer.toString(-1)}, user, world, room);
         throw new RequestException(client.getName() + " has canceled the trade.");
      } else if(user.getUserId() != ((Integer)client.properties.get("tradetgt")).intValue()) {
         (new TradeCancel()).process(new String[]{Integer.toString(-1)}, user, world, room);
         throw new RequestException(client.getName() + " has canceled the trade.");
      } else if(item.isTemporary()) {
         (new TradeCancel()).process(new String[]{Integer.toString(client.getUserId())}, user, world, room);
         throw new RequestException("You\'re not able to trade temporary items!");
      } else if(quantity <= 0) {
         (new TradeCancel()).process(new String[]{Integer.toString(user.getUserId())}, client, world, world.zone.getRoom(client.getRoom()));
         SmartFoxServer.log.warning("Attempting to put negative item quantity amount: " + user.properties.get("username"));
         world.users.log(user, "Packet Edit [TradeFromInventory]", "Doing quantity black majiks");
         world.users.kick(user);
      } else {
         if(item.getFactionId() > 1) {
            Map je = (Map)client.properties.get("factions");
            JSONObject offers;
            if(!je.containsKey(Integer.valueOf(item.getFactionId()))) {
               offers = new JSONObject();
               offers.element("cmd", "tradeFromInv");
               offers.element("bitSuccess", 0);
               offers.element("msg", "Reputation requirement not met for " + client.getName() + "! " + item.getName() + " requires " + (String)world.factions.get(Integer.valueOf(item.getFactionId())) + ", Rank " + Rank.getRankFromPoints(item.getReqReputation()) + ".");
               world.send(offers, user);
               return;
            }

            if(((Integer)je.get(Integer.valueOf(item.getFactionId()))).intValue() < item.getReqReputation()) {
               offers = new JSONObject();
               offers.element("cmd", "tradeFromInv");
               offers.element("bitSuccess", 0);
               offers.element("msg", "Reputation requirement not met for " + client.getName() + "! " + item.getName() + " requires " + (String)world.factions.get(Integer.valueOf(item.getFactionId())) + ", Rank " + Rank.getRankFromPoints(item.getReqReputation()) + ".");
               world.send(offers, user);
            }
         }

         world.db.jdbc.beginTransaction();

         try {
            QueryResult je1 = world.db.jdbc.query("SELECT * FROM users_items WHERE id = ? FOR UPDATE", new Object[]{Integer.valueOf(charItemId)});
            if(je1.next() && userDbId == je1.getInt("UserID") && itemId == je1.getInt("ItemID")) {
               if(item.getEquipment().equals("ar")) {
                  quantity = je1.getInt("Quantity");
               }

               world.users.addOfferItem(user, itemId, quantity, je1.getInt("EnhID"));
               Map offers1 = (Map)user.properties.get("offer");
               JSONObject tfi;
               if(je1.getInt("Quantity") < ((Integer)offers1.get(Integer.valueOf(itemId))).intValue()) {
                  tfi = new JSONObject();
                  tfi.element("cmd", "tradeFromInv");
                  tfi.element("bitSuccess", 0);
                  tfi.element("msg", "Invalid quantity amount!");
                  world.send(tfi, user);
               } else {
                  tfi = new JSONObject();
                  tfi.element("cmd", "tradeFromInv");
                  tfi.element("ItemID", itemId);
                  tfi.element("bSuccess", 1);
                  tfi.element("Type", 1);
                  tfi.element("Quantity", offers1.get(Integer.valueOf(itemId)));
                  world.send(tfi, user);
                  JSONObject lb = new JSONObject();
                  JSONArray items = new JSONArray();
                  int enhId = je1.getInt("EnhID");
                  Enhancement enhancement = (Enhancement)world.enhancements.get(Integer.valueOf(enhId));
                  JSONObject itemObj = Item.getItemJSON(item, enhancement);
                  itemObj.put("CharItemID", je1.getString("id"));
                  itemObj.put("iQty", offers1.get(Integer.valueOf(itemId)));
                  itemObj.put("iReqCP", Integer.valueOf(item.getReqClassPoints()));
                  itemObj.put("iReqRep", Integer.valueOf(item.getReqReputation()));
                  itemObj.put("FactionID", Integer.valueOf(item.getFactionId()));
                  itemObj.put("sFaction", world.factions.get(Integer.valueOf(item.getFactionId())));
                  items.add(itemObj);
                  lb.put("cmd", "loadOffer");
                  lb.put("itemsB", items);
                  lb.put("bitSuccess", Integer.valueOf(1));
                  world.send(lb, client);
                  if(((Boolean)user.properties.get("tradelock")).booleanValue() || ((Boolean)client.properties.get("tradelock")).booleanValue()) {
                     user.properties.put("tradelock", Boolean.valueOf(false));
                     user.properties.put("tradedeal", Boolean.valueOf(false));
                     client.properties.put("tradelock", Boolean.valueOf(false));
                     client.properties.put("tradedeal", Boolean.valueOf(false));
                     JSONObject tr = new JSONObject();
                     tr.put("cmd", "tradeUnlock");
                     tr.put("bitSuccess", Integer.valueOf(1));
                     world.send(tr, user);
                     world.send(tr, client);
                  }
               }
            }

            je1.close();
         } catch (JdbcException var23) {
            if(world.db.jdbc.isInTransaction()) {
               world.db.jdbc.rollbackTransaction();
            }

            SmartFoxServer.log.severe("Error in trade from inventory transaction: " + var23.getMessage());
         } finally {
            if(world.db.jdbc.isInTransaction()) {
               world.db.jdbc.commitTransaction();
            }

         }

      }
   }
}
