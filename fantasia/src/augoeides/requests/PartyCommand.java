package augoeides.requests;

import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.requests.party.PartyAccept;
import augoeides.requests.party.PartyAcceptSummon;
import augoeides.requests.party.PartyDecline;
import augoeides.requests.party.PartyDeclineSummon;
import augoeides.requests.party.PartyInvite;
import augoeides.requests.party.PartyKick;
import augoeides.requests.party.PartyLeave;
import augoeides.requests.party.PartyPromote;
import augoeides.requests.party.PartySummon;
import augoeides.world.World;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;

public class PartyCommand implements IRequest {
   public PartyCommand() {
      super();
   }

   public void process(String[] params, User user, World world, Room room) throws RequestException {
      if(params[0].equals("pi")) {
         (new PartyInvite()).process(params, user, world, room);
      } else if(params[0].equals("pk")) {
         (new PartyKick()).process(params, user, world, room);
      } else if(params[0].equals("pl")) {
         (new PartyLeave()).process(params, user, world, room);
      } else if(params[0].equals("ps")) {
         (new PartySummon()).process(params, user, world, room);
      } else if(params[0].equals("psa")) {
         (new PartyAcceptSummon()).process(params, user, world, room);
      } else if(params[0].equals("psd")) {
         (new PartyDeclineSummon()).process(params, user, world, room);
      } else if(params[0].equals("pp")) {
         (new PartyPromote()).process(params, user, world, room);
      } else if(params[0].equals("pa")) {
         (new PartyAccept()).process(params, user, world, room);
      } else if(params[0].equals("pd")) {
         (new PartyDecline()).process(params, user, world, room);
      }

   }
}
