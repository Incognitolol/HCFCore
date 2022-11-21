package rip.alpha.hcf.shop.sell;

import net.mcscrims.libraries.util.BalanceUtil;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.items.ItemUtils;
import net.mcscrims.libraries.util.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ShopSellButton extends Button {

    private final ShopSellEntry entry;
    private final boolean view;

    private int materialAmount;
    private int blockAmount;

    public ShopSellButton(ShopSellEntry entry, boolean view) {
        this.entry = entry;
        this.view = view;
    }

    @Override
    public String getName(Player player) {
        return entry.getDisplayName();
    }

    @Override
    public List<String> getDescription(Player player) {
        this.rescan(player);

        List<String> lore = new ArrayList<>();
        lore.add(" ");

        if (materialAmount == 0 && blockAmount == 0) {
            lore.add("&7You have none of this type of item to sell");
            return CC.translateLines(lore);
        }

        int blocks = Math.min(blockAmount, 16);
        String leftClickAmount = BalanceUtil.formatBalance(this.entry.calculateAmount(materialAmount, blockAmount));
        String rightClickAmount = BalanceUtil.formatBalance(this.entry.calculateAmount(0, blocks));

        lore.add("&6Left-click to sell: &a$" + leftClickAmount);
        if (blockAmount > 0) {
            lore.add("&6 - &7" + blockAmount + "x " + entry.getDisplayName() + " Blocks");
        }
        if (materialAmount > 0) {
            lore.add("&6 - &7" + materialAmount + "x " + entry.getDisplayName());
        }

        if (entry.getBlockMaterial() != null) {
            lore.add(" ");

            lore.add("&6Right-click to sell: &a$" + rightClickAmount);
            lore.add("&6 - &7" + blocks + "x " + entry.getDisplayName() + " Blocks");
        }

        return CC.translateLines(lore);
    }

    @Override
    public Material getMaterial(Player player) {
        if (entry.getBlockMaterial() != null) {
            return entry.getBlockMaterial();
        } else {
            return entry.getMaterial();
        }
    }

    @Override
    public byte getDamageValue(Player player) {
        return 0;
    }

    @Override
    public void clicked(Player player, int i, ClickType clickType, int i1) {
        if (view) {
            player.sendMessage(CC.RED + "You cannot use this menu in view mode.");
            return;
        }

        this.rescan(player);

        if (clickType == ClickType.LEFT) {
            if (blockAmount <= 0 && materialAmount <= 0) {
                player.sendMessage(CC.RED + "You do not have enough items to sell.");
                return;
            }

            int amount = entry.calculateAmount(materialAmount, blockAmount);
            TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
            if (teamProfile == null) {
                return;
            }
            teamProfile.setBalance(teamProfile.getBalance() + amount);

            PlayerInventory inventory = player.getInventory();
            ItemUtils.removeItem(inventory, this.entry.getMaterial(), (short) (this.entry.getMaterial() == Material.INK_SACK ? 4 : 0), this.materialAmount);
            if (this.entry.getBlockMaterial() != null) {
                ItemUtils.removeItem(inventory, this.entry.getBlockMaterial(), (short) 0, this.blockAmount);
            }

            StringJoiner stringJoiner = new StringJoiner("&6, &r");
            if (blockAmount > 0) {
                stringJoiner.add("&7x" + blockAmount + "x " + entry.getDisplayName() + " Blocks");
            }
            if (materialAmount > 0) {
                stringJoiner.add("&7x" + materialAmount + " " + entry.getDisplayName());
            }

            String formattedAmount = BalanceUtil.formatBalance(amount);
            player.sendMessage(CC.translate("&6You have sold " + stringJoiner + "&6 for &a$" + formattedAmount + "&6."));
        } else if (entry.getBlockMaterial() != null && clickType == ClickType.RIGHT) {
            int blocks = Math.min(blockAmount, 16);
            int amount = entry.calculateAmount(0, blocks);
            TeamProfile teamProfile = HCF.getInstance().getProfileHandler().getProfile(player);
            if (teamProfile == null) {
                return;
            }
            teamProfile.setBalance(teamProfile.getBalance() + amount);

            String formattedAmount = BalanceUtil.formatBalance(amount);
            PlayerInventory inventory = player.getInventory();
            ItemUtils.removeItem(inventory, this.entry.getBlockMaterial(), (short) 0, blocks);
            player.sendMessage(CC.translate("&6You have sold &7" + blocks + "x " + entry.getDisplayName() + " Blocks&6 for &a$" + formattedAmount + "&6."));
        }
    }

    private void rescan(Player player) {
        PlayerInventory inventory = player.getInventory();
        this.materialAmount = this.scan(inventory, entry.getMaterial());
        this.blockAmount = this.scan(inventory, entry.getBlockMaterial());
    }

    private int scan(PlayerInventory inventory, Material material) {
        int amount = 0;

        for (ItemStack content : inventory.getContents()) {
            if (content == null || content.getType() == Material.AIR) {
                continue;
            }
            Material type = content.getType();

            if (type == material) {
                if (type == Material.INK_SACK) {
                    if (content.getDurability() != 4) {
                        continue;
                    }
                }
                amount += content.getAmount();
            }
        }

        return amount;
    }
}
