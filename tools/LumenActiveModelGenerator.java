import java.nio.file.*;

/** Generates facing+active blockstates and matching active-front models. */
public final class LumenActiveModelGenerator {
    private static final String[] BLOCKS={"mesh_core","energy_bridge","pulse_buffer","coherence_stabilizer","prism_drive","item_terminal","crafting_terminal","blueprint_encoder","fabricator","crafting_processor","import_node","export_node","storage_link","machine_interface","level_keeper","stock_monitor","wireless_relay","quantum_bridge","security_console","matter_condenser"};
    public static void main(String[] args)throws Exception{
        Path assets=Path.of("src/main/resources/assets/techcraft");
        for(String name:BLOCKS){
            String variants="{\n  \"variants\": {\n"+
                variant(name,"north",0,false)+",\n"+variant(name,"east",90,false)+",\n"+variant(name,"south",180,false)+",\n"+variant(name,"west",270,false)+",\n"+
                variant(name,"north",0,true)+",\n"+variant(name,"east",90,true)+",\n"+variant(name,"south",180,true)+",\n"+variant(name,"west",270,true)+"\n  }\n}\n";
            Files.writeString(assets.resolve("blockstates/"+name+".json"),variants);
            String model="{\n  \"parent\": \"minecraft:block/cube\",\n  \"textures\": {\n"+
                "    \"particle\": \"techcraft:block/lumen_mesh/"+name+"_side\",\n"+
                "    \"north\": \"techcraft:block/lumen_mesh/"+name+"_front_active\",\n"+
                "    \"south\": \"techcraft:block/lumen_mesh/"+name+"_side\",\n"+
                "    \"east\": \"techcraft:block/lumen_mesh/"+name+"_side\",\n"+
                "    \"west\": \"techcraft:block/lumen_mesh/"+name+"_side\",\n"+
                "    \"up\": \"techcraft:block/lumen_mesh/"+name+"_top\",\n"+
                "    \"down\": \"techcraft:block/lumen_mesh/"+name+"_side\"\n  }\n}\n";
            Files.writeString(assets.resolve("models/block/"+name+"_active.json"),model);
        }
        for(String name:new String[]{"mesh_cable","smart_cable","dense_trunk","cable_junction"}) cable(assets,name);
    }
    private static String variant(String n,String facing,int y,boolean active){
        return "    \"facing="+facing+",active="+active+"\": { \"model\": \"techcraft:block/"+n+(active?"_active":"")+"\""+(y==0?"":", \"y\": "+y)+" }";
    }
    private static void cable(Path assets,String name)throws Exception{
        String activeTexture=name.equals("mesh_cable")?name+"_side":name+"_front_active";
        for(String part:new String[]{"center","arm"}){
            Path normal=assets.resolve("models/block/"+name+"_"+part+".json");
            String source=Files.readString(normal);
            Files.writeString(assets.resolve("models/block/"+name+"_"+part+"_active.json"),source.replace(name+"_side",activeTexture));
        }
        StringBuilder s=new StringBuilder("{\n  \"multipart\": [\n");
        s.append("    { \"when\": { \"active\": \"false\" }, \"apply\": { \"model\": \"techcraft:block/").append(name).append("_center\" } },\n");
        s.append("    { \"when\": { \"active\": \"true\" }, \"apply\": { \"model\": \"techcraft:block/").append(name).append("_center_active\" } }");
        String[] dirs={"north","east","south","west","up","down"}; int[] y={0,90,180,270,0,0}; int[] x={0,0,0,0,270,90};
        for(int i=0;i<dirs.length;i++)for(boolean active:new boolean[]{false,true}){
            s.append(",\n    { \"when\": { \"").append(dirs[i]).append("\": \"true\", \"active\": \"").append(active).append("\" }, \"apply\": { \"model\": \"techcraft:block/").append(name).append("_arm").append(active?"_active":"").append("\"");
            if(y[i]!=0)s.append(", \"y\": ").append(y[i]); if(x[i]!=0)s.append(", \"x\": ").append(x[i]); s.append(" } }");
        }
        s.append("\n  ]\n}\n"); Files.writeString(assets.resolve("blockstates/"+name+".json"),s.toString());
    }
}
