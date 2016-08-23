package augoeides.db;

import augoeides.config.ConfigData;
import augoeides.db.SFSDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import it.gotoandplay.smartfoxserver.SmartFoxServer;
import it.gotoandplay.smartfoxserver.db.DbManager;
import java.sql.SQLException;
import jdbchelper.ConnectionPool;
import jdbchelper.JdbcHelper;
import jdbchelper.PooledDataSource;

public class Database {
   public JdbcHelper jdbc;
   private MysqlConnectionPoolDataSource source;
   private ConnectionPool pool;

   public Database() {
      this(50);
   }

   public Database(int maxPoolSize) {
      super();
      this.source = new MysqlConnectionPoolDataSource();
      this.source.setServerName(ConfigData.DB_HOST);
      this.source.setPort(ConfigData.DB_PORT);
      this.source.setUser(ConfigData.DB_USERNAME);
      this.source.setPassword(ConfigData.DB_PASSWORD);
      this.source.setDatabaseName(ConfigData.DB_NAME);
      this.source.setAutoReconnectForConnectionPools(true);
      this.pool = new ConnectionPool(this.source, maxPoolSize);
      this.jdbc = new JdbcHelper(new PooledDataSource(this.pool));
      SmartFoxServer.log.info("Database connections initialized.");
   }

   /** @deprecated */
   @Deprecated
   public Database(DbManager db) {
      super();
      this.jdbc = new JdbcHelper(new SFSDataSource(db));
      SmartFoxServer.log.info("Database connections initialized.");
   }

   public void freeIdleConnections() {
      this.pool.freeIdleConnections();
   }

   public int getActiveConnections() {
      return this.pool.getActiveConnections();
   }

   public void destroy() {
      try {
         this.pool.dispose();
         this.pool = null;
         this.jdbc = null;
         SmartFoxServer.log.info("Database connections destroyed.");
      } catch (SQLException var2) {
         SmartFoxServer.log.severe("Error diposing connection pool: " + var2.getMessage());
      }

   }
}
