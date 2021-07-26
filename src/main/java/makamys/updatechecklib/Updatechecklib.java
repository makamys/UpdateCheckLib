package makamys.updatechecklib;

import net.minecraft.init.Blocks;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = Updatechecklib.MODID, version = Updatechecklib.VERSION)
public class Updatechecklib
{
    public static final String MODID = "UpdateCheckLib";
    public static final String VERSION = "0.0";

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        System.out.println("DIRT BLOCK >> "+Blocks.dirt.getUnlocalizedName());
    }
}
