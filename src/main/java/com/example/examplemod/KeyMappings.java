package com.example.examplemod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class KeyMappings {
    public static final String bindingGroup = "tetra.binding.group";
    public static final KeyMapping reloadMapping = new KeyMapping("tetra.gunblade.binding.reload", TetraKeyConflictContext.secondaryInteraction, InputConstants.Type.KEYSYM, 82, bindingGroup);

    enum TetraKeyConflictContext implements IKeyConflictContext {
        secondaryInteraction {
            public boolean isActive() {
                return Minecraft.getInstance().screen == null;
            }

            public boolean conflicts(IKeyConflictContext other) {
                return this == other;
            }
        };
    }
}