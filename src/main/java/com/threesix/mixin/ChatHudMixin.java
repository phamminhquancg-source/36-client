package com.threesix.mixin;

import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.threesix.module.NameProtectModule;
import com.threesix.manager.ConfigManager;
import com.threesix.module.AutoSellModule;
import com.threesix.internal.ModuleBase;
import com.threesix.module.FakeRolesModule;

@Mixin(ChatHud.class)
public class ChatHudMixin {
   @Shadow
   private List messages;

   @Inject(method = "addMessage*", at = @At("RETURN"))
   private void afterAddMessage(CallbackInfo callbackInfo) {
      ModuleBase configManagerValue = ConfigManager.INSTANCE.getModuleByName("RTP Home Reset");
      if (configManagerValue != null && configManagerValue.isEnabled() && this.messages != null && !this.messages.isEmpty()) {
         this.messages
            .removeIf(
               item -> {
                  if (item == null) {
                     return false;
                  }

                  try {
                     for (Method method : item.getClass().getDeclaredMethods()) {
                        if (method.getParameterCount() == 0) {
                           method.setAccessible(true);
                           if (method.invoke(item) instanceof Text local2) {
                              String local = local2.getString().toLowerCase();
                              return local.contains("home deleted")
                                 || local.contains("home set")
                                 || local.contains("teleported to a random location")
                                 || local.contains("teleported to your home");
                           }
                        }
                     }
                  } catch (Throwable error) {
                  }

                  return false;
               }
            );
      }
   }

   @ModifyVariable(method = "addMessage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private Text modifyChatMessage(Text arg) {
      if (arg != null) {
         try {
            AutoSellModule.handleChatMessage(arg.getString());
         } catch (Throwable error) {
         }
      }

      if (arg != null && NameProtectModule.nameProtectModule != null && NameProtectModule.nameProtectModule.isEnabled() && MinecraftClient.getInstance().getSession() != null) {
         String class310Value = MinecraftClient.getInstance().getSession().getUsername();
         if (class310Value != null) {
            String local = arg.getString();
            if (local.contains(class310Value)) {
               arg = Text.literal(local.replace(class310Value, NameProtectModule.nameProtectModule.getMaskedName()));
            }
         }
      }

      return FakeRolesModule.applyFakeRoles((Text)arg);
   }
}
