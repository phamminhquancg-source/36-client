package com.threesix.data;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class BaseFinderSearchState {
   public static boolean isSearchActive = false;
   public static boolean isRotating = false;
   public static float spoofedYaw = 0.0F;
   public static float spoofedPitch = 0.0F;
   public static float syncPitch = 0.0F;
   public static float snapshotPitch = 0.0F;
   public static Hand pendingHand;
   public static BlockHitResult pendingHitResult;
   public static BlockPos pendingPos;
   public static long nextActionTimeMillis = 0L;
   public static boolean isSchematicPlacing = false;
   public static boolean hasPendingRotation = false;
   public static float targetYaw = 0.0F;
   public static float targetPitch = 0.0F;
   public static float snapshotYaw = 0.0F;
   public static float syncYaw = 0.0F;
   public static float sellSpoofYaw = 0.0F;
   public static float sellSpoofPitch = 0.0F;
   public static int placedBlockCount = 0;
   public static int maxPendingActions = 2;
   public static boolean isRotationReady = false;
   public static int placementTicks = 0;
   public static boolean isProbeInteracting = false;
   public static final List pendingProbes = new ArrayList();

}
