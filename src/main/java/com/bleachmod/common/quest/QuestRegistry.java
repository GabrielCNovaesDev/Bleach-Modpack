package com.bleachmod.common.quest;
import com.bleachmod.Reference;
import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public final class QuestRegistry {
    public record Snapshot(Map<String,Saga> sagas, Map<String,Quest> quests, Map<String,List<String>> order) {}
    private static final Snapshot EMPTY = new Snapshot(Map.of(),Map.of(),Map.of());
    private static volatile Snapshot serverState=EMPTY, clientState=EMPTY;
    private QuestRegistry() {}
    private static Snapshot state() { return EffectiveSide.get().isServer() ? serverState : clientState; }
    public static Snapshot prepare(MinecraftServer server) throws IOException {
        Path root=server.getWorldPath(LevelResource.ROOT).resolve(Reference.MOD_ID).toAbsolutePath().normalize();
        Files.createDirectories(root.resolve("sagas"));
        Files.createDirectories(root.resolve("quests"));
        Files.createDirectories(root.resolve("sidequests"));
        QuestDefaults.writeIfMissing(root);
        JsonArray sagas=new JsonArray(), quests=new JsonArray();
        for(Path path:files(root.resolve("sagas"))) {
            JsonObject json=read(path);
            Saga saga=Saga.fromJson(json);
            Path folder=root.resolve("quests").resolve(saga.getQuestFolder()).normalize();
            if(!folder.startsWith(root.resolve("quests"))) throw new IOException("Invalid questFolder: "+path);
            sagas.add(json);
            for(Path q:files(folder)) { JsonObject obj=read(q); obj.addProperty("sagaId",saga.getId()); quests.add(obj); }
        }
        for(Path path:files(root.resolve("sidequests"))) quests.add(read(path));
        return parse(sagas.toString(),quests.toString());
    }
    private static JsonObject read(Path path) throws IOException {
        try {
            if(Files.size(path)>262144) throw new IllegalArgumentException("File exceeds 256 KiB");
            return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        } catch(Exception e) { throw new IOException(path+": "+e.getMessage(),e); }
    }
    private static List<Path> files(Path folder) throws IOException {
        if(!Files.isDirectory(folder)) return List.of();
        try(Stream<Path> paths=Files.walk(folder)) {
            List<Path> result=paths.filter(Files::isRegularFile).filter(p->p.toString().endsWith(".json")).sorted().toList();
            if(result.size()>128) throw new IOException("Too many files: "+folder);
            return result;
        }
    }
    public static Snapshot parse(String sagasJson,String questsJson) {
        Map<String,Saga> sagas=new LinkedHashMap<>();
        Map<String,Quest> quests=new LinkedHashMap<>();
        Map<String,List<String>> order=new LinkedHashMap<>();
        for(JsonElement el:JsonParser.parseString(sagasJson).getAsJsonArray()) {
            Saga saga=Saga.fromJson(el.getAsJsonObject()); requireId(saga.getId());
            if(sagas.putIfAbsent(saga.getId(),saga)!=null) throw new IllegalArgumentException("Duplicate saga "+saga.getId());
        }
        for(JsonElement el:JsonParser.parseString(questsJson).getAsJsonArray()) {
            JsonObject obj=el.getAsJsonObject();
            Quest q=QuestParser.parseQuest(obj,obj.has("sagaId")?obj.get("sagaId").getAsString():null);
            requireId(q.getQuestKey());
            if(q.getType()==QuestType.SAGA&&!sagas.containsKey(q.getSagaId())) throw new IllegalArgumentException("Missing saga: "+q.getQuestKey());
            if(quests.putIfAbsent(q.getQuestKey(),q)!=null) throw new IllegalArgumentException("Duplicate quest "+q.getQuestKey());
            if(q.getType()==QuestType.SAGA) order.computeIfAbsent(q.getSagaId(),k->new ArrayList<>()).add(q.getQuestKey());
        }
        if(quests.isEmpty()||quests.size()>128) throw new IllegalArgumentException("Expected 1..128 quests");
        for(Saga saga:sagas.values()) {
            Set<String> visited=new HashSet<>();
            Saga current=saga;
            while(!current.getPreviousSaga().isBlank()) {
                if(!visited.add(current.getId())) throw new IllegalArgumentException("Saga cycle: "+current.getId());
                String previous=current.getPreviousSaga(); current=sagas.get(previous);
                if(current==null||!order.containsKey(previous)) throw new IllegalArgumentException("Missing previous saga: "+previous);
            }
        }
        for(Quest q:quests.values()) for(var c:q.getPrerequisites().getConditions()) {
            String key=switch(c.type()) { case QUEST->c.questKey(); case SAGA_QUEST->c.sagaId()+":"+c.questId(); default->null; };
            if((c.type()==QuestPrerequisites.ConditionType.QUEST||c.type()==QuestPrerequisites.ConditionType.SAGA_QUEST)
                &&(key==null||!quests.containsKey(key)||key.equals(q.getQuestKey())))
                throw new IllegalArgumentException("Invalid prerequisite: "+q.getQuestKey());
        }
        order.replaceAll((k,v)->v.stream().sorted(Comparator.comparingInt(id->quests.get(id).getNumericId())).toList());
        return new Snapshot(Collections.unmodifiableMap(sagas),Collections.unmodifiableMap(quests),Collections.unmodifiableMap(order));
    }
    private static void requireId(String id) {
        if(id==null||!id.matches("[a-z0-9_:/.-]{1,128}")) throw new IllegalArgumentException("Invalid id: "+id);
    }
    public static void installServer(Snapshot snapshot) { serverState=snapshot; }
    public static void loadAll(MinecraftServer server) {
        try { installServer(prepare(server)); } catch(IOException e) { throw new IllegalArgumentException(e); }
    }
    public static void replaceFromNetwork(String sagas,String quests) { clientState=parse(sagas,quests); }
    public static void clearClient() { clientState=EMPTY; }
    public static String serializeSagas() {
        JsonArray array=new JsonArray(); state().sagas().values().forEach(s->array.add(s.toJson())); return array.toString();
    }
    public static String serializeQuests() {
        JsonArray array=new JsonArray();
        state().quests().values().forEach(q->{JsonObject obj=q.toJson(); if(q.getSagaId()!=null)obj.addProperty("sagaId",q.getSagaId()); array.add(obj);});
        return array.toString();
    }
    public static Quest getQuest(String key) { return state().quests().get(key); }
    public static Saga getSaga(String key) { return state().sagas().get(key); }
    public static Collection<Quest> allQuests() { return state().quests().values(); }
    public static List<Quest> questsForNpc(String npcId) {
        if (npcId == null || npcId.isBlank()) return List.of();
        return state().quests().values().stream().filter(q -> npcId.equals(q.getQuestGiver())).toList();
    }
    public static Collection<Saga> allSagas() { return state().sagas().values(); }
    public static List<String> sagaQuestKeys(String id) { return state().order().getOrDefault(id,List.of()); }
}
