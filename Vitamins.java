package tes.vitamins;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.block.Action;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Vitamins extends JavaPlugin implements Listener {
    private final HashMap<UUID, Integer> vitaminC = new HashMap<>();
    private final HashMap<UUID, Integer> vitaminD = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        startNutritionTimer();

        this.getCommand("givevitamincompass").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                ItemStack ironFragment = new ItemStack(Material.IRON_NUGGET); // 철 조각
                ItemMeta meta = ironFragment.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GREEN + "비타민보기");
                    ironFragment.setItemMeta(meta);
                    player.getInventory().addItem(ironFragment);
                    player.sendMessage(ChatColor.GREEN + "비타민보기 철 조각을 받았습니다!");
                }
            }
            return true;
        });
    }

    private void startNutritionTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    updateVitamin(player, vitaminC, "비타민 C", ChatColor.GREEN);
                    updateVitamin(player, vitaminD, "비타민 D", ChatColor.BLUE);
                    // openVitaminGUI(player); // 주기적으로 GUI를 열지 않도록 이 줄을 주석 처리하거나 제거하세요.
                }
            }
        }.runTaskTimer(this, 0L, 1200L); // 1200L = 1분마다 실행
    }

    private void updateVitamin(Player player, HashMap<UUID, Integer> vitaminMap, String vitaminName, ChatColor color) {
        UUID playerId = player.getUniqueId();
        int currentLevel = vitaminMap.getOrDefault(playerId, 10);
        currentLevel = Math.max(currentLevel - 1, 0);
        vitaminMap.put(playerId, currentLevel);

        if (currentLevel == 0) {
            player.sendMessage(color + player.getName() + "이(가) " + vitaminName + "가 부족합니다!");
            // 여기에 플레이어에게 효과를 적용하는 코드 추가
            if (vitaminName.equals("비타민 C")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 4));//채굴피로 5
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, Integer.MAX_VALUE, 0));//허기
            } else if (vitaminName.equals("비타민 D")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 4));//채굴피로 5
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0));//실명
            }
        }
    }

    private void recoverVitamin(Player player, HashMap<UUID, Integer> vitaminMap, int amount) {
        UUID playerId = player.getUniqueId();
        int currentLevel = vitaminMap.getOrDefault(playerId, 0);
        currentLevel = Math.min(currentLevel + amount, 10);
        vitaminMap.put(playerId, currentLevel);

        if (currentLevel > 0) {
            player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
            player.removePotionEffect(PotionEffectType.POISON);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.HUNGER);
        }
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Material consumed = event.getItem().getType();
        switch (consumed) {
            case APPLE:
                recoverVitamin(player, vitaminC, 2);
                break;
            case GOLDEN_APPLE:
                recoverVitamin(player, vitaminC, 6);
                break;
            case MELON_SLICE:
                recoverVitamin(player, vitaminC, 4);
                break;
            case SALMON:
                recoverVitamin(player, vitaminD, 2);
                break;
            case COD:
                recoverVitamin(player, vitaminD, 2);
                break;
            case COOKED_SALMON:
                recoverVitamin(player, vitaminD, 5);
                break;
            case COOKED_COD:
                recoverVitamin(player, vitaminD, 5);
                break;
            case TROPICAL_FISH:
                recoverVitamin(player, vitaminD, 7);
                break;


            case MILK_BUCKET: // 우유 버킷 소비 시 기존 효과 제거 후 특정 디버프 재적용
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    player.removePotionEffect(PotionEffectType.SLOW); // 느림 효과 제거
                    player.removePotionEffect(PotionEffectType.WEAKNESS); // 약화 효과 제거

                    // 비타민 수치 체크 후 디버프 재적용 로직
                    if (vitaminC.getOrDefault(player.getUniqueId(), 10) <= 0) { // 비타민 C 수치가 0 이하일 경우
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 4));//채굴피로 5
                        player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, Integer.MAX_VALUE, 1));//허기
                        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 2400, 0));//독
                    }
                    if (vitaminD.getOrDefault(player.getUniqueId(), 10) <= 0) { // 비타민 D 수치가 0 이하일 경우
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 4));//채굴피로 5
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0));//실명
                        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 2400, 0));//독
                    }

                    // 우유 양동이를 마신 플레이어에게만 메시지 표시 및 독 효과 부여
                    player.sendMessage("그러니까 꼼수 쓰지 말랬지! -개발자-");
                    player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 2400, 0)); // /채굴피로 5 1분간 디버프를 줌

                }, 1L); // 1틱 후에 디버프 재적용
                break;

        }
    }




    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("비타민 상태")) {
            event.setCancelled(true); // 인벤토리 클릭으로 아이템을 옮길 수 없게 합니다.
        }
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getDisplayName().equals(ChatColor.GREEN + "비타민보기")) {
                Action action = event.getAction();
                // 좌클릭 또는 우클릭인 경우 GUI를 엽니다.
                if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK ||
                        action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    openVitaminGUI(player);
                }
            }
        }
    }

    private void openVitaminGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "비타민 상태"); // 9칸짜리 GUI 생성

        ItemStack vitaminCItem = new ItemStack(Material.APPLE); // 비타민 C를 대표하는 아이템
        ItemMeta vitaminCItemMeta = vitaminCItem.getItemMeta();
        if (vitaminCItemMeta != null) {
            vitaminCItemMeta.setDisplayName(ChatColor.GREEN + "비타민 C 수준");
            vitaminCItemMeta.setLore(Arrays.asList("현재 수준: " + vitaminC.getOrDefault(player.getUniqueId(), 0)));
            vitaminCItem.setItemMeta(vitaminCItemMeta);
        }

        ItemStack vitaminDItem = new ItemStack(Material.MILK_BUCKET); // 비타민 D를 대표하는 아이템
        ItemMeta vitaminDItemMeta = vitaminDItem.getItemMeta();
        if (vitaminDItemMeta != null) {
            vitaminDItemMeta.setDisplayName(ChatColor.BLUE + "비타민 D 수준");
            vitaminDItemMeta.setLore(Arrays.asList("현재 수준: " + vitaminD.getOrDefault(player.getUniqueId(), 0)));
            vitaminDItem.setItemMeta(vitaminDItemMeta);
        }

        gui.setItem(3, vitaminCItem); // 비타민 C 아이템을 GUI의 4번째 칸에 배치
        gui.setItem(5, vitaminDItem); // 비타민 D 아이템을 GUI의 6번째 칸에 배치

        player.openInventory(gui); // 플레이어에게 GUI 열기
    }
}

