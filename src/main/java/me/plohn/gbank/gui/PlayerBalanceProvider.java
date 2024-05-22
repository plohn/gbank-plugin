package me.plohn.gbank.gui;

import io.github.johnnypixelz.utilizer.config.Configs;
import io.github.johnnypixelz.utilizer.itemstack.Items;
import io.github.johnnypixelz.utilizer.shade.smartinvs.ClickableItem;
import io.github.johnnypixelz.utilizer.shade.smartinvs.SmartInventory;
import io.github.johnnypixelz.utilizer.shade.smartinvs.content.InventoryContents;
import io.github.johnnypixelz.utilizer.shade.smartinvs.content.InventoryProvider;
import me.plohn.gbank.GBankCurrency;
import me.plohn.gbank.GBankManager;
import me.plohn.gbank.GBankPlayerProfile;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerBalanceProvider implements InventoryProvider {
    private static final int COLUMNS = 9;
    private GBankPlayerProfile playerProfile;
    private int menuPages;
    private int currentPage;
    private ArrayList<ArrayList<GBankCurrency>> menuItems = new ArrayList<>();

    public PlayerBalanceProvider(GBankPlayerProfile playerProfile, int page) {
        this.playerProfile = playerProfile;
        this.currentPage = page;
        divideItemsToPages();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ArrayList<GBankCurrency> serverCurrencies = GBankManager.getServerCurrencies();
        if (!menuItems.isEmpty()) {
            setGuiItems(menuItems.get(this.currentPage), contents, player);
            setGuiControls(contents, player);
            return;
        }
        setGuiItems(serverCurrencies, contents, player);
        setGuiControls(contents, player);
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        InventoryProvider.super.update(player, contents);
    }

    public void open(Player player, int page) {
        String menuTitle = Configs.get("language").getString("balance.header", "Your balances: ");
        GBankPlayerProfile playerProfile = GBankManager.getPlayerProfile(player);
        SmartInventory.builder()
                .title(menuTitle)
                .size(2, 9)
                .provider(new PlayerBalanceProvider(playerProfile, page))
                .build()
                .open(player);
    }

    private void divideItemsToPages() {
        ArrayList<GBankCurrency> serverCurrencies = GBankManager.getServerCurrencies();
        int totalCurrencies = serverCurrencies.size();

        if (totalCurrencies == 0) {
            this.menuPages = 0;
            return;
        }
        if (totalCurrencies < 9) {
            this.menuPages = 1;
            return;
        }

        this.menuPages = totalCurrencies / COLUMNS;
        if (totalCurrencies % COLUMNS > 0) {
            this.menuPages += 1;
        }

        for (int i = 0; i < totalCurrencies; i += COLUMNS) {
            int end = Math.min(totalCurrencies, i + COLUMNS);
            List<GBankCurrency> inventory = serverCurrencies.subList(i, end);
            menuItems.add(new ArrayList<>(inventory));
        }

    }

    private void setGuiItems(ArrayList<GBankCurrency> items, InventoryContents contents, Player player) {
        int slot = 0;
        for (GBankCurrency serverCurrency : items) {
            contents.set(0, slot, ClickableItem.of(Items.edit(Material.DIAMOND)
                    .setDisplayName(serverCurrency.getName())
                    .getItem(), event -> {

                double amount = playerProfile.getBalance(serverCurrency);

                String message = Configs.get("language")
                        .getString("balance.balance_format", "%prefix%%amount% %currency%")
                        .replace("%prefix%", serverCurrency.getPrefix())
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%currency%", serverCurrency.getName());

                player.closeInventory();
                sendToast(player, message);
            }));
            slot++;
        }
    }

    // Set appropriate controls for gui
    private void setGuiControls(InventoryContents contents, Player player) {

        ItemStack borderItem = Items.edit(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName("")
                .getItem();

        contents.fillRow(1, ClickableItem.empty(borderItem));

        Optional<ClickableItem> nextPageItem = getNextPageItem(player);
        Optional<ClickableItem> previousPage = getPreviousPage(player);

        if (nextPageItem.isEmpty()) {
            contents.set(1, 7, ClickableItem.empty(borderItem));
        } else {
            contents.set(1, 7, nextPageItem.get());
        }

        if (previousPage.isEmpty()) {
            contents.set(1, 2, ClickableItem.empty(borderItem));
        } else {
            contents.set(1, 2, previousPage.get());
        }
    }

    //This function returns a Clickable item for "Next Page" button,
    //if there are no other pages left, then will return an empty optional.
    private Optional<ClickableItem> getNextPageItem(Player player) {

        int newPage = currentPage + 1;

        if (this.menuItems.size() <= newPage) {
            return Optional.empty();
        }

        return Optional.of(ClickableItem.of(
                Items.edit(Material.ARROW)
                        .setDisplayName("Next Page")
                        .getItem(),
                event -> {
                    player.closeInventory();
                    open(player, newPage);
                }));
    }

    //This function returns a Clickable item for "Prev Page" button,
    //if there are no pages are before (current page is the first page ),
    //then will return an empty optional.
    private Optional<ClickableItem> getPreviousPage(Player player) {

        int newPage = currentPage - 1;

        if (currentPage == 0) {
            return Optional.empty();
        }

        return Optional.of(ClickableItem.of(
                Items.edit(Material.ARROW)
                        .setDisplayName("Previous Page")
                        .getItem(),
                event -> {
                    player.closeInventory();
                    open(player, newPage);
                }));
    }

    public void sendToast(Player player, String amount) {
        Toast.displayTo(player, "diamond", "Balance: " + amount, Toast.Style.TASK);
    }
}
