package augoeides.tasks;

import augoeides.db.Database;

public class FreeDbPool implements Runnable {
   private Database db;

   public FreeDbPool(Database db) {
      super();
      this.db = db;
   }

   public void run() {
      this.db.freeIdleConnections();
   }
}
