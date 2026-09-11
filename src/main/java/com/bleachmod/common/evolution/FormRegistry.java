package com.bleachmod.common.evolution;
import com.bleachmod.Reference;
import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

public final class FormRegistry {
    private static volatile Map<String,Map<String,FormGroup>> serverState=Map.of(),clientState=Map.of();
    private FormRegistry() {}
    private static Map<String,Map<String,FormGroup>> state() { return EffectiveSide.get().isServer()?serverState:clientState; }
    public static Map<String,Map<String,FormGroup>> prepare(MinecraftServer server) throws IOException {
        Path root=server.getWorldPath(LevelResource.ROOT).resolve(Reference.MOD_ID).resolve("forms");
        Files.createDirectories(root); FormDefaults.writeIfMissing(root);
        JsonObject all=new JsonObject();
        try(var paths=Files.list(root)) {
            for(Path path:paths.filter(Files::isRegularFile).filter(p->p.toString().endsWith(".json")).sorted().toList()) {
                try {
                    if(Files.size(path)>262144) throw new IllegalArgumentException("File exceeds 256 KiB");
                    JsonObject json=JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                    String race=json.get("race").getAsString();
                    if(all.has(race)) throw new IllegalArgumentException("Duplicate race "+race);
                    all.add(race,json.getAsJsonObject("groups"));
                } catch(Exception e) { throw new IOException(path+": "+e.getMessage(),e); }
            }
        }
        return parse(all.toString());
    }
    public static Map<String,Map<String,FormGroup>> parse(String text) {
        JsonObject root=JsonParser.parseString(text).getAsJsonObject();
        Map<String,Map<String,FormGroup>> result=new LinkedHashMap<>();
        for(String race:root.keySet()) {
            if(!race.equals("shinigami")) throw new IllegalArgumentException("Unsupported race "+race);
            Map<String,FormGroup> groups=new LinkedHashMap<>();
            JsonObject groupJson=root.getAsJsonObject(race);
            for(String name:groupJson.keySet()) {
                if(!name.equals("zanpakuto")) throw new IllegalArgumentException("Unsupported group "+name);
                JsonObject json=groupJson.getAsJsonObject(name);
                FormGroup group=new FormGroup(); group.setGroupName(name);
                group.setFormType(json.has("formType")?json.get("formType").getAsString():name);
                if(!"zanpakuto".equals(group.getFormType())) throw new IllegalArgumentException("Unknown skill");
                JsonObject forms=json.getAsJsonObject("forms");
                if(!forms.keySet().equals(Set.of("sealed","shikai","bankai"))) throw new IllegalArgumentException("Expected sealed/shikai/bankai");
                for(String form:List.of("sealed","shikai","bankai")) {
                    FormData data=FormData.fromJson(form,forms.getAsJsonObject(form));
                    if(!form.equals(data.getName())) throw new IllegalArgumentException("Form key/name mismatch "+form);
                    validate(data); group.getForms().put(form,data);
                }
                FormData sealed=group.getForms().get("sealed");
                List<String> order=List.of("sealed","shikai","bankai");
                for(String form:order) {
                    FormData data=group.getForms().get(form);
                    if(data.getFormRequisite().isBlank()) continue;
                    for(String token:data.getFormRequisite().split(",")) {
                        String prerequisite=token.trim().substring("zanpakuto.".length());
                        if(order.indexOf(prerequisite)>=order.indexOf(form))
                            throw new IllegalArgumentException("Prerequisite must precede form: "+form);
                        if(data.getUnlockOnMastery()>group.getForms().get(prerequisite).getMaxMastery())
                            throw new IllegalArgumentException("Unreachable prerequisite mastery: "+form);
                    }
                }
                if(sealed.getEnergyDrain()!=0||sealed.getUnlockOnSkillLevel()!=0||!sealed.getFormRequisite().isBlank())
                    throw new IllegalArgumentException("Sealed must be free and always unlocked");
                groups.put(name,group);
            }
            if(groups.isEmpty()) throw new IllegalArgumentException("Missing zanpakuto");
            result.put(race,Collections.unmodifiableMap(groups));
        }
        if(!result.containsKey("shinigami")) throw new IllegalArgumentException("Missing shinigami");
        return Collections.unmodifiableMap(result);
    }
    private static void validate(FormData f) {
        if(f.getUnlockOnSkillLevel()<0||f.getUnlockOnSkillLevel()>2) throw new IllegalArgumentException("Invalid skill level");
        double[] values={f.getEnergyDrain(),f.getMaxMastery(),f.getUnlockOnMastery(),f.getInstantTransformOnMastery(),f.getAllowFreeTransformOnMastery(),f.getPassiveMasteryEveryFiveSeconds()};
        for(double v:values) if(!Double.isFinite(v)||v<0||v>100) throw new IllegalArgumentException("Invalid form value");
        if(f.getMaxMastery()<=0||f.getInstantTransformOnMastery()>f.getMaxMastery()||f.getAllowFreeTransformOnMastery()>f.getMaxMastery())
            throw new IllegalArgumentException("Unreachable mastery threshold");
        if(!Set.of("all","any").contains(f.getFormRequisiteType())) throw new IllegalArgumentException("Invalid requisite type");
        if(!f.getFormRequisite().isBlank()) for(String token:f.getFormRequisite().split(","))
            if(!Set.of("zanpakuto.sealed","zanpakuto.shikai","zanpakuto.bankai").contains(token.trim()))
                throw new IllegalArgumentException("Unknown prerequisite "+token);
    }
    public static void installServer(Map<String,Map<String,FormGroup>> value) { serverState=value; }
    public static void loadAll(MinecraftServer server) {
        try { installServer(prepare(server)); } catch(IOException e) { throw new IllegalArgumentException(e); }
    }
    public static void replaceFromNetwork(String json) { clientState=parse(json); }
    public static void clearClient() { clientState=Map.of(); }
    public static void register(String race,FormGroup group) { serverState=Map.of(race,Map.of(group.getGroupName(),group)); }
    private static String key(String s) { return s==null?"":s.toLowerCase(Locale.ROOT); }
    public static FormGroup getGroup(String race,String group) { return state().getOrDefault(key(race),Map.of()).get(key(group)); }
    public static FormData getForm(String race,String group,String form) {
        FormGroup g=getGroup(race,group); return g==null?null:g.getForms().get(key(form));
    }
    public static Map<String,FormGroup> groupsForRace(String race) { return state().getOrDefault(key(race),Map.of()); }
    public static String serialize() {
        JsonObject json=new JsonObject();
        state().forEach((race,groups)->{JsonObject r=new JsonObject(); groups.forEach((name,g)->r.add(name,g.toJson())); json.add(race,r);});
        return json.toString();
    }
}
