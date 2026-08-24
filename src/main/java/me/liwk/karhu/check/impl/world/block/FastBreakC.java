package me.liwk.karhu.check.impl.world.block;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.*;
import me.liwk.karhu.handler.collision.type.MaterialChecks;
import me.liwk.karhu.util.ReflectionUtil;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.MathHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

@CheckInfo(name = "FastBreak (C)", category = Category.WORLD, subCategory = SubCategory.BLOCK, experimental = false)
public final class FastBreakC extends PacketCheck {

    public boolean digStarted;
    public float toolDigEfficiency, blockHardness, curBlockDamage;
    public Block block;

    public FastBreakC(KarhuPlayer data, Karhu karhu) {
        super(data, karhu);
    }

    @Override
    public void handle(Event packet) {

        if(Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_9)) return;

        if(packet instanceof SwingEvent) {
            if(digStarted && block != null) {

                this.blockHardness = Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_16)
                        ? 0.05F
                        : ReflectionUtil.getBlockDurability(block);

                boolean canBreak = ReflectionUtil.canDestroyBlock(data, block);

                this.simulateDig(canBreak);

                this.curBlockDamage += ((this.toolDigEfficiency / this.blockHardness) / (!canBreak ? 100.0F : 30.0F));
            }
        } else if(packet instanceof DigEvent) {

            Player player = data.getBukkitPlayer();

            DiggingAction digType = ((DigEvent) packet).getDigType();

            Location blockLocation = new Location(player.getWorld(),
                    ((DigEvent) packet).getBlockPos().getX(),
                    ((DigEvent) packet).getBlockPos().getY(),
                    ((DigEvent) packet).getBlockPos().getZ());


            Block block = Karhu.getInstance().getChunkManager().getChunkBlockAt(blockLocation);

            if(block == null) return;

            if(Karhu.SERVER_VERSION.getProtocolVersion() >= 47) {
                if (block.getType() == Material.BARRIER
                        && data.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_7_10)) return;
            }

            switch (digType) {
                case START_DIGGING: {

                    this.digStarted = true;

                    this.curBlockDamage = 0;

                    this.block = block;

                    boolean canBreak = ReflectionUtil.canDestroyBlock(data, block);

                    this.simulateDig(canBreak);

                    this.blockHardness = Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_16)
                            ? 0.05F
                            : ReflectionUtil.getBlockDurability(block);

                    this.curBlockDamage += ((this.toolDigEfficiency / this.blockHardness) / (!canBreak ? 100.0F : 30.0F));
                    break;
                }

                case FINISHED_DIGGING: {

                    if (digStarted) {

                        if (curBlockDamage < 0.7F) {
                            double speed = 1.0F / curBlockDamage;

                            fail("* Fastbreak (speed edited)\n" +
                                    "\n§f* speed: §b" + speed +
                                    "\n§f* item: §b" + data.getStackInHand().getType(), getBanVL(), 600L);

                            data.setCancelBreak(true);
                        }

                        this.curBlockDamage = this.toolDigEfficiency = 0;
                        this.digStarted = false;
                        this.block = null;
                    }

                    break;
                }

                case CANCELLED_DIGGING:
                    this.digStarted = false;
                    break;

                //The braindead shiit

                case RELEASE_USE_ITEM: {
                    if (digStarted) {
                        boolean canBreak = ReflectionUtil.canDestroyBlock(data, block);

                        this.simulateDig(canBreak);

                        this.curBlockDamage += Math.abs((this.toolDigEfficiency / this.blockHardness) / (!canBreak ? 100.0F : 30.0F));
                        //Bukkit.broadcastMessage("§bcur " + this.curBlockDamage);
                    }
                    break;
                }

                default: {
                    if (digStarted) {
                        boolean canBreak = ReflectionUtil.canDestroyBlock(data, block);

                        this.simulateDig(canBreak);

                        this.curBlockDamage += Math.abs((this.toolDigEfficiency / this.blockHardness) / (!canBreak ? 100.0F : 30.0F));
                    }
                    break;
                }

            }


        } else if(packet instanceof RespawnEvent) {
            //Respawns could fuckup shit, because tickloop stops.
            this.curBlockDamage = 1.1F;
        } else if(packet instanceof HeldItemSlotEvent) {
            //KYS
            this.curBlockDamage = 1.1F;
            this.toolDigEfficiency = 0;
            this.digStarted = false;
            this.block = null;
        }
    }

    private boolean testWater(KarhuPlayer data) {
        CustomLocation location = data.getLocation();
        double d0 = location.getY() + 1.62D;

        int i = MathHelper.floor_double(location.getX());
        int j = (int)MathHelper.floor_double_long((float) MathHelper.floor_double(d0));
        int k = MathHelper.floor_double(location.getZ());

        Location bukkitLocation = new Location(data.getWorld(), i, j, k);

        Block block = Karhu.getInstance().getChunkManager().getChunkBlockAt(bukkitLocation);

        if(block == null) return false;

        if (MaterialChecks.WATER.contains(block.getType())) {
            float f = testWaterHeight(block.getData()) - 0.11111111F;
            float f1 = (float)(j + 1) - f;
            return d0 < (double)f1;
        } else {
            return false;
        }
    }

    private float testWaterHeight(int i) {
        if (i >= 8) {
            i = 0;
        }
        return (i + 1) / 9.0F;
    }

    private void simulateDig(boolean canBreak) {
        float destroySpeed = data.getStackInHand().getType() == Material.AIR
                ? 1.0F
                : 1.0F * ReflectionUtil.getDestroySpeed(block, data);

        if (destroySpeed > 1.0F) {
            int enchLvl = data.getStackInHand().getEnchantmentLevel(Enchantment.DIG_SPEED);
            if (enchLvl > 0) {
                float f1 = (float)(enchLvl * enchLvl + 1);
                if (!canBreak && destroySpeed <= 1.0F) {
                    destroySpeed += f1 * 0.08F;
                } else {
                    destroySpeed += f1;
                }
            }
        }

        if(data.getHaste() != 0) {
            destroySpeed *= 1.0F + data.getHaste() * 0.2F;
        }

        if(data.getFatigue() != 0) {
            if(Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_8)) {
                float f1;

                switch (data.getFatigue()) {
                    case 1:
                        f1 = 0.3F;
                        break;

                    case 2:
                        f1 = 0.09F;
                        break;

                    case 3:
                        f1 = 0.0027F;
                        break;

                    case 4:
                    default:
                        f1 = 8.1E-4F;
                }

                destroySpeed *= f1;
            } else {
                destroySpeed *= 1.0F - data.getFatigue() * 0.2F;
            }
        }

        /*if(testWater(data) && !MovementUtils.searchEnchant(data.getBukkitPlayer(), Enchantment.WATER_WORKER)) {
            destroySpeed /= 5.0F;
        }*/

        this.toolDigEfficiency = destroySpeed;
    }
}