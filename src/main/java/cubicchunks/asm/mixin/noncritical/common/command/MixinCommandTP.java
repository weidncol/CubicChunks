/*
 *  This file is part of Cubic Chunks Mod, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package cubicchunks.asm.mixin.noncritical.common.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandTP;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import cubicchunks.world.ICubicWorld;
import mcp.MethodsReturnNonnullByDefault;

import static cubicchunks.asm.JvmNames.COMMAND_TP_GET_COMMAND_SENDER_AS_PLAYER;
import static cubicchunks.asm.JvmNames.COMMAND_TP_GET_ENTITY;
import static net.minecraft.command.CommandBase.getCommandSenderAsPlayer;
import static net.minecraft.command.CommandBase.getEntity;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mixin(CommandTP.class)
public class MixinCommandTP {
	@Nullable private WeakReference<ICubicWorld> commandWorld;

	@Inject(method = "execute",
	        at = @At(value = "INVOKE", target = COMMAND_TP_GET_ENTITY, ordinal = 0))
	private void postGetEntityInject(MinecraftServer server, ICommandSender sender, String args[], CallbackInfo ci) {
		try {
			commandWorld = new WeakReference<>((ICubicWorld) getEntity(server, sender, args[0]).getEntityWorld());
		} catch (CommandException e) {
			commandWorld = null;
		}
	}

	@Inject(method = "execute",
	        at = @At(value = "INVOKE", target = COMMAND_TP_GET_COMMAND_SENDER_AS_PLAYER, ordinal = 0))
	private void postGetEntityPlayerInject(MinecraftServer server, ICommandSender sender, String args[], CallbackInfo ci) {
		try {
			commandWorld = new WeakReference<>((ICubicWorld) getCommandSenderAsPlayer(sender).getEntityWorld());
		} catch (PlayerNotFoundException e) {
			commandWorld = null;
		}
	}

	@ModifyConstant(method = "execute", constant = @Constant(intValue = -4096))
	private int getMinY(int orig) {
		if (commandWorld == null) {
			return orig;
		}
		ICubicWorld world = commandWorld.get();
		if (world == null) {
			return orig;
		}
		return world.getMinHeight() + orig;
	}

	@ModifyConstant(method = "execute", constant = @Constant(intValue = 4096))
	private int getMaxY(int orig) {
		if (commandWorld == null) {
			return orig;
		}
		ICubicWorld world = commandWorld.get();
		if (world == null) {
			return orig;
		}
		return world.getMaxHeight() + orig - 256;
	}
}
