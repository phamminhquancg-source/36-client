package com.threesix.module;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;
import com.threesix.render.WorldShapeRenderer;
import com.threesix.util.XorBitUtils;
import com.threesix.internal.ModuleBase;
import com.threesix.util.TextStyleUtil;
import com.threesix.data.ModuleCategory;
import com.threesix.util.StringVaultDecoder;
import com.threesix.setting.ClientSetting;

public final class TntExplosionMarkerModule extends ModuleBase {
   public static final double markerScale = 0.12;
   public final ClientSetting alphaSetting = new ClientSetting("Alpha", 80, 0, 255);
   public final ClientSetting chatNotifySetting = new ClientSetting("Chat Notify", true);
   public final Map<ChunkPos, Double> explosionHeights = new ConcurrentHashMap<>();

   public TntExplosionMarkerModule() {
      super("TNT Explosion Marker", ModuleCategory.RENDER);
      this.registerSetting(this.alphaSetting);
      this.registerSetting(this.chatNotifySetting);
   }

   @Override
   public void onDisable() {
      this.explosionHeights.clear();
   }

   @Override
   public void onPacketSend(Packet arg) {
      if (minecraftClient.world != null && minecraftClient.player != null && arg != null) {
         String local = arg.getClass().getSimpleName();
         if (local.equals("WorldEventS2CPacket")) {
            this.getExplosionCenter(arg);
         }

         if (local.toLowerCase().contains("explosion") || local.equals("WorldEventS2CPacket")) {
            Vec3d local2 = this.getExplosionCenter(arg);
            if (local2 != null && !Double.isNaN(local2.x) && !Double.isNaN(local2.y) && !Double.isNaN(local2.z)) {
               ChunkPos local3 = new ChunkPos((int)Math.floor(local2.x) >> 4, (int)Math.floor(local2.z) >> 4);
               this.explosionHeights.put(local3, local2.y);
               if ((Boolean)this.chatNotifySetting.getValue() && minecraftClient.inGameHud != null) {
                  minecraftClient.inGameHud
                     .getChatHud()
                     .addMessage(Text.literal("§c[TNT] §fChunk markiert §7(" + local3.x + ", " + local3.z + ")"));
               }
            }
         }
      }
   }

   @Override
   public void onRender(MatrixStack arg, float floatVal) {
      if (minecraftClient.world != null && minecraftClient.player != null && !this.explosionHeights.isEmpty()) {
         Camera textStyleUtilValue = TextStyleUtil.getGameRenderer();
         if (textStyleUtilValue != null) {
            Vec3d textStyleUtilValue2 = TextStyleUtil.getCameraRotation(textStyleUtilValue);
            Color colorInst = new Color(255, 60, 60, (Integer)this.alphaSetting.getValue());
            Color colorInst2 = new Color(255, 60, 60, 255);
            GL11.glDisable(2929);
            arg.push();

            try {
               WorldShapeRenderer textStyleUtilValue3 = TextStyleUtil.acquireRenderer(arg);

               for (Entry<ChunkPos, Double> entry : this.explosionHeights.entrySet()) {
                  ChunkPos local = (ChunkPos)entry.getKey();
                  double doubleVal = (Double)entry.getValue();
                  double doubleVal2 = (local.x << 4) - textStyleUtilValue2.x;
                  double doubleVal3 = (local.z << 4) - textStyleUtilValue2.z;
                  double var1316Value = doubleVal2 + 16.0;
                  double var1516Value = doubleVal3 + 16.0;
                  double var11TextStyleUtilValue2Va = doubleVal - textStyleUtilValue2.y;
                  double var210Value = var11TextStyleUtilValue2Va + 0.12;
                  textStyleUtilValue3.fillBox(doubleVal2, var11TextStyleUtilValue2Va, doubleVal3, var1316Value, var210Value, var1516Value, colorInst);
                  textStyleUtilValue3.strokeBox(doubleVal2, var11TextStyleUtilValue2Va, doubleVal3, var1316Value, var210Value, var1516Value, colorInst2);
               }
            } catch (Exception error) {
            } finally {
               arg.pop();
               GL11.glEnable(2929);
            }
         }
      }
   }

   public Vec3d getExplosionCenter(Object object) {
      Object local = this.readField(object, "center");
      if (local == null) {
         local = this.readField(object, "getCenter");
      }

      if (local instanceof Vec3d) {
         return (Vec3d)local;
      }

      Double local2 = this.readDoubleProperty(object, "x", "getX");
      Double local3 = this.readDoubleProperty(object, "y", "getY");
      Double local4 = this.readDoubleProperty(object, "z", "getZ");
      if (local2 != null && local3 != null && local4 != null) {
         return new Vec3d(local2, local3, local4);
      }

      Float local5 = this.readFloatProperty(object, "x", "getX");
      Float local6 = this.readFloatProperty(object, "y", "getY");
      Float local7 = this.readFloatProperty(object, "z", "getZ");
      if (local5 != null && local6 != null && local7 != null) {
         return new Vec3d(local5.floatValue(), local6.floatValue(), local7.floatValue());
      }

      Vec3d local8 = this.readVec3dField(object);
      if (local8 != null) {
         return local8;
      }

      local2 = this.readDoubleField(object, "x");
      local3 = this.readDoubleField(object, "y");
      local4 = this.readDoubleField(object, "z");
      return local2 != null && local3 != null && local4 != null ? new Vec3d(local2, local3, local4) : null;
   }

   public Object readField(Object object, String string) {

      for (Class index = object.getClass(); index != null; index = index.getSuperclass()) {
         for (Method method : index.getDeclaredMethods()) {
            if (method.getName().equals(string) && method.getParameterCount() == 0) {
               try {
                  method.setAccessible(true);
                  return method.invoke(object);
               } catch (Throwable error) {
               }
            }
         }
      }

      return null;
   }

   public Double readDoubleProperty(Object object, String string, String string2) {
      Object local = this.readField(object, string);
      if (!(local instanceof Number)) {
         local = this.readField(object, string2);
      }

      return local instanceof Number local2 ? local2.doubleValue() : null;
   }

   public Float readFloatProperty(Object object, String string, String string2) {
      Object local = this.readField(object, string);
      if (!(local instanceof Float)) {
         local = this.readField(object, string2);
      }

      return local instanceof Float local2 ? local2 : null;
   }

   public Vec3d readVec3dField(Object object) {

      for (Class index = object.getClass(); index != null; index = index.getSuperclass()) {
         for (Field field : index.getDeclaredFields()) {
            try {
               field.setAccessible(true);
               Object local = field.get(object);
               if (local instanceof Vec3d) {
                  return (Vec3d)local;
               }
            } catch (Throwable error) {
            }
         }
      }

      return null;
   }

   public Double readDoubleField(Object object, String string) {

      for (Class index = object.getClass(); index != null; index = index.getSuperclass()) {
         for (Field field : index.getDeclaredFields()) {
            if (field.getName().toLowerCase().contains(string)) {
               try {
                  field.setAccessible(true);
                  if (field.get(object) instanceof Number local) {
                     return local.doubleValue();
                  }
               } catch (Throwable error) {
               }
            }
         }
      }

      return null;
   }

}
