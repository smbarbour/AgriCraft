package com.InfinityRaider.AgriCraft.reference;

//yes, I got the information for most harvestcraft plants from wikipedia, go ahead, call the fucking cops.

import com.InfinityRaider.AgriCraft.items.ItemModSeed;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;

@SideOnly(Side.CLIENT)
public final class SeedInformation {
    private static final HashMap<Item, String[]> informationTable = new HashMap<Item, String[]>();

    @SideOnly(Side.CLIENT)
    public static void init() {
        informationTable.put(Items.wheat_seeds, new String[]{wheat});
        informationTable.put(Items.pumpkin_seeds, new String[]{pumpkin});
        informationTable.put(Items.melon_seeds, new String[]{melon});
    }

    //retrieve seed information
    public static String getSeedInformation(ItemStack seedStack) {
        String output = "";
        if (seedStack.getItem() instanceof ItemSeeds) {
            if (seedStack.getItem() instanceof ItemModSeed) {
                output = ((ItemModSeed) seedStack.getItem()).getInformation();
            }
            else {
                String[] info = informationTable.get(seedStack.getItem());
                if(info!=null && info.length>seedStack.getItemDamage()) {
                    output = info[seedStack.getItemDamage()];
                }
            }
        }
        return StatCollector.translateToLocal(output);
    }

    //agricraft seeds
    public static final String potato = "agricraft_journal.potato";
    public static final String carrot = "agricraft_journal.carrot";
    public static final String sugarcane = "agricraft_journal.sugarcane";
    public static final String dandelion = "agricraft_journal.dandelion";
    public static final String poppy = "agricraft_journal.poppy";
    public static final String orchid = "agricraft_journal.orchid";
    public static final String allium = "agricraft_journal.allium";
    public static final String tulipPink = "agricraft_journal.tulipPink";
    public static final String tulipOrange = "agricraft_journal.tulipOrange";
    public static final String tulipRed = "agricraft_journal.tulipRed";
    public static final String tulipWhite = "agricraft_journal.tulipWhite";
    public static final String daisy = "agricraft_journal.daisy";
    public static final String cactus = "agricraft_journal.cactus";
    public static final String shroomRed = "agricraft_journal.shroomRed";
    public static final String shroomBrown = "agricraft_journal.shroomBrown";
    public static final String diamahlia = "agricraft_journal.diamahlia";
    public static final String aurigold = "agricraft_journal.aurigold";
    public static final String ferranium = "agricraft_journal.ferranium";
    public static final String lapender = "agricraft_journal.lapender";
    public static final String emeryllis = "agricraft_journal.emeryllis";
    public static final String redstodendron = "agricraft_journal.redstodendron";
    public static final String cuprosia = "agricraft_journal.cuprosia";
    public static final String petinia = "agricraft_journal.petinia";
    public static final String plombean = "agricraft_journal.plombean";
    public static final String silverweed = "agricraft_journal.silverweed";
    public static final String jaslumine = "agricraft_journal.jaslumine";
    public static final String niccissus = "agricraft_journal.niccissus";
    public static final String platiolus = "agricraft_journal.platiolus";
    public static final String osmonium = "agricraft_journal.osmonium";

    //minecraft seeds
    public static final String wheat = "agricraft_journal.wheat";
    public static final String pumpkin = "agricraft_journal.pumpkin";
    public static final String melon = "agricraft_journal.melon";

    //botania seeds
    public static final String botaniaWhite = "agricraft_journal.botaniaWhite";
    public static final String botaniaOrange = "agricraft_journal.botaniaOrange";
    public static final String botaniaMagenta = "agricraft_journal.botaniaMagenta";
    public static final String botaniaLightBlue = "agricraft_journal.botaniaLightBlue";
    public static final String botaniaYellow = "agricraft_journal.botaniaYellow";
    public static final String botaniaLime = "agricraft_journal.botaniaLime";
    public static final String botaniaPink = "agricraft_journal.botaniaPink";
    public static final String botaniaGray = "agricraft_journal.botaniaGray";
    public static final String botaniaLightGray = "agricraft_journal.botaniaLightGray";
    public static final String botaniaCyan = "agricraft_journal.botaniaCyan";
    public static final String botaniaPurple = "agricraft_journal.botaniaPurple";
    public static final String botaniaBlue = "agricraft_journal.botaniaBlue";
    public static final String botaniaBrown = "agricraft_journal.botaniaBrown";
    public static final String botaniaGreen = "agricraft_journal. botaniaGreen";
    public static final String botaniaRed = "agricraft_journal.botaniaRed";
    public static final String botaniaBlack = "agricraft_journal.botaniaBlack";


    //natura seeds
    public static final String barleyNatura = "agricraft_journal.barleyNatura";
    public static final String cottonNatura = "agricraft_journal.cottonNatura";

    //harvestcraft seeds
    public static final String hc_Artichoke = "agricraft_journal.hc_Artichoke";
    public static final String hc_Asparagus = "agricraft_journal.hc_Asparagus";
    public static final String hc_BambooShoot = "agricraft_journal.hc_BambooShoot";
    public static final String hc_Barley = barleyNatura;
    public static final String hc_Bean = "agricraft_journal.hc_Bean";
    public static final String hc_Beet = "agricraft_journal.hc_Beet";
    public static final String hc_Bellpepper = "agricraft_journal.hc_Bellpepper";
    public static final String hc_Blackberry = "agricraft_journal.hc_Blackberry";
    public static final String hc_Blueberry = "agricraft_journal.hc_Blueberry";
    public static final String hc_Broccoli = "agricraft_journal.hc_Broccoli";
    public static final String hc_BrusselsSprout = "agricraft_journal.hc_BrusselsSprout";
    public static final String hc_Cabbage = "agricraft_journal.hc_Cabbage";
    public static final String hc_CactusFruit = "agricraft_journal.hc_CactusFruit";
    public static final String hc_CandleBerry = "agricraft_journal.hc_CandleBerry";
    public static final String hc_Cantaloupe = "agricraft_journal.hc_Cantaloupe";
    public static final String hc_Cauliflower = "agricraft_journal.hc_Cauliflower";
    public static final String hc_Celery = "agricraft_journal.hc_Celery";
    public static final String hc_ChiliPepper = "agricraft_journal.hc_ChiliPepper";
    public static final String hc_Coffee = "agricraft_journal.hc_Coffee";
    public static final String hc_Corn = "agricraft_journal.hc_Corn";
    public static final String hc_Cotton = cottonNatura;
    public static final String hc_Cranberry = "agricraft_journal.hc_Cranberry";
    public static final String hc_Cucumber = "agricraft_journal.hc_Cucumber";
    public static final String hc_Eggplant = "agricraft_journal.hc_Eggplant";
    public static final String hc_Garlic = "agricraft_journal.hc_Garlic";
    public static final String hc_Ginger = "agricraft_journal.hc_Ginger";
    public static final String hc_Grape = "agricraft_journal.hc_Grape";
    public static final String hc_Kiwi = "agricraft_journal.hc_Kiwi";
    public static final String hc_Leek = "agricraft_journal.hc_Leek";
    public static final String hc_Lettuce = "agricraft_journal.hc_Lettuce";
    public static final String hc_Mustard = "agricraft_journal.hc_Mustard";
    public static final String hc_Oats = "agricraft_journal.hc_Oats";
    public static final String hc_Okra = "agricraft_journal.hc_Okra";
    public static final String hc_Onion = "agricraft_journal.hc_Onion";
    public static final String hc_Parsnip = "agricraft_journal.hc_Parsnip";
    public static final String hc_Peanut = "agricraft_journal.hc_Peanut";
    public static final String hc_Peas = "agricraft_journal.hc_Peas";
    public static final String hc_Pineapple = "agricraft_journal.hc_Pineapple";
    public static final String hc_Radish = "agricraft_journal.hc_Radish";
    public static final String hc_Raspberry = "agricraft_journal.hc_Raspberry";
    public static final String hc_Rhubarb = "agricraft_journal.hc_Rhubarb";
    public static final String hc_Rice = "agricraft_journal.hc_Rice";
    public static final String hc_Rutabaga = "agricraft_journal.hc_Rutabaga";
    public static final String hc_Rye = "agricraft_journal.hc_Rye";
    public static final String hc_Scallion = "agricraft_journal.hc_Scallion";
    public static final String hc_Seaweed = "agricraft_journal.hc_Seaweed";
    public static final String hc_Soybean = "agricraft_journal.hc_Soybean";
    public static final String hc_SpiceLeaf = "agricraft_journal.hc_SpiceLeaf";
    public static final String hc_Strawberry = "agricraft_journal.hc_Strawberry";
    public static final String hc_SweetPotato = "agricraft_journal.hc_SweetPotato";
    public static final String hc_Tea = "agricraft_journal.hc_Tea";
    public static final String hc_Tomato = "agricraft_journal.hc_Tomato";
    public static final String hc_Turnip = "agricraft_journal.hc_Turnip";
    public static final String hc_WhiteMushroom = "agricraft_journal.hc_WhiteMushroom";
    public static final String hc_WinterSquash = "agricraft_journal.hc_WinterSquash";
    public static final String hc_Zucchini = "agricraft_journal.hc_Zucchini";

    //plant mega pack seeds
    public static final String pmp_Onion = hc_Onion;
    public static final String pmp_Spinach = "agricraft_journal.pmp_Spinach";
    public static final String pmp_Celery = hc_Celery;
    public static final String pmp_lettuce = hc_Lettuce;
    public static final String pmp_Bellpepper = hc_Bellpepper;
    public static final String pmp_Corn = hc_Corn;
    public static final String pmp_Cucumber = hc_Cucumber;
    public static final String pmp_Tomato = hc_Tomato;
    public static final String pmp_Beet = hc_Beet;

    //chococraft seeds
    public static final String cc_gysahl ="agricraft_journal.cc_Gysahl";
}
