package com.threesix.util;

import net.minecraft.client.util.Window;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.system.MemoryStack;
import com.threesix.util.XorBitUtils;
import com.threesix.util.GlStateSnapshot;
import com.threesix.util.StringVaultDecoder;

public final class NanoTextRenderer {
   public static boolean isEnabled2 = true;
   private static long nvgContext = 0L;
   private static boolean isDisabled = false;
   private static int failureCount = 0;

   private NanoTextRenderer() {
   }

   public static boolean isAvailable() {
      return isEnabled2 && !isDisabled;
   }

   private static boolean ensureContext() {
      if (!isAvailable()) {
         return false;
      }

      if (nvgContext != 0L) {
         return true;
      }

      try {
         nvgContext = NanoVGGL3.nvgCreate(1);
         if (nvgContext == 0L) {
            isDisabled = true;
            return false;
         } else {
            return true;
         }
      } catch (Throwable error) {
         isDisabled = true;
         return false;
      }
   }

   private static NVGColor toNvgColor(MemoryStack memoryStack, int intVal) {
      return NanoVG.nvgRGBA((byte)(intVal >> 16 & 0xFF), (byte)(intVal >> 8 & 0xFF), (byte)(intVal & 0xFF), (byte)(intVal >>> 24 & 0xFF), NVGColor.malloc(memoryStack));
   }

   private static int scaleAlpha(int intVal, float floatVal) {
      int intVal2 = intVal >>> 24 & 0xFF;
      int minValue = Math.min(255, (int)((intVal >> 16 & 0xFF) * floatVal));
      int minValue2 = Math.min(255, (int)((intVal >> 8 & 0xFF) * floatVal));
      int minValue3 = Math.min(255, (int)((intVal & 0xFF) * floatVal));
      return intVal2 << 24 | minValue << 16 | minValue2 << 8 | minValue3;
   }

   private static void applyTransform(Matrix3x2fStack matrix3x2fStack) {
      NanoVG.nvgResetTransform(nvgContext);
      NanoVG.nvgTransform(nvgContext, matrix3x2fStack.m00, matrix3x2fStack.m01, matrix3x2fStack.m10, matrix3x2fStack.m11, matrix3x2fStack.m20, matrix3x2fStack.m21);
   }

   public static boolean drawRoundedRect(DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, int intVal) {

      if (!ensureContext() || arg == null) {
         return false;
      }

      if (!(floatVal3 <= 0.0F) && !(floatVal4 <= 0.0F)) {
         if ((intVal >>> 24 & 0xFF) == 0) {
            return true;
         }

         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc != null && mc.getWindow() != null) {
            GlStateSnapshot glStateSnapshotValue;
            try {
               glStateSnapshotValue = GlStateSnapshot.capture();
            } catch (Throwable error) {
               return false;
            }

            boolean trueSnapshot;
            try {
               MemoryStack memoryStackValue = MemoryStack.stackPush();

               try {
                  Window var7Value = mc.getWindow();
                  NanoVG.nvgBeginFrame(nvgContext, var7Value.getScaledWidth(), var7Value.getScaledHeight(), var7Value.getScaleFactor());

                  try {
                     NanoVG.nvgSave(nvgContext);

                     try {
                        applyTransform(arg.getMatrices());
                        NanoVG.nvgBeginPath(nvgContext);
                        NanoVG.nvgRoundedRect(nvgContext, floatVal, floatVal2, floatVal3, floatVal4, Math.max(0.0F, floatVal5));
                        NanoVG.nvgFillColor(nvgContext, toNvgColor(memoryStackValue, intVal));
                        NanoVG.nvgFill(nvgContext);
                     } finally {
                        try {
                           NanoVG.nvgRestore(nvgContext);
                        } catch (Throwable error2) {
                        }
                     }
                  } finally {
                     try {
                        NanoVG.nvgEndFrame(nvgContext);
                     } catch (Throwable error3) {
                     }
                  }

                  failureCount = 0;
                  trueSnapshot = true;
               } catch (Throwable error4) {
                  if (memoryStackValue != null) {
                     try {
                        memoryStackValue.close();
                     } catch (Throwable error5) {
                        error4.addSuppressed(error5);
                     }
                  }

                  throw error4;
               }

               if (memoryStackValue != null) {
                  memoryStackValue.close();
               }
            } catch (Throwable error6) {
               if (++failureCount >= 10) {
                  isDisabled = true;
               }

               return false;
            } finally {
               try {
                  glStateSnapshotValue.restore();
               } catch (Throwable error7) {
               }
            }

            return trueSnapshot;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public static boolean drawGradientRect(DrawContext arg, float floatVal, float floatVal2, float floatVal3, float floatVal4, float floatVal5, int intVal) {
      if (!ensureContext() || arg == null) {
         return false;
      }

      if (!(floatVal3 <= 0.0F) && !(floatVal4 <= 0.0F)) {
         if ((intVal >>> 24 & 0xFF) == 0) {
            return true;
         }

         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc != null && mc.getWindow() != null) {
            GlStateSnapshot glStateSnapshotValue;
            try {
               glStateSnapshotValue = GlStateSnapshot.capture();
            } catch (Throwable error) {
               return false;
            }

            boolean trueSnapshot;
            try {
               MemoryStack memoryStackValue = MemoryStack.stackPush();

               try {
                  Window var7Value = mc.getWindow();
                  int intVal2 = scaleAlpha(intVal, 1.35F);
                  NanoVG.nvgBeginFrame(nvgContext, var7Value.getScaledWidth(), var7Value.getScaledHeight(), var7Value.getScaleFactor());

                  try {
                     NanoVG.nvgSave(nvgContext);

                     try {
                        applyTransform(arg.getMatrices());
                        NVGPaint nVGPaintValue = NVGPaint.malloc(memoryStackValue);
                        NanoVG.nvgLinearGradient(nvgContext, floatVal, floatVal2, floatVal, floatVal2 + floatVal4, toNvgColor(memoryStackValue, intVal2), toNvgColor(memoryStackValue, intVal), nVGPaintValue);
                        NanoVG.nvgBeginPath(nvgContext);
                        NanoVG.nvgRoundedRect(nvgContext, floatVal, floatVal2, floatVal3, floatVal4, Math.max(0.0F, floatVal5));
                        NanoVG.nvgFillPaint(nvgContext, nVGPaintValue);
                        NanoVG.nvgFill(nvgContext);
                     } finally {
                        try {
                           NanoVG.nvgRestore(nvgContext);
                        } catch (Throwable error2) {
                        }
                     }
                  } finally {
                     try {
                        NanoVG.nvgEndFrame(nvgContext);
                     } catch (Throwable error3) {
                     }
                  }

                  failureCount = 0;
                  trueSnapshot = true;
               } catch (Throwable error4) {
                  if (memoryStackValue != null) {
                     try {
                        memoryStackValue.close();
                     } catch (Throwable error5) {
                        error4.addSuppressed(error5);
                     }
                  }

                  throw error4;
               }

               if (memoryStackValue != null) {
                  memoryStackValue.close();
               }
            } catch (Throwable error6) {
               if (++failureCount >= 10) {
                  isDisabled = true;
               }

               return false;
            } finally {
               try {
                  glStateSnapshotValue.restore();
               } catch (Throwable error7) {
               }
            }

            return trueSnapshot;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

}
