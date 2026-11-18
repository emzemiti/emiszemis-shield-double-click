package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ClientModInitializer {
    public static KeyBinding toggleKey;
    private static boolean isEnabled = false;
    private static boolean wasRightClickPressed = false;
    private static boolean scheduleNextTickClick = false;

    @Override
    public void onInitializeClient() {
        // 1. Register the 'G' toggle keybind in Controls
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.shieldclick.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.shieldclick.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // Handle G key toggle
            while (toggleKey.wasPressed()) {
                isEnabled = !isEnabled;
                client.player.sendMessage(
                    Text.literal(isEnabled ? "§aShield Double-Click: ON" : "§cShield Double-Click: OFF"),
                    true
                );
            }

            // 2. If scheduled from the previous tick, trigger the second click now
            if (scheduleNextTickClick) {
                scheduleNextTickClick = false;
                KeyBinding.onKeyPressed(client.options.useKey.getDefaultKey());
            }

            if (!isEnabled) return;

            // 3. Detect fresh right-click press while looking at an entity blocking with a shield
            boolean isRightClickDown = client.options.useKey.isPressed();
            if (isRightClickDown && !wasRightClickPressed) {
                if (client.targetedEntity instanceof LivingEntity target && target.isBlocking()) {
                    scheduleNextTickClick = true;
                }
            }
            wasRightClickPressed = isRightClickDown;
        });
    }
}
