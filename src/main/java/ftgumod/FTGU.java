package ftgumod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import ftgumod.CapabilityTechnology.DefaultImpl;
import ftgumod.CapabilityTechnology.ITechnology;
import ftgumod.CapabilityTechnology.Storage;
import ftgumod.block.BlockIdeaTable;
import ftgumod.block.BlockResearchTable;
import ftgumod.gui.GuiHandler;
import ftgumod.gui.ideatable.TileEntityIdeaTable;
import ftgumod.gui.researchtable.TileEntityResearchTable;
import ftgumod.item.ItemLookingGlass;
import ftgumod.item.ItemParchmentEmpty;
import ftgumod.item.ItemParchmentIdea;
import ftgumod.item.ItemParchmentResearch;
import ftgumod.item.ItemResearchBook;
import ftgumod.packet.PacketDispatcher;

@Mod(modid = FTGU.MODID, version = FTGU.VERSION)
public class FTGU {

	public static final String MODID = "ftgumod";
	public static final String VERSION = "Minecraft 1.9.4";
	
	public static boolean headstart = false;
	public static boolean moddedOnly = false;

	@Instance(value = FTGU.MODID)
	public static FTGU instance;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		GameRegistry.registerTileEntity(TileEntityIdeaTable.class, FTGUAPI.n_ideaTable);
		GameRegistry.registerTileEntity(TileEntityResearchTable.class, FTGUAPI.n_researchTable);

		FTGUAPI.b_ideaTable = new BlockIdeaTable(FTGUAPI.n_ideaTable);
		FTGUAPI.b_researchTable = new BlockResearchTable(FTGUAPI.n_researchTable);

		FTGUAPI.i_parchmentEmpty = new ItemParchmentEmpty(FTGUAPI.n_parchmentEmpty);
		FTGUAPI.i_parchmentIdea = new ItemParchmentIdea(FTGUAPI.n_parchmentIdea);
		FTGUAPI.i_parchmentResearch = new ItemParchmentResearch(FTGUAPI.n_parchmentResearch);
		FTGUAPI.i_researchBook = new ItemResearchBook(FTGUAPI.n_researchBook);
		FTGUAPI.i_lookingGlass = new ItemLookingGlass(FTGUAPI.n_lookingGlass);

		GameRegistry.registerBlock(FTGUAPI.b_ideaTable, FTGUAPI.n_ideaTable);
		GameRegistry.registerBlock(FTGUAPI.b_researchTable, FTGUAPI.n_researchTable);

		GameRegistry.registerItem(FTGUAPI.i_parchmentEmpty, FTGUAPI.n_parchmentEmpty);
		GameRegistry.registerItem(FTGUAPI.i_parchmentIdea, FTGUAPI.n_parchmentIdea);
		GameRegistry.registerItem(FTGUAPI.i_parchmentResearch, FTGUAPI.n_parchmentResearch);
		GameRegistry.registerItem(FTGUAPI.i_researchBook, FTGUAPI.n_researchBook);
		GameRegistry.registerItem(FTGUAPI.i_lookingGlass, FTGUAPI.n_lookingGlass);

		CapabilityManager.INSTANCE.register(ITechnology.class, new Storage(), DefaultImpl.class);

		MinecraftForge.EVENT_BUS.register(new CapabilityTechnology());
		MinecraftForge.EVENT_BUS.register(new EventHandler());

		PacketDispatcher.registerPackets();
		
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		headstart = config.get(config.CATEGORY_GENERAL, "Headstart", false, "Set this to true to automatically research Stonecraft, Stoneworking, Carpentry, Refinement, Bibliography, Advanced Combat, Building Blocks and Cooking").getBoolean();
		moddedOnly = config.get(config.CATEGORY_GENERAL, "Modded", false, "Set this to true to automatically research all vanilla technologies").getBoolean();
		
		config.save();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.addRecipe(new ItemStack(FTGUAPI.b_ideaTable), "F P", "SSS", "WBW", 'S', Blocks.WOODEN_SLAB, 'W', Blocks.PLANKS, 'B', Blocks.CRAFTING_TABLE, 'F', Items.FEATHER, 'P', FTGUAPI.i_parchmentEmpty);
		GameRegistry.addRecipe(new ItemStack(FTGUAPI.b_researchTable), "SSS", "CBC", "CWC", 'S', Blocks.WOODEN_SLAB, 'W', Blocks.PLANKS, 'B', Blocks.CRAFTING_TABLE, 'C', Blocks.COBBLESTONE);
		GameRegistry.addRecipe(new ItemStack(FTGUAPI.i_parchmentEmpty), "S", "P", "S", 'S', Items.STICK, 'P', Items.PAPER);
		GameRegistry.addRecipe(new ItemStack(FTGUAPI.i_lookingGlass), " N ", "NGN", "SN ", 'N', Items.GOLD_NUGGET, 'G', Blocks.GLASS_PANE, 'S', Items.STICK);

		Item r = FTGUAPI.i_parchmentResearch;

		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r, r, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r, r, r, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), Items.BOOK, r, r, r, r, r, r, r, r);

		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r, r, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r, r, r, r, r, r, r);
		GameRegistry.addShapelessRecipe(new ItemStack(FTGUAPI.i_researchBook), FTGUAPI.i_researchBook, r, r, r, r, r, r, r, r);

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

		if (event.getSide() == Side.CLIENT) {
			RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

			renderItem.getItemModelMesher().register(Item.getItemFromBlock(FTGUAPI.b_ideaTable), 0, new ModelResourceLocation(MODID + ":" + FTGUAPI.n_ideaTable, "inventory"));
			renderItem.getItemModelMesher().register(Item.getItemFromBlock(FTGUAPI.b_researchTable), 0, new ModelResourceLocation(MODID + ":" + FTGUAPI.n_researchTable, "inventory"));

			renderItem.getItemModelMesher().register(FTGUAPI.i_parchmentEmpty, 0, new ModelResourceLocation(MODID + ":" + FTGUAPI.n_parchmentEmpty, "inventory"));
			renderItem.getItemModelMesher().register(FTGUAPI.i_parchmentIdea, 0, new ModelResourceLocation(MODID + ":" + FTGUAPI.n_parchmentIdea, "inventory"));
			renderItem.getItemModelMesher().register(FTGUAPI.i_parchmentResearch, 0, new ModelResourceLocation(MODID + ":" + FTGUAPI.n_parchmentResearch, "inventory"));
			renderItem.getItemModelMesher().register(FTGUAPI.i_researchBook, 0, new ModelResourceLocation(MODID + ":" + FTGUAPI.n_researchBook, "inventory"));
			renderItem.getItemModelMesher().register(FTGUAPI.i_lookingGlass, 0, new ModelResourceLocation(MODID + ":" + FTGUAPI.n_lookingGlass, "inventory"));
		}

		TechnologyHandler.init();

		TechnologyHandler.BASIC_CRAFTING.researched = true;
		TechnologyHandler.WOODWORKING.researched = true;
		TechnologyHandler.WRITING.researched = true;
		TechnologyHandler.WOODEN_TOOLS.researched = true;
		TechnologyHandler.RESEARCH.researched = true;
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}

}