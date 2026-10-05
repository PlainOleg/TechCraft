import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

/** Reproducible pixel-art source for Lumen Mesh container backgrounds. */
public final class LumenGuiGenerator {
    private static final Color CLEAR = new Color(0, 0, 0, 0);
    private static final Color FACE = new Color(198, 198, 198);
    private static final Color LIGHT = new Color(255, 255, 255);
    private static final Color MID = new Color(139, 139, 139);
    private static final Color DARK = new Color(55, 55, 55);
    private static final Color SLOT = new Color(151, 151, 151);
    private static final Color CYAN = new Color(64, 190, 210);
    private static final Color PURPLE = new Color(165, 100, 210);
    private static final Color YELLOW = new Color(255, 195, 58);
    private static final Color GREEN = new Color(92, 184, 92);
    private static final Color RED = new Color(198, 78, 68);
    private static final String OUT = "src/main/resources/assets/techcraft/textures/gui/container/lumen_mesh/";

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("item_terminal")) {
            large("item_terminal", 235, g -> terminal(g, false));
            return;
        }
        if (args.length == 1 && args[0].equals("crafting_terminal")) {
            // The crafting terminal deliberately starts from the item terminal
            // shell. Interactive crafting panes are rendered by its screen.
            large("crafting_terminal", 235, g -> terminal(g, false));
            return;
        }
        if (args.length == 1 && args[0].equals("mesh_core")) {
            compact("mesh_core", g -> meshCore(g));
            return;
        }
        if (args.length == 1 && args[0].equals("prism_drive")) {
            large("prism_drive", 172, g -> prismDrive(g));
            return;
        }
        compact("mesh_core", g -> meshCore(g));
        compact("cable_junction", g -> { junction(g, 88, 43); tinyPorts(g, 28, 33, 5); });
        compact("pulse_buffer", g -> { battery(g, 61, 29, 54, 31); pulse(g, 67, 44); });
        compact("coherence_stabilizer", g -> { orbit(g, 88, 43); meter(g, 119, 29, 38, PURPLE); });
        compact("crafting_processor", g -> { chip(g, 88, 43); progress(g, 119, 39, 38, CYAN); });
        compact("fabricator", g -> { slot(g, 29, 33); progress(g, 56, 37, 62, CYAN); slot(g, 127, 33); gear(g, 88, 62); });
        compact("matter_condenser", g -> { slot(g, 29, 33); progress(g, 56, 37, 62, PURPLE); slot(g, 127, 33); funnel(g, 75, 54); crystal(g, 99, 61); });
        compact("import_node", g -> { flowHeader(g, true, GREEN); filters(g); });
        compact("export_node", g -> { flowHeader(g, false, RED); filters(g); });
        large("storage_link", 210, LumenGuiGenerator::storageLink);
        compact("level_keeper", g -> { filters(g); gauge(g, 25, 61, 132); });
        compact("machineWell", g -> {});
        compact("machine_interface", g -> { for (int i=0;i<6;i++) slot(g, 18+i*22, 32); machine(g, 62, 56); arrow(g, 96, 66, 1, CYAN); });
        compact("stock_monitor", g -> { for(int i=0;i<5;i++) column(g, 29+i*25, 60, new int[]{18,29,12,35,23}[i]); });
        compact("wireless_relay", g -> { antenna(g, 88, 53); meter(g, 20, 65, 136, CYAN); });
        compact("quantum_bridge", g -> { portal(g, 56, 27); portal(g, 104, 27); link(g, 88, 44); });
        large("item_terminal", 235, g -> terminal(g, false));
        large("crafting_terminal", 235, g -> terminal(g, false));
        large("security_console", 222, LumenGuiGenerator::security);
        large("prism_drive", 172, g -> prismDrive(g));
    }

    private static void compact(String name, Consumer<Graphics2D> art) throws Exception {
        if (name.equals("matterCondenserWell") || name.equals("craftingWell") || name.equals("machineWell")) return;
        BufferedImage im = canvas(); Graphics2D g = graphics(im); frame(g,176,166); art.accept(g); divider(g, 80,176); inventory(g,7,84); save(name,im);
    }
    private static void large(String name, int height, Consumer<Graphics2D> art) throws Exception {
        BufferedImage im=canvas(); Graphics2D g=graphics(im); frame(g,224,height); art.accept(g); save(name,im);
    }
    private static BufferedImage canvas(){ BufferedImage i=new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB); Graphics2D g=i.createGraphics(); g.setColor(CLEAR); g.fillRect(0,0,256,256); g.dispose(); return i; }
    private static Graphics2D graphics(BufferedImage i){ Graphics2D g=i.createGraphics(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF); return g; }
    private static void frame(Graphics2D g,int w,int h){
        g.setColor(DARK); g.fillRect(2,0,w-4,h); g.fillRect(0,2,w,h-4);
        g.setColor(FACE); g.fillRect(3,2,w-6,h-5); g.fillRect(2,3,w-4,h-7);
        g.setColor(LIGHT); g.fillRect(3,2,w-6,1); g.fillRect(2,3,1,h-6);
        g.setColor(MID); g.fillRect(3,h-3,w-6,1); g.fillRect(w-3,3,1,h-6);
    }
    private static void divider(Graphics2D g,int y,int w){ g.setColor(MID);g.fillRect(7,y,w-14,1);g.setColor(LIGHT);g.fillRect(7,y+1,w-14,1); }
    private static void inventory(Graphics2D g,int x,int y){ for(int r=0;r<3;r++)for(int c=0;c<9;c++)slot(g,x+c*18,y+r*18);for(int c=0;c<9;c++)slot(g,x+c*18,y+58); }
    private static void slot(Graphics2D g,int x,int y){ x--;y--;g.setColor(MID);g.fillRect(x,y,18,18);g.setColor(DARK);g.fillRect(x,y,17,1);g.fillRect(x,y,1,17);g.setColor(LIGHT);g.fillRect(x+1,y+17,17,1);g.fillRect(x+17,y+1,1,17);g.setColor(SLOT);g.fillRect(x+1,y+1,16,16); }
    private static void panel(Graphics2D g,int x,int y,int w,int h){g.setColor(MID);g.fillRect(x,y,w,h);g.setColor(DARK);g.fillRect(x,y,w,1);g.fillRect(x,y,1,h);g.setColor(LIGHT);g.fillRect(x+1,y+h-1,w-1,1);g.fillRect(x+w-1,y+1,1,h-1);g.setColor(SLOT);g.fillRect(x+1,y+1,w-2,h-2);}
    private static void meter(Graphics2D g,int x,int y,int w,Color color){panel(g,x,y,w,8);g.setColor(color);g.fillRect(x+2,y+2,w-12,4);g.setColor(LIGHT);g.fillRect(x+2,y+2,w-12,1);}
    private static void progress(Graphics2D g,int x,int y,int w,Color c){panel(g,x,y,w,10);for(int i=0;i<5;i++){g.setColor(c);g.fillRect(x+3+i*11,y+3,8,4);}}
    private static void bars(Graphics2D g,int x,int y,int w){meter(g,x,y,w,YELLOW);meter(g,x,y+11,w,CYAN);meter(g,x,y+22,w,PURPLE);}
    private static void meshCore(Graphics2D g){core(g,33,48);meter(g,69,36,90,YELLOW);meter(g,69,53,90,CYAN);meter(g,69,70,90,PURPLE);}
    private static void prismDrive(Graphics2D g){for(int i=0;i<4;i++)slot(g,44+i*22,36);meter(g,16,67,144,CYAN);divider(g,86,176);inventory(g,7,90);}
    private static void core(Graphics2D g,int x,int y){ g.setColor(DARK);g.fillRect(x-14,y-14,29,29);g.setColor(MID);g.fillRect(x-11,y-11,23,23);g.setColor(CYAN);g.fillRect(x-6,y-6,13,13);g.setColor(LIGHT);g.fillRect(x-4,y-4,5,5); for(int d=-18;d<=18;d+=36){g.setColor(DARK);g.fillRect(x+d,y-2,5,5);g.fillRect(x-2,y+d,5,5);} }
    private static void junction(Graphics2D g,int x,int y){g.setColor(DARK);g.fillRect(x-4,y-18,9,37);g.fillRect(x-18,y-4,37,9);g.setColor(CYAN);g.fillRect(x-2,y-16,5,33);g.fillRect(x-16,y-2,33,5);g.setColor(LIGHT);g.fillRect(x-2,y-2,5,5);}
    private static void tinyPorts(Graphics2D g,int x,int y,int n){for(int i=0;i<n;i++){panel(g,x+i*9,y,7,7);}}
    private static void battery(Graphics2D g,int x,int y,int w,int h){panel(g,x,y,w,h);g.setColor(DARK);g.fillRect(x+w,y+10,4,10);for(int i=0;i<4;i++){g.setColor(i<3?YELLOW:MID);g.fillRect(x+5+i*11,y+5,8,h-10);}}
    private static void pulse(Graphics2D g,int x,int y){g.setColor(LIGHT);g.drawPolyline(new int[]{x,x+6,x+9,x+13,x+19},new int[]{y,y,y-8,y+7,y+7},5);}
    private static void orbit(Graphics2D g,int x,int y){g.setColor(DARK);g.drawOval(x-18,y-12,36,24);g.drawOval(x-12,y-18,24,36);g.setColor(PURPLE);g.fillRect(x-4,y-4,9,9);g.setColor(LIGHT);g.fillRect(x-1,y-1,3,3);}
    private static void chip(Graphics2D g,int x,int y){panel(g,x-14,y-12,29,25);g.setColor(DARK);for(int i=-9;i<=9;i+=6){g.fillRect(x-18,y+i,4,2);g.fillRect(x+15,y+i,4,2);}g.setColor(CYAN);g.fillRect(x-7,y-6,15,13);g.setColor(DARK);g.fillRect(x-3,y-3,7,7);}
    private static void gear(Graphics2D g,int x,int y){g.setColor(DARK);g.fillOval(x-11,y-11,23,23);for(int i=0;i<4;i++){g.fillRect(x-14+i*9,y-2,5,5);g.fillRect(x-2,y-14+i*9,5,5);}g.setColor(FACE);g.fillOval(x-6,y-6,13,13);g.setColor(MID);g.fillRect(x-2,y-2,5,5);}
    private static void arrow(Graphics2D g,int x,int y,int dir,Color c){g.setColor(c);if(dir>0){g.fillRect(x,y-2,12,5);g.fillRect(x+8,y-5,4,11);g.fillRect(x+12,y-2,3,5);}else{g.fillRect(x-12,y-2,12,5);g.fillRect(x-12,y-5,4,11);g.fillRect(x-15,y-2,3,5);}}
    private static void funnel(Graphics2D g,int x,int y){g.setColor(DARK);g.fillRect(x,y,22,4);g.fillRect(x+3,y+4,16,4);g.fillRect(x+7,y+8,8,9);g.setColor(PURPLE);g.fillRect(x+9,y+17,4,7);}
    private static void crystal(Graphics2D g,int x,int y){g.setColor(PURPLE);g.fillPolygon(new int[]{x,x+6,x+12,x+9,x+3},new int[]{y,y-7,y,y+10,y+10},5);g.setColor(LIGHT);g.fillRect(x+3,y-1,2,6);}
    private static void flowHeader(Graphics2D g,boolean in,Color c){chest(g,in?112:39,55);arrow(g,88,65,in?-1:1,c);core(g,in?55:121,65);}
    private static void filters(Graphics2D g){for(int i=0;i<5;i++)slot(g,20+i*24,32);panel(g,142,32,15,18);g.setColor(DARK);g.fillRect(147,37,5,8);}
    private static void chest(Graphics2D g,int x,int y){panel(g,x,y,25,21);g.setColor(DARK);g.fillRect(x,y+6,25,2);g.fillRect(x+10,y+6,5,5);}
    private static void link(Graphics2D g,int x,int y){g.setColor(DARK);g.drawRect(x-13,y-5,15,10);g.drawRect(x-2,y-5,15,10);g.setColor(CYAN);g.fillRect(x-5,y-1,11,3);}
    private static void gauge(Graphics2D g,int x,int y,int w){meter(g,x,y,w,GREEN);g.setColor(DARK);for(int i=0;i<5;i++)g.fillRect(x+2+i*32,y+9,1,5);g.setColor(RED);g.fillRect(x+w/2,y-2,2,12);}
    private static void machine(Graphics2D g,int x,int y){panel(g,x,y,25,21);g.setColor(DARK);g.fillRect(x+5,y+5,15,4);g.fillRect(x+5,y+12,7,4);g.setColor(GREEN);g.fillRect(x+16,y+12,4,4);}
    private static void column(Graphics2D g,int x,int base,int h){panel(g,x,base-38,14,40);g.setColor(GREEN);g.fillRect(x+3,base-h,8,h);g.setColor(LIGHT);g.fillRect(x+3,base-h,8,1);}
    private static void antenna(Graphics2D g,int x,int y){g.setColor(DARK);g.fillRect(x-2,y-17,5,28);g.fillRect(x-10,y+9,21,4);g.drawArc(x-14,y-17,28,20,35,110);g.drawArc(x-22,y-25,44,36,35,110);g.setColor(CYAN);g.fillRect(x-3,y-20,7,7);}
    private static void portal(Graphics2D g,int x,int y){g.setColor(DARK);g.fillRect(x,y,17,35);g.setColor(PURPLE);g.fillRect(x+4,y+4,9,27);g.setColor(LIGHT);g.fillRect(x+6,y+6,2,19);}
    private static void drive(Graphics2D g,int x,int y){panel(g,x,y,17,38);g.setColor(CYAN);for(int i=0;i<3;i++)g.fillRect(x+4,y+5+i*10,9,5);}
    private static void terminal(Graphics2D g,boolean crafting){frameArea(g,12,20,200,crafting?103:121);if(crafting){panel(g,14,23,156,12);panel(g,174,22,16,14);panel(g,194,22,16,14);for(int r=0;r<3;r++)for(int c=0;c<3;c++)slot(g,14+c*18,48+r*18);arrow(g,66,75,1,CYAN);slot(g,82,66);for(int r=0;r<3;r++)for(int c=0;c<6;c++)slot(g,110+c*18,48+r*18);divider(g,134,224);inventory(g,28,139);}else{panel(g,16,23,76,14);for(int r=0;r<5;r++)for(int c=0;c<9;c++)slot(g,28+c*18,43+r*18);divider(g,146,224);inventory(g,28,151);}}
    private static void frameArea(Graphics2D g,int x,int y,int w,int h){panel(g,x,y,w,h);}
    private static void security(Graphics2D g){panel(g,14,22,196,74);shield(g,38,38);bars(g,70,31,132);String[] dummy={""};for(int i=0;i<4;i++){panel(g,18+i*48,106,40,14);g.setColor(i==0?GREEN:MID);g.fillRect(22+i*48,110,5,6);}divider(g,134,224);inventory(g,28,139);}
    private static void storageLink(Graphics2D g){panel(g,12,20,200,30);for(int i=0;i<5;i++)slot(g,52+i*24,51);divider(g,122,224);inventory(g,31,128);}
    private static void shield(Graphics2D g,int x,int y){g.setColor(DARK);g.fillPolygon(new int[]{x,x+12,x+12,x+6,x},new int[]{y,y,y+12,y+20,y+12},5);g.setColor(GREEN);g.fillRect(x+4,y+5,5,8);g.setColor(LIGHT);g.fillRect(x+5,y+6,2,4);}
    private static void save(String name,BufferedImage im)throws Exception{ImageIO.write(im,"png",new File(OUT+name+".png"));}
}
