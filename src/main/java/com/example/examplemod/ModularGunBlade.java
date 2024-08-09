package com.example.examplemod;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.properties.TetraAttributes;

public class ModularGunBlade extends ItemModularHandheld {

    private static final GuiModuleOffsets majorOffsets = new GuiModuleOffsets(new int[]{-13, 0, -13, 18, 4, 0});
    private static final GuiModuleOffsets minorOffsets = new GuiModuleOffsets(new int[]{13, 18, 3, 28});

    public ModularGunBlade(Properties properties) {
        super(properties);
        this.majorModuleKeys = new String[]{"sword/blade", "sword/hilt", "gunblade/receiver"};
        this.minorModuleKeys = new String[]{"sword/fuller", "sword/pommel"};
        this.requiredModules = new String[]{"sword/blade", "sword/hilt", "gunblade/receiver"};
        this.baseIntegrity = 10;
        this.updateConfig(ConfigHandler.honeSwordBase.get(), ConfigHandler.honeSwordIntegrityMultiplier.get());
        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this, "modular_gunblade"));
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }

    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMajorGuiOffsets(ItemStack itemStack) {
        return majorOffsets;
    }

    @OnlyIn(Dist.CLIENT)
    public GuiModuleOffsets getMinorGuiOffsets(ItemStack itemStack) {
        return minorOffsets;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            int multishotEnchantLevel = stack.getEnchantmentLevel(Enchantments.MULTISHOT) * 3;
            int count = Math.max(this.getEffectLevel(stack, ItemEffect.multishot) + multishotEnchantLevel, 1);
            double spread = (double) this.getEffectEfficiency(stack, ItemEffect.multishot);
            for (int i = 0; i < count; ++i) {
                double yaw = (double) player.getYRot() - spread * (double) (count - 1) / 2.0 + spread * (double) i;
                boolean isDupe = player.getAbilities().instabuild || count > 1 && i != count / 2;
                this.fireProjectile(world, stack, new ItemStack(Items.ARROW,1), player, yaw, isDupe);
            }
            stack.hurtAndBreak(1, player, (p) -> {
                p.broadcastBreakEvent(p.getUsedItemHand());
            });
            this.applyUsageEffects(player, stack, 1.0);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return super.use(world, player, hand);
    }
    public static float getProjectileVelocity(double strength, float velocityBonus) {
        float velocity = (float)Math.max(1.0, 1.0 + (strength - 6.0) * 0.125);
        velocity += velocity * velocityBonus;
        return velocity;
    }
    protected void fireProjectile(Level world, ItemStack crossbowStack, ItemStack ammoStack, Player player, double yaw, boolean isDupe) {
        double strength = this.getAttributeValue(crossbowStack, (Attribute) TetraAttributes.drawStrength.get());
        float velocityBonus = (float)this.getEffectLevel(crossbowStack, ItemEffect.velocity) / 100.0F;
        float projectileVelocity = getProjectileVelocity(strength, velocityBonus);
        ArrowItem ammoItem = (ArrowItem) CastOptional.cast(ammoStack.getItem(), ArrowItem.class).orElse((ArrowItem)Items.ARROW);
        AbstractArrow projectile = ammoItem.createArrow(world, ammoStack, player);
        projectile.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        projectile.setShotFromCrossbow(true);
        projectile.setCritArrow(true);
        projectile.setBaseDamage(projectile.getBaseDamage() - 2.0 + strength / 3.0);
        if (projectileVelocity > 1.0F) {
            projectile.setBaseDamage(projectile.getBaseDamage() / (double)projectileVelocity);
        }

        int piercingLevel = this.getEffectLevel(crossbowStack, ItemEffect.piercing) + crossbowStack.getEnchantmentLevel(Enchantments.PIERCING);
        if (piercingLevel > 0) {
            projectile.setPierceLevel((byte)piercingLevel);
        }

        if (isDupe) {
            projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }

        projectile.shootFromRotation(player, player.getXRot(), (float)yaw, 0.0F, projectileVelocity * 3.15F, 1.0F);
        world.addFreshEntity(projectile);
    }
}
