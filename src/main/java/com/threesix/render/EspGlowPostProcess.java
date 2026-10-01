package com.threesix.render;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class EspGlowPostProcess {
   public static int mainFramebuffer = -1;
   public static int blurTextureA = -1;
   public static int blurFramebufferH = -1;
   public static int blurTextureB = -1;
   public static int blurFramebufferV = -1;
   public static int blurTextureC = -1;
   public static int blurProgramH = -1;
   public static int blurProgramV = -1;
   public static int compositeProgram = -1;
   public static int viewportWidth = -1;
   public static int viewportHeight = -1;
   public static boolean isRendering = false;
   public static int prevDrawFramebuffer = 0;
   public static boolean isInitialized = false;
   public static float glowStrength = 2.0F;

   public static void init() {
      if (!isRendering) {
         MinecraftClient mc = MinecraftClient.getInstance();
         int var0Value = mc.getFramebuffer().textureWidth;
         int var0Value2 = mc.getFramebuffer().textureHeight;

         try {
            ensureResources(var0Value, var0Value2);
         } catch (Exception error) {
            isInitialized = false;
            return;
         }

         if (isInitialized) {
            prevDrawFramebuffer = GL11.glGetInteger(36006);
            GL30.glBindFramebuffer(36160, mainFramebuffer);
            GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            GL11.glClear(16384);
            isRendering = true;
         }
      }

   }

   public static void apply() {
      if (isRendering && isInitialized) {
         isRendering = false;
         int viewportWidthSnapshot = viewportWidth;
         int viewportHeightSnapshot = viewportHeight;
         boolean gL11Value = GL11.glIsEnabled(3042);
         boolean gL11Value2 = GL11.glIsEnabled(2929);
         boolean gL11Value3 = GL11.glGetBoolean(2930);
         int gL11Value4 = GL11.glGetInteger(32969);
         int gL11Value5 = GL11.glGetInteger(32968);
         GL11.glDisable(2929);
         GL11.glDepthMask(false);
         GL30.glBindFramebuffer(36160, blurFramebufferH);
         GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         GL11.glClear(16384);
         GL11.glDisable(3042);
         GL20.glUseProgram(blurProgramH);
         GL13.glActiveTexture(33984);
         GL11.glBindTexture(3553, blurTextureA);
         GL20.glUniform1i(GL20.glGetUniformLocation(blurProgramH, "Sampler0"), 0);
         GL20.glUniform2f(GL20.glGetUniformLocation(blurProgramH, "uResolution"), viewportWidthSnapshot, viewportHeightSnapshot);
         GL11.glDrawArrays(4, 0, 6);
         GL30.glBindFramebuffer(36160, blurFramebufferV);
         GL11.glClear(16384);
         GL20.glUseProgram(blurProgramV);
         GL13.glActiveTexture(33984);
         GL11.glBindTexture(3553, blurTextureB);
         GL20.glUniform1i(GL20.glGetUniformLocation(blurProgramV, "Sampler0"), 0);
         GL20.glUniform2f(GL20.glGetUniformLocation(blurProgramV, "uResolution"), viewportWidthSnapshot, viewportHeightSnapshot);
         GL11.glDrawArrays(4, 0, 6);
         GL30.glBindFramebuffer(36160, prevDrawFramebuffer);
         GL11.glEnable(3042);
         GL14.glBlendFuncSeparate(770, 771, 1, 771);
         GL20.glUseProgram(compositeProgram);
         GL13.glActiveTexture(33984);
         GL11.glBindTexture(3553, blurTextureA);
         GL20.glUniform1i(GL20.glGetUniformLocation(compositeProgram, "Sampler0"), 0);
         GL13.glActiveTexture(33985);
         GL11.glBindTexture(3553, blurTextureC);
         GL20.glUniform1i(GL20.glGetUniformLocation(compositeProgram, "Sampler1"), 1);
         GL20.glUniform1f(GL20.glGetUniformLocation(compositeProgram, "uGlowStrength"), glowStrength);
         GL11.glDrawArrays(4, 0, 6);
         GL20.glUseProgram(0);
         GL13.glActiveTexture(33985);
         GL11.glBindTexture(3553, 0);
         GL13.glActiveTexture(33984);
         GL11.glBindTexture(3553, 0);
         GL14.glBlendFuncSeparate(gL11Value4, gL11Value5, gL11Value4, gL11Value5);
         if (!gL11Value) {
            GL11.glDisable(3042);
         }

         if (gL11Value2) {
            GL11.glEnable(2929);
         }

         GL11.glDepthMask(gL11Value3);
      }

   }

   public static boolean isReady() {
      return isInitialized;
   }

   public static void ensureResources(int intVal, int intVal2) {
      if (intVal != viewportWidth || intVal2 != viewportHeight || mainFramebuffer == -1 || !isInitialized) {
         viewportWidth = intVal;
         viewportHeight = intVal2;
         deleteResources();
         blurTextureA = createColorTexture(intVal, intVal2);
         blurTextureB = createColorTexture(intVal, intVal2);
         blurTextureC = createColorTexture(intVal, intVal2);
         mainFramebuffer = createFramebuffer(blurTextureA);
         blurFramebufferH = createFramebuffer(blurTextureB);
         blurFramebufferV = createFramebuffer(blurTextureC);
         String local = readShaderResource("esp_glow_vertex.vsh");
         blurProgramH = compileShaderProgram(local, readShaderResource("esp_blur_h_fragment.fsh"));
         blurProgramV = compileShaderProgram(local, readShaderResource("esp_blur_v_fragment.fsh"));
         compositeProgram = compileShaderProgram(local, readShaderResource("esp_composite_fragment.fsh"));
         isInitialized = true;
      }
   }

   public static int createColorTexture(int intVal, int intVal2) {
      int gL11Value = GL11.glGenTextures();
      GL11.glBindTexture(3553, gL11Value);
      GL11.glTexImage2D(3553, 0, 6408, intVal, intVal2, 0, 6408, 5121, (ByteBuffer)null);
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      GL11.glTexParameteri(3553, 10242, 33071);
      GL11.glTexParameteri(3553, 10243, 33071);
      return gL11Value;
   }

   public static int createFramebuffer(int intVal) {
      int gL30Value = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, gL30Value);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, intVal, 0);
      GL30.glBindFramebuffer(36160, 0);
      return gL30Value;
   }

   public static void deleteResources() {
      if (mainFramebuffer != -1) {
         GL30.glDeleteFramebuffers(mainFramebuffer);
      }

      if (blurFramebufferH != -1) {
         GL30.glDeleteFramebuffers(blurFramebufferH);
      }

      if (blurFramebufferV != -1) {
         GL30.glDeleteFramebuffers(blurFramebufferV);
      }

      if (blurTextureA != -1) {
         GL11.glDeleteTextures(blurTextureA);
      }

      if (blurTextureB != -1) {
         GL11.glDeleteTextures(blurTextureB);
      }

      if (blurTextureC != -1) {
         GL11.glDeleteTextures(blurTextureC);
      }

      isInitialized = false;
   }

   public static String readShaderResource(String string) {
      try {

         try (InputStream local = EspGlowPostProcess.class.getResourceAsStream("/assets/threesix/shaders/" + string)) {
            if (local == null) {
               throw new RuntimeException("Not found: " + string);
            } else {
               return new String(local.readAllBytes(), StandardCharsets.UTF_8);
            }
         }
      } catch (Exception error) {
         throw new RuntimeException("Load failed: " + string, error);
      }
   }

   public static int compileShaderProgram(String string, String string2) {
      int gL20Value = GL20.glCreateShader(35633);
      GL20.glShaderSource(gL20Value, string);
      GL20.glCompileShader(gL20Value);
      if (GL20.glGetShaderi(gL20Value, 35713) == 0) {
         throw new RuntimeException("VSH: " + GL20.glGetShaderInfoLog(gL20Value));
      }

      int gL20Value2 = GL20.glCreateShader(35632);
      GL20.glShaderSource(gL20Value2, string2);
      GL20.glCompileShader(gL20Value2);
      if (GL20.glGetShaderi(gL20Value2, 35713) == 0) {
         throw new RuntimeException("FSH: " + GL20.glGetShaderInfoLog(gL20Value2));
      }

      int gL20Value3 = GL20.glCreateProgram();
      GL20.glAttachShader(gL20Value3, gL20Value);
      GL20.glAttachShader(gL20Value3, gL20Value2);
      GL20.glLinkProgram(gL20Value3);
      GL20.glDeleteShader(gL20Value);
      GL20.glDeleteShader(gL20Value2);
      return gL20Value3;
   }

}
