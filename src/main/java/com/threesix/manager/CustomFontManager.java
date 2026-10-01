package com.threesix.manager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import com.threesix.util.XorBitUtils;
import com.threesix.util.GuiRenderUtil;
import com.threesix.data.ClientFontType;
import com.threesix.data.HudTextEntry;
import com.threesix.util.StringVaultDecoder;
import com.threesix.render.ThreesixTexturePipeline;

public class CustomFontManager {
   public static final CustomFontManager INSTANCE7 = new CustomFontManager();
   public static volatile ClientFontType currentFontType = ClientFontType.XUONG;
   public Font font;
   public ClientFontType loadedFontType = null;
   public static volatile int textYOffset = -1;
   public final AtomicInteger textureIdCounter = new AtomicInteger(0);
   public final LinkedHashMap textTextureCache = new LinkedHashMap() {

      @Override
      public boolean removeEldestEntry(Entry local) {
         if (this.size() > 1024) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
               try {
                  mc.getTextureManager().destroyTexture(((HudTextEntry)local.getValue()).textureId);
               } catch (Exception error) {
               }
            } else {
               ((HudTextEntry)local.getValue()).nativeImage.close();
            }

            return true;
         } else {
            return false;
         }
      }

   };
   public final ConcurrentHashMap widthCache = new ConcurrentHashMap();
   private int cachedLineHeight = -1;

   public static void setTextYOffset(int intVal) {
      textYOffset = intVal;
   }

   public static void clearTextYOffset() {
   }

   public static void setFontType(ClientFontType clientFontType) {
      if (clientFontType != currentFontType) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc != null) {
            for (HudTextEntry hudTextEntry : (Iterable<HudTextEntry>)INSTANCE7.textTextureCache.values()) {
               if (hudTextEntry != null && hudTextEntry.textureId != null) {
                  try {
                     mc.getTextureManager().destroyTexture(hudTextEntry.textureId);
                  } catch (Exception error) {
                  }
               }
            }
         }

         currentFontType = clientFontType;
         INSTANCE7.font = null;
         INSTANCE7.loadedFontType = null;
         INSTANCE7.textTextureCache.clear();
         INSTANCE7.cachedLineHeight = -1;

         try {
            INSTANCE7.widthCache.clear();
         } catch (Throwable error2) {
         }
      }
   }

   public static ClientFontType getFontType() {
      return currentFontType;
   }

   public static String[] getFontDisplayNames() {
      ClientFontType[] clientFontTypeValue = ClientFontType.values();
      String[] local = new String[clientFontTypeValue.length];

      for (int index = 0; index < clientFontTypeValue.length; index++) {
         local[index] = clientFontTypeValue[index].displayName;
      }

      return local;
   }

   public void ensureFontLoaded() {
      ClientFontType currentFontTypeSnapshot = currentFontType;
      if (this.loadedFontType != currentFontTypeSnapshot || this.font == null) {
         this.loadedFontType = currentFontTypeSnapshot;
         if (currentFontTypeSnapshot == ClientFontType.VANILLA) {
            this.font = null;
         } else {
            try (InputStream local = this.getClass().getResourceAsStream(currentFontTypeSnapshot.resourcePath)) {
               if (local != null) {
                  this.font = Font.createFont(currentFontTypeSnapshot.fontType, local).deriveFont(0, currentFontTypeSnapshot.size);
                  return;
               }
            } catch (Exception error) {
            }

            this.font = new Font("SansSerif", 0, 16);
         }
      }
   }

   public HudTextEntry getTextTexture(String string) {
      String currentFontTypeValue = currentFontType.name() + ":" + string;
      HudTextEntry local = (HudTextEntry)this.textTextureCache.get(currentFontTypeValue);
      if (local != null) {
         return local;
      }

      local = this.createTextTexture(string);
      if (local != null) {
         this.textTextureCache.put(currentFontTypeValue, local);
      }

      return local;
   }

   public HudTextEntry createTextTexture(String string) {
      this.ensureFontLoaded();
      if (this.font == null) {
         return null;
      }

      BufferedImage bufferedImageInst = new BufferedImage(1, 1, 2);
      Graphics2D local = bufferedImageInst.createGraphics();
      local.setFont(this.font);
      FontRenderContext local2 = local.getFontRenderContext();
      Rectangle2D local3 = this.font.getStringBounds(string, local2);
      local.dispose();
      int maxValue = Math.max(1, (int)Math.ceil(local3.getWidth()) + 6);
      int maxValue2 = Math.max(1, (int)Math.ceil(this.font.getSize() * 1.3F));
      BufferedImage bufferedImageInst2 = new BufferedImage(maxValue, maxValue2, 2);
      Graphics2D local4 = bufferedImageInst2.createGraphics();
      local4.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
      local4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      local4.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      local4.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      local4.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      local4.setFont(this.font);
      local4.setColor(Color.WHITE);
      local4.drawString(string, 1, this.font.getSize() - 1);
      local4.dispose();

      try {
         ByteArrayOutputStream byteArrayOutputStreamInst = new ByteArrayOutputStream();
         ImageIO.write(bufferedImageInst2, "png", byteArrayOutputStreamInst);
         NativeImage class1011Value = NativeImage.read(new ByteArrayInputStream(byteArrayOutputStreamInst.toByteArray()));
         NativeImageBackedTexture local5 = new NativeImageBackedTexture(() -> {
            return "threesix_font_cache";
         }, class1011Value);
         Identifier class2960Id = Identifier.of("threesix", "font_cache_" + this.textureIdCounter.getAndIncrement());
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc != null) {
            mc.getTextureManager().registerTexture(class2960Id, local5);
         }

         return new HudTextEntry(local5, class2960Id, maxValue / 2, maxValue2 / 2);
      } catch (Exception error) {
         return null;
      }
   }

   public void drawText(DrawContext arg, String string, float floatVal, float floatVal2, int intVal) {
      if (string != null && !string.isEmpty() && (textYOffset < 0 || !(floatVal2 >= textYOffset))) {
         if (currentFontType == ClientFontType.VANILLA) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
               arg.drawText(mc.textRenderer, string, (int)floatVal, (int)floatVal2, intVal, false);
            }
         } else {
            HudTextEntry local = this.getTextTexture(string);
            if (local != null && local.nativeImage.getGlTextureView() != null) {
               Matrix4f guiRenderUtilValue = GuiRenderUtil.getContextProjectionMatrix(arg);
               ThreesixTexturePipeline.call2(guiRenderUtilValue, floatVal, floatVal2, local.halfWidth, local.halfHeight, local.nativeImage.getGlTextureView(), intVal, 0.0F);
            }
         }
      }
   }

   public int getLineHeight() {
      if (this.cachedLineHeight > 0) {
         return this.cachedLineHeight;
      }

      try {
         HudTextEntry local = this.getTextTexture("Ag");
         if (local != null && local.halfHeight > 0) {
            this.cachedLineHeight = local.halfHeight;
            return this.cachedLineHeight;
         }

         if (currentFontType == ClientFontType.VANILLA) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.textRenderer != null) {
               this.cachedLineHeight = 9;
               return this.cachedLineHeight;
            }
         }
      } catch (Throwable error) {
      }

      return 11;
   }

   public int getStringWidth(String string) {
      if (string == null || string.isEmpty()) {
         return 0;
      }

      if (currentFontType == ClientFontType.VANILLA) {
         MinecraftClient mc = MinecraftClient.getInstance();
         return mc != null ? mc.textRenderer.getWidth(string) : string.length() * 6;
      }

      Integer local = (Integer)this.widthCache.get(currentFontType.name() + ":" + string);
      if (local != null) {
         return local;
      }

      this.ensureFontLoaded();
      if (this.font == null) {
         return string.length() * 6;
      }

      BufferedImage bufferedImageInst = new BufferedImage(1, 1, 2);
      Graphics2D local2 = bufferedImageInst.createGraphics();
      local2.setFont(this.font);
      int intVal = local2.getFontMetrics().stringWidth(string) / 2;
      local2.dispose();
      if (this.widthCache.size() > 2048) {
         this.widthCache.clear();
      }

      this.widthCache.put(currentFontType.name() + ":" + string, intVal);
      return intVal;
   }

   public int getCharSequenceWidth(CharSequence charSequence) {
      return this.getStringWidth(charSequence != null ? charSequence.toString() : "");
   }

}
