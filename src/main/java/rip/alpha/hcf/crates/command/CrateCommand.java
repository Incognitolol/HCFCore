package rip.alpha.hcf.crates.command;

import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.mcscrims.command.annotation.Command;
import net.mcscrims.command.annotation.Param;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.ItemBuilder;
import net.mcscrims.libraries.util.gson.GsonUtil;
import net.mcscrims.libraries.util.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.crates.Crate;
import rip.alpha.hcf.crates.CrateHandler;
import rip.alpha.hcf.crates.CrateItem;
import rip.alpha.hcf.crates.impl.CommandCrateItem;
import rip.alpha.hcf.crates.impl.ItemstackCrateItem;
import rip.alpha.hcf.crates.impl.KOTHCrateItem;
import rip.alpha.modsuite.ModSuiteMode;
import rip.alpha.modsuite.ModSuitePlugin;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CrateCommand {
    private static final CrateHandler CRATE_HANDLER = HCF.getInstance().getCrateHandler();

    @Command(names = "crate create", permission = "hcf.crate.create", async = true)
    public static void crateCreate(CommandSender sender, @Param(name = "name") String name) {
        if (CRATE_HANDLER.getCrateByName(name) != null) {
            sender.sendMessage(CC.RED + "That crate already exists.");
            return;
        }

        Crate crate = new Crate(UUID.randomUUID(), name);
        CRATE_HANDLER.createCrate(crate);
        sender.sendMessage(CC.GREEN + "You have created " + name + " crate!");
    }

    @Command(names = "crate remove", permission = "hcf.crate.remove", async = true)
    public static void crateRemove(CommandSender sender, @Param(name = "crate") Crate crate) {
        CRATE_HANDLER.uncacheCrate(crate);
        sender.sendMessage(CC.GREEN + "You have deleted " + crate.getName() + " crate!");
    }

    @Command(names = "crate addcommanditem", permission = "hcf.crate.additem", async = true)
    public static void crateAddCommandItem(Player player, @Param(name = "crate") Crate crate,
                                           @Param(name = "id") int id, @Param(name = "weight") int weight,
                                           @Param(name = "command", wildcard = true) String command) {
        ItemStack item = player.getInventory().getItemInHand();

        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(CC.RED + "You must be holding the item in your hand");
            return;
        }

        CommandCrateItem commandCrateItem = new CommandCrateItem(item, weight, id, 0, command);
        crate.getItems().put(id, commandCrateItem);
        commandCrateItem.saveCrateItem(crate.getName(), 0);

        player.sendMessage(CC.GREEN + "You have added that item to " + crate.getName() + "'s item list.");
    }

    @Command(names = "crate additem", permission = "hcf.crate.additem", async = true)
    public static void crateAddItem(Player player, @Param(name = "crate") Crate crate, @Param(name = "id") int id,
                                    @Param(name = "weight") int weight) {
        ItemStack item = player.getInventory().getItemInHand();

        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(CC.RED + "You must be holding the item in your hand");
            return;
        }

        ItemstackCrateItem itemstackCrateItem = new ItemstackCrateItem(item, weight, id, 1);
        crate.getItems().put(id, itemstackCrateItem);
        itemstackCrateItem.saveCrateItem(crate.getName(), 1);
        player.sendMessage(CC.GREEN + "You have added that item to " + crate.getName() + "'s item list.");
    }

    @Command(names = "crate addkothitem", permission = "hcf.crate.additem", async = true)
    public static void crateAddKothItem(Player player, @Param(name = "crate") Crate crate, @Param(name = "id") int id,
                                        @Param(name = "weight") int weight) {
        ItemStack item = player.getInventory().getItemInHand();

        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(CC.RED + "You must be holding the item in your hand");
            return;
        }

        KOTHCrateItem kothCrateItem = new KOTHCrateItem(item, weight, id, 2);
        crate.getItems().put(id, kothCrateItem);
        kothCrateItem.saveCrateItem(crate.getName(), 2);
        player.sendMessage(CC.GREEN + "You have added that item to " + crate.getName() + "'s item list.");
    }

    @Command(names = "crate setkey", permission = "hcf.crate.setkey", async = true)
    public static void crateSetKey(Player player, @Param(name = "crate") Crate crate) {
        ItemStack item = player.getInventory().getItemInHand();

        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(CC.RED + "You must be holding the item in your hand");
            return;
        }

        crate.setKey(item);
        CRATE_HANDLER.getCratesConfig().getConfig().set("crates." + crate.getName() + ".key", GsonUtil.GSON.toJson(item));
        CRATE_HANDLER.getCratesConfig().save();
        player.sendMessage(CC.GREEN + "You have set the key for " + crate.getName());
    }

    @Command(names = "crate setdelay", permission = "hcf.crate.setdelay", async = true)
    public static void crateSetDelay(Player player, @Param(name = "crate") Crate crate, @Param(name = "delay") int delay) {
        crate.setOpenDelay(delay);
        CRATE_HANDLER.getCratesConfig().getConfig().set("crates." + crate.getName() + ".delay", delay);
        CRATE_HANDLER.getCratesConfig().save();
    }

    @Command(names = "crate getitem", permission = "hcf.crate.getitem", async = true)
    public static void crateGetItem(Player player, @Param(name = "crate") Crate crate, @Param(name = "index") int index) {
        ObjectCollection<CrateItem> items = crate.getItems().values();
        if (index > items.size() || index < 0){
            player.sendMessage(CC.RED + "That item doesnt exist!");
            return;
        }

        CrateItem crateItem = crate.getItems().get(index);
        crateItem.giveItem(player);
        player.sendMessage(CC.GREEN + "You have been given that item from the crate!");
    }

    @Command(names = "crate getkey", permission = "hcf.crate.getkey", async = true)
    public static void crateGetKey(Player player, @Param(name = "crate") Crate crate,
                                   @Param(name = "amount", defaultValue = "1") int amount) {
        if (crate.getKey() == null) {
            player.sendMessage(CC.RED + "There is not key set for this crate.");
            return;
        }

        if (amount > 64) {
            player.sendMessage(CC.RED + "You cannot give yourself more than 64 keys at a time.");
            return;
        }

        if (amount < 1) {
            player.sendMessage(CC.RED + "You cannot give yourself less than 1 key.");
            return;
        }

        ItemStack key = crate.getKey();

        // So that we don't modify the original ItemStack
        if (amount != 1) {
            key = key.clone();
            key.setAmount(amount);
        }

        player.getInventory().addItem(key);
        player.sendMessage(CC.GREEN + "You have received the key for " + crate.getName());
    }

    @Command(names = "crate keyall", permission = "hcf.crate.keyall", async = true)
    public static void crateKeyAll(Player player, @Param(name = "crate") Crate crate,
                                   @Param(name = "amount", defaultValue = "1") int amount) {
        if (crate.getKey() == null) {
            player.sendMessage(CC.RED + "There is not key set for this crate.");
            return;
        }

        if (amount > 64) {
            player.sendMessage(CC.RED + "You cannot give more than 64 keys at a time.");
            return;
        }

        if (amount < 1) {
            player.sendMessage(CC.RED + "You cannot give less than 1 key.");
            return;
        }

        ItemStack key = crate.getKey();

        // So that we don't modify the original ItemStack
        if (amount != 1) {
            key = key.clone();
            key.setAmount(amount);
        }

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        String receiveMessage = CC.GREEN + "You have received " + amount + "x " + crate.getName() + " crate key" + (amount == 1 ? "" : "s") + ".";
        int i = 0;
        for (Player online : onlinePlayers) {
            if (ModSuitePlugin.getInstance().getModSuiteAPI().getCurrentMode(player.getPlayer()) == ModSuiteMode.NONE) {
                ItemUtils.giveItem(online, key);
                online.sendMessage(receiveMessage);
                i++;
            }
        }

        player.sendMessage(CC.GREEN + "You have successfully given " + i + " players "
                + amount + "x " + crate.getName() + " crate key.");
    }

    @Command(names = "crate givecrate", permission = "hcf.crate.givecrate", async = true)
    public static void crateGiveKey(Player player, @Param(name = "crate") Crate crate) {
        ItemStack itemStack = new ItemBuilder(Material.ENDER_CHEST).name(CC.B_BLUE + crate.getName())
                .lore(CC.GRAY + "Crate").build();
        player.getInventory().addItem(itemStack);
        player.sendMessage(CC.GREEN + "You have given yourself the crate for " + crate.getName());
    }

    @Command(names = "crate list", permission = "hcf.crate.list", async = true)
    public static void crateList(Player player) {
        player.sendMessage(CC.GOLD + "Crates: ");
        for (Crate crate : CRATE_HANDLER.getCrateMap().values()) {
            player.sendMessage(CC.GRAY + " - " + crate.getName());
        }
    }
}
