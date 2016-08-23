package augoeides.tasks;

import augoeides.world.World;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.data.User;
import java.util.concurrent.TimeUnit;

public class Shutdown implements Runnable {
   private World world;
   private User user;

   public Shutdown(World world, User user) {
      super();
      this.world = world;
      this.user = user;
   }

   public void run() {
      try {
         this.world.send(new String[]{"server", "Server shutting down in 5 minutes."}, this.world.zone.getChannelList());
         Thread.sleep(TimeUnit.MINUTES.toMillis(4L));
         this.world.send(new String[]{"warning", "Server shutting down in 1 minute."}, this.world.zone.getChannelList());
         Thread.sleep(TimeUnit.MINUTES.toMillis(1L));
         this.world.send(new String[]{"logoutWarning", "", "60"}, this.world.zone.getChannelList());
         this.world.shutdown();
         Thread.sleep(TimeUnit.SECONDS.toMillis(2L));
         SmartFoxServer.getInstance().halt(this.user);
         Thread.sleep(TimeUnit.SECONDS.toMillis(5L));
         System.exit(0);
      } catch (InterruptedException var2) {
         ;
      }

   }
}
