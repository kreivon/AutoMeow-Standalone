package me.kreivon.automeow;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

public class AutoMeow implements ModInitializer {

	private static MinecraftClient client;
	private boolean toggled = false;
	private long lastMeow;
	private int cooldown = 10000;
	public static final KeyBinding toggleKey = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.automeow.toggle", GLFW.GLFW_KEY_UNKNOWN, "category.automeow.name")
	);
	public static final KeyBinding cycleCooldownKey = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.automeow.cycle", GLFW.GLFW_KEY_UNKNOWN, "category.automeow.name")
	);
	public static final Text MOD_PREFIX =
			Text.literal("")
					.append(Text.literal("[").formatted(Formatting.DARK_GRAY))
					.append(Text.literal("AutoMeow").formatted(Formatting.LIGHT_PURPLE))
					.append(Text.literal("]").formatted(Formatting.DARK_GRAY));

	@Override
	public void onInitialize() {
		client = MinecraftClient.getInstance();

		ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			if (toggled) {
				if (client.player != null && message.getString().toUpperCase().contains("MEOW")) {
					if (sender != null && sender.getId() == client.player.getUuid()) {
						lastMeow = Util.getMeasuringTimeMs();
						return true;
					}
					if (Util.getMeasuringTimeMs() - lastMeow >= cooldown) {
						lastMeow = Util.getMeasuringTimeMs();
						sendMessage("meow :3");
					}
				}
			}
			return true;
		});
		ClientReceiveMessageEvents.MODIFY_GAME.register(((message, overlay) -> {
			if (toggled) {
				if (client.player != null && message.getString().toUpperCase().contains("MEOW") && !message.contains(client.player.getName())) {
					if (message.getString().toUpperCase().contains(client.player.getName().getString().toUpperCase())) {
						lastMeow = Util.getMeasuringTimeMs();
					} else if (Util.getMeasuringTimeMs() - lastMeow >= cooldown) {
						lastMeow = Util.getMeasuringTimeMs();
						sendMessage("meow :3");
					}
				}
			}
			return message;
		}));
		ClientTickEvents.END_CLIENT_TICK.register(client1 -> {
			if (toggleKey.wasPressed()) {
				toggled = !toggled;
				if (toggled) {
					info("AutoMeow enabled!");
				} else {
					info("AutoMeow disabled!");
				}
			}
			if (cycleCooldownKey.wasPressed()) {
				if (cooldown == 10000) {
					cooldown = 1000;
				} else {
					cooldown += 1000;
				}
				info(String.format("Cooldown set to %s ms!", cooldown));
			}
		});

	}

	/**
	 * Displays info to the chat with the mod prefix.
	 * @param message String to display as the message
	 */
	public static void info(String message) {
		if (client.player != null) {
			client.player.sendMessage(MOD_PREFIX.copy()
					.append(" ").append(Text.of(message)).formatted(Formatting.RESET).formatted(Formatting.GRAY));
		}
	}

	/**
	 * Sends a message to the game chat.
	 * @param message String message to send
	 */
	public static void sendMessage(String message) {
		client.inGameHud.getChatHud().addToMessageHistory(message);
		if (client.player != null) {
			if (message.startsWith("/")) client.player.networkHandler.sendChatCommand(message.substring(1));
			else client.player.networkHandler.sendChatMessage(message);
		}
	}

}