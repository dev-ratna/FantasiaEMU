package augoeides.requests;

import augoeides.ai.MonsterAI;
import augoeides.aqw.Rank;
import augoeides.db.Database;
import augoeides.db.objects.Area;
import augoeides.db.objects.Aura;
import augoeides.db.objects.AuraEffects;
import augoeides.db.objects.Item;
import augoeides.db.objects.Monster;
import augoeides.db.objects.Skill;
import augoeides.dispatcher.IRequest;
import augoeides.dispatcher.RequestException;
import augoeides.tasks.DamageOverTime;
import augoeides.tasks.RemoveAura;
import augoeides.world.Rooms;
import augoeides.world.Users;
import augoeides.world.World;
import augoeides.world.stats.Stats;
import it.gotoandplay.smartfoxserver.data.Room;
import it.gotoandplay.smartfoxserver.data.User;
import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import jdbchelper.JdbcHelper;
import jdbchelper.NoResultException;
import jdbchelper.QueryResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Action
        implements IRequest {

    private static final Random rand = new Random();

    public void process(String[] params, User user, World world, Room room)
            throws RequestException {
        if (((Integer) user.properties.get("state")).intValue() != 0) {
            int actId = Integer.parseInt(params[0]);
            String skillReference = getSkillRefence(params[1]);
            String tInf = parseTargetInfo(params[1]);
            rand.setSeed(actId);
            JSONObject ct = new JSONObject();
            JSONArray sarsa = new JSONArray();
            JSONObject sarsaObj = new JSONObject();
            JSONArray anims = new JSONArray();
            JSONObject anim = new JSONObject();
            JSONArray auras = new JSONArray();
            JSONArray a = new JSONArray();
            JSONObject p = new JSONObject();
            JSONObject m = new JSONObject();
            Stats stats = (Stats) user.properties.get("stats");
            String fromTarget = "p:" + user.getUserId();
            Area area = (Area) world.areas.get(room.getName().split("-")[0]);
            Map skills = (Map) user.properties.get("skills");
            Skill skill;
            try {
                skill = (Skill) world.skills.get(skills.get(skillReference));
                if (skill == null) {
                    return;
                }
                int userMana = Rank.getRankFromPoints(((Integer) user.properties.get("cp")).intValue());
                if (((userMana < 2) && (skill.getReference().equals("a2"))) || ((userMana < 3) && (skill.getReference().equals("a3"))) || ((userMana < 5) && (skill.getReference().equals("a4")))) {
                    world.users.log(user, "Packet Edit [Action]", "Using a skill when designated rank is not yet achieved.");
                    return;
                }
                if (skill.getReference().equals("i1")) {
                    int manaIncrease = world.db.jdbc.queryForInt("SELECT id FROM items WHERE Meta = ?", new Object[]{Integer.valueOf(skill.getId())});
                    if (!world.users.turnInItem(user, manaIncrease, 1)) {
                        world.users.log(user, "Packet Edit [Action]", "TurnIn failed when using potions.");
                        return;
                    }
                }
                if ((user.properties.get("perfecttimings") != null) && (!skill.getReference().equals("aa"))) {
                    Long var52 = Long.valueOf(System.currentTimeMillis());
                    Long maxMana = Long.valueOf(((Long) user.properties.get("perfecttimings")).longValue() + skill.getCooldown() - skill.getCooldown() / 2 - 500L);
                    Integer targets = (Integer) user.properties.get("requestbotcounter");
                    if (targets == null) {
                        user.properties.put("requestbotcounter", Integer.valueOf(0));
                    } else if (maxMana.longValue() > var52.longValue()) {
                        user.properties.put("requestbotcounter", Integer.valueOf(0));
                    } else if (targets.intValue() >= 5) {
                        /*
                        world.sendServerMessage(user.getName() + " is suspected of botting thus kicked by the server!");
                        world.users.kick(user);
                         */
                    } else {
                        user.properties.put("requestbotcounter", Integer.valueOf(targets.intValue() + 1));
                    }
                }
                if (user.properties.get(skill.getReference()) != null) {
                    long var53 = System.currentTimeMillis();
                    long var55 = ((Long) user.properties.get(skill.getReference())).longValue() + skill.getCooldown() - skill.getCooldown() / 2 - 500L;
                    int inputSet = ((Integer) user.properties.get("requestwarncounter")).intValue();
                    if (var55 > var53) {
                        if (user.properties.get("language").equals("BR")) {
                            world.send(new String[]{"warning", "Medidas tomadas muito rapidamente, tente novamente em um momento."}, user);
                        } else {
                            world.send(new String[]{"warning", "Action taken too quickly, try again in a moment."}, user);
                        }
                        user.properties.put("requestwarncounter", Integer.valueOf(inputSet + 1));
                        return;
                    }
                    user.properties.put("requestwarncounter", Integer.valueOf(0));
                }
            } catch (NoResultException var51) {
                throw new UnsupportedOperationException("Unassigned skill ID: " + skillReference);
            }

            int userMana = ((Integer) user.properties.get("mp")).intValue() - skill.getMana();
            int manaIncrease = (int) (stats.get_INT() + stats.get_INT() / 2.0D);
            userMana += rand.nextInt(Math.abs(manaIncrease));
            int var54 = ((Integer) user.properties.get("mpmax")).intValue();
            userMana = userMana >= var54 ? var54 : userMana;
            user.properties.put("mp", Integer.valueOf(userMana));
            String[] var56 = tInf.split(",");
            List inputList = Arrays.asList(var56);
            HashSet var57 = new HashSet(inputList);
            if (var57.size() < inputList.size()) {
                world.db.jdbc.run("UPDATE users SET Access = 0, PermamuteFlag = 1 WHERE id = ?", new Object[]{user.properties.get("dbId")});
                world.users.kick(user);
                world.users.log(user, "Packet Edit [gar]", "Attack packet hack.");
            }
            ConcurrentHashMap monsters = (ConcurrentHashMap) room.properties.get("monsters");
            String[] arr$ = var56;
            int len$ = var56.length;
            for (int i$ = 0; i$ < len$; i$++) {
                String target = arr$[i$];
                String tgtType = target.split(":")[0];
                int tgtId = Integer.parseInt(target.split(":")[1]);
                int damage;
                if ((((Integer) user.properties.get("access")).intValue()) == 61) {
                    damage = 1000000000;
                    //getRandomDamage(stats, skill);

                } else {
                    damage = getRandomDamage(stats, skill);
                }
                boolean dodge = false;
                boolean crit = Math.random() < stats.get$tcr();
                boolean miss = Math.random() > 1.0D - ((Double) world.coreValues.get("baseMiss")).doubleValue() + stats.get$thi();
                Set userAuras = (Set) user.properties.get("auras");
                Iterator tgtInfo = userAuras.iterator();
                while (tgtInfo.hasNext()) {
                    RemoveAura damageResult = (RemoveAura) tgtInfo.next();
                    Aura userStats = damageResult.getAura();
                    if (!userStats.getCategory().equals("d")) {
                        damage = (int) (damage * (1.0D + userStats.getDamageIncrease()));
                    }
                }
                damage = (int) (crit ? damage * 1.5D : miss ? 0.0D : damage);
                if ((damage > 0) && (!skill.getReference().equals("i1")) && (user.getUserId() != tgtId)) {
                    user.properties.put("state", Integer.valueOf(2));
                }
                JSONObject var58 = new JSONObject();
                if (tgtType.equals("m")) {
                    MonsterAI var59 = (MonsterAI) monsters.get(Integer.valueOf(tgtId));
                    if (var59 != null) {
                        Item var62 = (Item) user.properties.get("weaponitem");
                        String userItem = ((Monster) world.monsters.get(Integer.valueOf(var59.getMonsterId()))).getElement();
                        byte element = -1;
                        switch (userItem.hashCode()) {
                            case -1604554070:
                                if (userItem.equals("Lightning")) {
                                    element = 5;
                                }
                                break;
                            case 2122646:
                                if (userItem.equals("Dark")) {
                                    element = 3;
                                }
                                break;
                            case 2189910:
                                if (userItem.equals("Fire")) {
                                    element = 0;
                                }
                                break;
                            case 2696232:
                                if (userItem.equals("Wind")) {
                                    element = 4;
                                }
                                break;
                            case 73417974:
                                if (userItem.equals("Light")) {
                                    element = 2;
                                }
                                break;
                            case 83350775:
                                if (userItem.equals("Water")) {
                                    element = 1;
                                }
                                break;
                        }
                        switch (element) {
                            case 0:
                                if (var62.getElement().equals("Water")) {
                                    damage += (int) (damage * 0.1D);
                                }
                                break;
                            case 1:
                                if (var62.getElement().equals("Fire")) {
                                    damage += (int) (damage * 0.1D);
                                }
                                break;
                            case 2:
                                if (var62.getElement().equals("Dark")) {
                                    damage += (int) (damage * 0.1D);
                                }
                                break;
                            case 3:
                                if (var62.getElement().equals("Light")) {
                                    damage += (int) (damage * 0.1D);
                                }
                                break;
                            case 4:
                                if (var62.getElement().equals("Lightning")) {
                                    damage += (int) (damage * 0.1D);
                                }
                                break;
                            case 5:
                                if (var62.getElement().equals("Wind")) {
                                    damage += (int) (damage * 0.1D);
                                }
                                break;
                        }
                        Set tgtUserItem = var59.getAuras();
                        Iterator var72 = tgtUserItem.iterator();
                        while (var72.hasNext()) {
                            RemoveAura userTgtHp = (RemoveAura) var72.next();
                            Aura tgtList = userTgtHp.getAura();
                            if (!tgtList.getCategory().equals("d")) {
                                damage = (int) (damage * (1.0D + tgtList.getDamageIncrease()));
                            }
                        }
                        var59.setHealth(var59.getHealth() - damage);
                        var59.addTarget(user.getUserId());
                        Set var74 = var59.getTargets();
                        if ((var59.getHealth() <= 0) && (var59.getState() != 0)) {
                            var59.die();
                            if (area.isPvP()) {
                                world.rooms.relayPvPEvent(var59, ((Integer) user.properties.get("pvpteam")).intValue());
                                ct.put("pvp", world.rooms.getPvPResult(room));
                            }
                        } else if (var59.getState() == 0) {
                            a.clear();
                        } else if (var59.getState() != 2) {
                            var59.setAttacking(world.scheduleTask(var59, 2500L, TimeUnit.MILLISECONDS, true));
                        }
                        if ((skill.hasAuraId()) && (!miss)) {
                            auras.add(applyAura(world, var59, skill.getAuraId(), fromTarget, damage));
                        }
                        JSONArray var68 = new JSONArray();
                        Iterator var71 = var74.iterator();
                        while (var71.hasNext()) {
                            int mainPlayer = ((Integer) var71.next()).intValue();
                            User mainExp = ExtensionHelper.instance().getUserById(mainPlayer);
                            if (mainExp != null) {
                                if (var59.getState() == 0) {
                                    mainExp.properties.put("state", Integer.valueOf(1));
                                    world.users.regen(mainExp);
                                    var68.add(mainExp.getName());
                                }
                                JSONObject plusKill = new JSONObject();
                                plusKill.put("intMP", (Integer) mainExp.properties.get("mp"));
                                plusKill.put("intState", (Integer) mainExp.properties.get("state"));
                                p.put(mainExp.getName(), plusKill);
                            } else {
                                var59.removeTarget(mainPlayer);
                            }
                        }
                        if (!var68.isEmpty()) {
                            var58.put("targets", var68);
                        }
                        var58.put("intState", Integer.valueOf(var59.getState()));
                        var58.put("intHP", Integer.valueOf(var59.getHealth()));
                        if (var59.getState() == 0) {
                            var58.put("intMP", Integer.valueOf(var59.getMana()));
                        }
                        m.put(String.valueOf(tgtId), var58);
                    }
                } else {
                    if (tgtType.equals("p")) {
                        User var60 = ExtensionHelper.instance().getUserById(tgtId);
                        if ((var60 == null) || (((Integer) var60.properties.get("state")).intValue() == 0)) {
                            continue;
                        }
                        Stats var63 = (Stats) var60.properties.get("stats");
                        dodge = Math.random() < var63.get$tdo();
                        if (!user.equals(var60)) {
                            Set var64 = (Set) var60.properties.get("auras");
                            Iterator var66 = var64.iterator();
                            while (var66.hasNext()) {
                                RemoveAura var75 = (RemoveAura) var66.next();
                                Aura var69 = var75.getAura();
                                if (!var69.getCategory().equals("d")) {
                                    damage = (int) (damage * (1.0D + var69.getDamageIncrease()));
                                }
                            }
                        }
                        if (damage > 0) {
                            if (dodge) {
                                damage = 0;
                            }
                            if (!area.isPvP()) {
                                throw new RequestException("Can't attack in a non-pvp area.");
                            }
                            if (user.properties.get("pvpteam") == var60.properties.get("pvpteam")) {
                                return;
                            }
                            var60.properties.put("state", Integer.valueOf(2));
                        }
                        damage /= 5;
                        Item var65 = (Item) user.properties.get("weaponitem");
                        Item var67 = (Item) var60.properties.get("weaponitem");
                        String var77 = (String) var60.properties.get("none");
                        byte var73 = -1;
                        switch (var77.hashCode()) {
                            case -1604554070:
                                if (var77.equals("Lightning")) {
                                    var73 = 5;
                                }
                                break;
                            case 2122646:
                                if (var77.equals("Dark")) {
                                    var73 = 3;
                                }
                                break;
                            case 2189910:
                                if (var77.equals("Fire")) {
                                    var73 = 0;
                                }
                                break;
                            case 2696232:
                                if (var77.equals("Wind")) {
                                    var73 = 4;
                                }
                                break;
                            case 73417974:
                                if (var77.equals("Light")) {
                                    var73 = 2;
                                }
                                break;
                            case 83350775:
                                if (var77.equals("Water")) {
                                    var73 = 1;
                                }
                                break;
                        }
                        switch (var73) {
                            case 0:
                                if (var65.getElement().equals("Water")) {
                                    damage += (int) (damage * 0.1D);
                                } else if (var67.getElement().equals("Fire")) {
                                    damage -= (int) (damage * 0.2D);
                                }
                                break;
                            case 1:
                                if (var65.getElement().equals("Fire")) {
                                    damage += (int) (damage * 0.1D);
                                } else if (var67.getElement().equals("Water")) {
                                    damage -= (int) (damage * 0.2D);
                                }
                                break;
                            case 2:
                                if (var65.getElement().equals("Dark")) {
                                    damage += (int) (damage * 0.1D);
                                } else if (var67.getElement().equals("Light")) {
                                    damage -= (int) (damage * 0.2D);
                                }
                                break;
                            case 3:
                                if (var65.getElement().equals("Light")) {
                                    damage += (int) (damage * 0.1D);
                                } else if (var67.getElement().equals("Dark")) {
                                    damage -= (int) (damage * 0.2D);
                                }
                                break;
                            case 4:
                                if (var65.getElement().equals("Lightning")) {
                                    damage += (int) (damage * 0.1D);
                                } else if (var67.getElement().equals("Wind")) {
                                    damage -= (int) (damage * 0.2D);
                                }
                                break;
                            case 5:
                                if (var65.getElement().equals("Wind")) {
                                    damage += (int) (damage * 0.1D);
                                } else if (var67.getElement().equals("Lightning")) {
                                    damage -= (int) (damage * 0.2D);
                                }
                                break;
                        }
                        int var70 = ((Integer) var60.properties.get("hp")).intValue() - damage;
                        var70 = var70 <= 0 ? 0 : var70;
                        var70 = var70 >= ((Integer) var60.properties.get("hpmax")).intValue() ? ((Integer) var60.properties.get("hpmax")).intValue() : var70;
                        var60.properties.put("hp", Integer.valueOf(var70));
                        if ((var70 <= 0) && (((Integer) var60.properties.get("state")).intValue() != 0)) {
                            JSONArray var80 = new JSONArray();
                            var80.add(user.getName());
                            var58.put("targets", var80);
                            world.users.die(var60);
                            user.properties.put("state", Integer.valueOf(1));

                            QueryResult isTournament = world.db.jdbc.query("SELECT * FROM tournament_settings WHERE ActiveTournament = 1", new Object[0]);
                            if (isTournament.next()) {
                                int tournamentLeft = world.db.jdbc.queryForInt("SELECT TIMESTAMPDIFF(SECOND, NOW(), ?)", new Object[]{isTournament.getString("TournamentEnds")});
                                tournamentLeft = tournamentLeft >= 0 ? tournamentLeft : 0;
                                if (tournamentLeft > 0) {
                                    Random r = new Random();
                                    int Points1 = 1;
                                    int Points2 = 10;
                                    int Result = r.nextInt(Points2 - Points1) + Points1;
                                    int Points3 = 1;
                                    int Points4 = 10;
                                    int Result1 = r.nextInt(Points4 - Points3) + Points3;
                                    world.send(new String[]{"moderator", "Congratulations! For winning you have received " + Result + " tournament points."}, user);
                                    world.db.jdbc.run("UPDATE users SET DeathCount = (DeathCount + 1) WHERE id = ?", new Object[]{var60.properties.get("dbId")});
                                    world.db.jdbc.run("UPDATE users SET KillCount = (KillCount + 1) WHERE id = ?", new Object[]{user.properties.get("dbId")});
                                    world.db.jdbc.run("UPDATE users SET TournamentPoints = (TournamentPoints + " + Result + ") WHERE id = ?", new Object[]{user.properties.get("dbId")});
                                    world.db.jdbc.run("UPDATE users SET TournamentPoints = (TournamentPoints - " + Result1 + ") WHERE id = ?", new Object[]{var60.properties.get("dbId")});
                                    world.db.jdbc.run("INSERT INTO users_tournaments (WinnerID, LosserID, WinnerPoints, LosserPoints, Date) VALUES (?, ?, " + Result + ", " + Result1 + ", NOW())", new Object[]{user.properties.get("dbId"), var60.properties.get("dbId")});
                                    //world.rooms.basicRoomJoin(user, "battleon");
                                }
                            } else {
                                world.db.jdbc.run("UPDATE users SET DeathCount = (DeathCount + 1) WHERE id = ?", new Object[]{var60.properties.get("dbId")});
                                world.db.jdbc.run("UPDATE users SET KillCount = (KillCount + 1) WHERE id = ?", new Object[]{user.properties.get("dbId")});
                                world.db.jdbc.run("INSERT INTO users_lastpvp (WinnerID, LosserID, Date) VALUES (?, ?, NOW())", new Object[]{user.properties.get("dbId"), var60.properties.get("dbId")});
                                //world.rooms.basicRoomJoin(user, "battleon");
                                QueryResult checkPVPtoken = world.db.jdbc.query("SELECT * FROM users_items WHERE UserID = ? AND ItemID = 247", new Object[]{user.properties.get("dbId")});
                                if (checkPVPtoken.next()) {
                                    world.db.jdbc.run("UPDATE users_items SET Quantity = (Quantity + 1) WHERE ItemID = 247 AND UserID = ?", new Object[]{user.properties.get("dbId")});
                                } else {
                                    world.db.jdbc.run("INSERT INTO users_items (ItemID, UserID, Equipped, Quantity, EnhID, Bank) VALUES ('247', ?, '0', '1','1957', 0", new Object[]{user.properties.get("dbId")});
                                }
                            }
                            if (area.isPvP()) {
                                Iterator var76 = area.items.iterator();
                                while (var76.hasNext()) {
                                    int var79 = ((Integer) var76.next()).intValue();
                                    world.users.dropItem(user, var79);
                                }
                                if (room.getName().split("-")[0].equals("guildwars")) {
                                    world.db.jdbc.run("UPDATE guilds SET TotalKills = (TotalKills + 1) WHERE id = ?", new Object[]{user.properties.get("guildid")});
                                    world.db.jdbc.run("UPDATE guilds SET Exp = (Exp + 100) WHERE id = ?", new Object[]{user.properties.get("guildid")});
                                    JSONObject var78 = world.users.getGuildObject(((Integer) user.properties.get("guildid")).intValue());
                                    int var79 = world.db.jdbc.queryForInt("SELECT Exp FROM guilds WHERE id = ?", new Object[]{user.properties.get("guildid")});
                                    int var81 = world.db.jdbc.queryForInt("SELECT TotalKills FROM guilds WHERE id = ?", new Object[]{user.properties.get("guildid")});
                                    var78.put("TotalKills", Integer.valueOf(var81 + 1));
                                    world.sendGuildUpdate(var78);
                                    if (var79 >= world.getGuildExpToLevel(((Integer) var78.get("Level")).intValue())) {
                                        world.users.guildLevelUp((Integer) user.properties.get("guildid"), ((Integer) var78.get("Level")).intValue() + 1);
                                    } else {
                                        var78.put("Exp", Integer.valueOf(var79 + 100));
                                        world.sendGuildUpdate(var78);
                                    }
                                }
                                if (room.getName().split("-")[0].equals("1v1")) {
                                    world.rooms.addPvPScore(room, 1000, ((Integer) user.properties.get("pvpteam")).intValue());
                                } else {
                                    world.rooms.addPvPScore(room, ((Integer) var60.properties.get("level")).intValue(), ((Integer) user.properties.get("pvpteam")).intValue());
                                }
                                ct.put("pvp", world.rooms.getPvPResult(room));
                            }
                        }
                        if ((skill.hasAuraId()) && ((!miss) || (!dodge) || (!skill.getReference().equals("i1")))) {
                            auras.add(applyAura(world, var60, skill.getAuraId(), fromTarget, damage));
                        }
                        var58.put("intState", var60.properties.get("state"));
                        var58.put("intHP", var60.properties.get("hp"));
                        var58.put("intMP", var60.properties.get("mp"));
                        p.put(var60.getName(), var58);
                        if (!p.containsKey(user.getName())) {
                            var58.clear();
                            var58.put("intMP", user.properties.get("mp"));
                            var58.put("intState", (Integer) user.properties.get("state"));
                            p.put(user.getName(), var58);
                        }
                    }
                    JSONObject var61 = new JSONObject();
                    var61.put("hp", Integer.valueOf(damage));
                    var61.put("tInf", target);
                    var61.put("type", crit ? "crit" : miss ? "miss" : dodge ? "dodge" : "hit");
                    a.add(var61);
                }
            }
            if (((Integer) user.properties.get("state")).intValue() == 1) {
                world.users.regen(user);
            }
            anim.put("strFrame", user.properties.get("frame"));
            anim.put("cInf", fromTarget);
            anim.put("fx", skill.getEffects());
            anim.put("tInf", tInf);
            anim.put("animStr", skill.getAnimation());
            if (!skill.getStrl().isEmpty()) {
                anim.put("strl", skill.getStrl());
            }
            anims.add(anim);
            sarsaObj.put("cInf", fromTarget);
            sarsaObj.put("a", a);
            sarsaObj.put("actID", Integer.valueOf(actId));
            sarsaObj.put("iRes", Integer.valueOf(1));
            sarsa.add(sarsaObj);
            if (!m.isEmpty()) {
                ct.put("m", m);
            }
            if (!auras.isEmpty()) {
                ct.put("a", auras);
            }
            ct.put("p", p);
            ct.put("cmd", "ct");
            ct.put("anims", anims);
            if (area.isPvP()) {
                ct.put("sarsa", sarsa);
                world.sendToRoom(ct, user, room);
            } else {
                world.sendToRoomButOne(ct, user, room);
                ct.put("sarsa", sarsa);
                world.send(ct, user);
            }
            user.properties.put(skill.getReference(), Long.valueOf(System.currentTimeMillis()));
            user.properties.put("perfecttimings", Long.valueOf(System.currentTimeMillis()));
        }
    }

    private JSONObject applyAura(World world, MonsterAI ai, int auraId, String fromTarget, int damage) {
        JSONObject aInfo = new JSONObject();
        Aura aura = (Aura) world.auras.get(Integer.valueOf(auraId));
        boolean auraExists = ai.hasAura(aura.getId());
        aInfo.put("cInf", fromTarget);
        aInfo.put("cmd", "aura+");
        aInfo.put("auras", aura.getAuraArray(!auraExists));
        aInfo.put("tInf", "m:" + ai.getMapId());
        if (auraExists) {
            return aInfo;
        }
        RemoveAura ra = ai.applyAura(aura);
        if (aura.getCategory().equals("d")) {
            DamageOverTime dot = new DamageOverTime(world, ai, damage, fromTarget);
            dot.setRunning(world.scheduleTask(dot, 2L, TimeUnit.SECONDS, true));
            ra.setDot(dot);
        }
        return aInfo;
    }

    private JSONObject applyAura(World world, User user, int auraId, String fromTarget, int damage) {
        JSONObject aInfo = new JSONObject();
        Aura aura = (Aura) world.auras.get(Integer.valueOf(auraId));
        boolean auraExists = world.users.hasAura(user, aura.getId());
        aInfo.put("cInf", fromTarget);
        aInfo.put("cmd", "aura+");
        aInfo.put("auras", aura.getAuraArray(!auraExists));
        aInfo.put("tInf", "p:" + user.getUserId());
        if (auraExists) {
            return aInfo;
        }
        RemoveAura ra = world.users.applyAura(user, aura);
        if (!aura.effects.isEmpty()) {
            Stats dot = (Stats) user.properties.get("stats");
            HashSet auraEffects = new HashSet();
            Iterator i$ = aura.effects.iterator();
            while (i$.hasNext()) {
                int effectId = ((Integer) i$.next()).intValue();
                AuraEffects ae = (AuraEffects) world.effects.get(Integer.valueOf(effectId));
                dot.effects.add(ae);
                auraEffects.add(ae);
            }
            dot.update();
            dot.sendStatChanges(dot, auraEffects);
        }
        if (aura.getCategory().equals("d")) {
            DamageOverTime dot1 = new DamageOverTime(world, user, damage, fromTarget);
            dot1.setRunning(world.scheduleTask(dot1, 2L, TimeUnit.SECONDS, true));
            ra.setDot(dot1);
        }
        return aInfo;
    }

    private String getSkillRefence(String str) {
        return str.contains(",") ? str.split(",")[0].split(">")[0] : str.split(">")[0];
    }

    private String parseTargetInfo(String str) {
        StringBuilder tb = new StringBuilder();
        if (str.contains(",")) {
            String[] multi = str.split(",");
            for (int i = 0; i < multi.length; i++) {
                if (i != 0) {
                    tb.append(",");
                }
                tb.append(multi[i].split(">")[1]);
            }
        } else {
            tb.append(str.split(">")[1]);
        }
        return tb.toString();
    }

    private int getRandomDamage(Stats stats, Skill skill) {
        return (int) ((rand.nextInt(1 + Math.abs(stats.getMaxDmg() - stats.getMinDmg())) + stats.getMinDmg()) * skill.getDamage());
    }
}
