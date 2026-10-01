package com.threesix.util;

import org.lwjgl.opengl.GL33C;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class GlStateSnapshot {
   private int program;
   private int vertexArrayBinding;
   private int arrayBufferBinding;
   private int shaderStorageBufferBinding;
   private int shaderStorageBufferIndexed;
   private int activeTextureUnit;
   private int textureBinding2D;
   private int samplerBinding;
   private int blendSrcRgb;
   private int blendSrcAlpha;
   private int blendDstRgb;
   private int blendDstAlpha;
   private int blendEquationRgb;
   private int blendEquationAlpha;
   private int cullFaceMode;
   private int frontFace;
   private int depthFunc;
   private int stencilFunc;
   private int stencilRef;
   private int stencilValueMask;
   private int stencilWriteMask;
   private int stencilFail;
   private int stencilPassDepthFail;
   private int stencilPassDepthPass;
   private boolean isBlendEnabled;
   private boolean isCullFaceEnabled;
   private boolean isDepthTestEnabled;
   private boolean isStencilTestEnabled;
   private boolean isScissorTestEnabled;
   private boolean depthWriteMask;
   private final int[] viewport = new int[4];
   private final int[] scissorBox = new int[4];
   private final boolean[] colorWriteMask = new boolean[4];
   private int unpackAlignment;
   private int unpackRowLength;
   private int unpackSkipPixels;
   private int unpackSkipRows;

   private GlStateSnapshot() {
   }

   public static GlStateSnapshot capture() {
      GlStateSnapshot glStateSnapshotInst = new GlStateSnapshot();
      glStateSnapshotInst.program = GL33C.glGetInteger(35725);
      glStateSnapshotInst.vertexArrayBinding = GL33C.glGetInteger(34229);
      glStateSnapshotInst.arrayBufferBinding = GL33C.glGetInteger(34964);
      glStateSnapshotInst.shaderStorageBufferBinding = GL33C.glGetInteger(35368);
      glStateSnapshotInst.shaderStorageBufferIndexed = GL33C.glGetIntegeri(35368, 0);
      glStateSnapshotInst.activeTextureUnit = GL33C.glGetInteger(34016);
      GL33C.glActiveTexture(33984);
      glStateSnapshotInst.textureBinding2D = GL33C.glGetInteger(32873);
      glStateSnapshotInst.samplerBinding = GL33C.glGetInteger(35097);
      GL33C.glBindSampler(0, 0);
      GL33C.glActiveTexture(glStateSnapshotInst.activeTextureUnit);
      glStateSnapshotInst.isBlendEnabled = GL33C.glIsEnabled(3042);
      glStateSnapshotInst.blendSrcRgb = GL33C.glGetInteger(32969);
      glStateSnapshotInst.blendSrcAlpha = GL33C.glGetInteger(32968);
      glStateSnapshotInst.blendDstRgb = GL33C.glGetInteger(32971);
      glStateSnapshotInst.blendDstAlpha = GL33C.glGetInteger(32970);
      glStateSnapshotInst.blendEquationRgb = GL33C.glGetInteger(32777);
      glStateSnapshotInst.blendEquationAlpha = GL33C.glGetInteger(34877);
      glStateSnapshotInst.isCullFaceEnabled = GL33C.glIsEnabled(2884);
      glStateSnapshotInst.cullFaceMode = GL33C.glGetInteger(2885);
      glStateSnapshotInst.frontFace = GL33C.glGetInteger(2886);
      glStateSnapshotInst.isDepthTestEnabled = GL33C.glIsEnabled(2929);
      glStateSnapshotInst.depthFunc = GL33C.glGetInteger(2932);
      glStateSnapshotInst.depthWriteMask = GL33C.glGetBoolean(2930);
      glStateSnapshotInst.isStencilTestEnabled = GL33C.glIsEnabled(3089);
      glStateSnapshotInst.isScissorTestEnabled = GL33C.glIsEnabled(2960);
      glStateSnapshotInst.stencilFunc = GL33C.glGetInteger(2962);
      glStateSnapshotInst.stencilRef = GL33C.glGetInteger(2967);
      glStateSnapshotInst.stencilValueMask = GL33C.glGetInteger(2963);
      glStateSnapshotInst.stencilWriteMask = GL33C.glGetInteger(2968);
      glStateSnapshotInst.stencilFail = GL33C.glGetInteger(2964);
      glStateSnapshotInst.stencilPassDepthFail = GL33C.glGetInteger(2965);
      glStateSnapshotInst.stencilPassDepthPass = GL33C.glGetInteger(2966);
      GL33C.glGetIntegerv(2978, glStateSnapshotInst.viewport);
      GL33C.glGetIntegerv(3088, glStateSnapshotInst.scissorBox);
      int[] local = new int[4];
      GL33C.glGetIntegerv(3107, local);

      for (int index = 0; index < 4; index++) {
         glStateSnapshotInst.colorWriteMask[index] = local[index] != 0;
      }

      glStateSnapshotInst.unpackAlignment = GL33C.glGetInteger(3317);
      glStateSnapshotInst.unpackRowLength = GL33C.glGetInteger(3314);
      glStateSnapshotInst.unpackSkipPixels = GL33C.glGetInteger(3316);
      glStateSnapshotInst.unpackSkipRows = GL33C.glGetInteger(3315);
      return glStateSnapshotInst;
   }

   public void restore() {

      GL33C.glUseProgram(this.program);
      GL33C.glBindVertexArray(this.vertexArrayBinding);
      GL33C.glBindBuffer(34962, this.arrayBufferBinding);
      GL33C.glBindBufferBase(35345, 0, this.shaderStorageBufferIndexed);
      GL33C.glBindBuffer(35345, this.shaderStorageBufferBinding);
      GL33C.glActiveTexture(33984);
      GL33C.glBindTexture(3553, this.textureBinding2D);
      GL33C.glBindSampler(0, this.samplerBinding);
      GL33C.glActiveTexture(this.activeTextureUnit);
      setCapability(3042, this.isBlendEnabled);
      GL33C.glBlendFuncSeparate(this.blendSrcRgb, this.blendSrcAlpha, this.blendDstRgb, this.blendDstAlpha);
      GL33C.glBlendEquationSeparate(this.blendEquationRgb, this.blendEquationAlpha);
      setCapability(2884, this.isCullFaceEnabled);
      GL33C.glCullFace(this.cullFaceMode);
      GL33C.glFrontFace(this.frontFace);
      setCapability(2929, this.isDepthTestEnabled);
      GL33C.glDepthFunc(this.depthFunc);
      GL33C.glDepthMask(this.depthWriteMask);
      setCapability(3089, this.isStencilTestEnabled);
      setCapability(2960, this.isScissorTestEnabled);
      GL33C.glStencilFunc(this.stencilFunc, this.stencilRef, this.stencilValueMask);
      GL33C.glStencilMask(this.stencilWriteMask);
      GL33C.glStencilOp(this.stencilFail, this.stencilPassDepthFail, this.stencilPassDepthPass);
      GL33C.glViewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);
      GL33C.glScissor(this.scissorBox[0], this.scissorBox[1], this.scissorBox[2], this.scissorBox[3]);
      GL33C.glColorMask(this.colorWriteMask[0], this.colorWriteMask[1], this.colorWriteMask[2], this.colorWriteMask[3]);
      GL33C.glPixelStorei(3317, this.unpackAlignment);
      GL33C.glPixelStorei(3314, this.unpackRowLength);
      GL33C.glPixelStorei(3316, this.unpackSkipPixels);
      GL33C.glPixelStorei(3315, this.unpackSkipRows);
   }

   private static void setCapability(int intVal, boolean flag) {
      if (flag) {
         GL33C.glEnable(intVal);
      } else {
         GL33C.glDisable(intVal);
      }
   }

}
