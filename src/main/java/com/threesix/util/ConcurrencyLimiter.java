package com.threesix.util;

import java.util.concurrent.atomic.AtomicInteger;
import com.threesix.util.XorBitUtils;
import com.threesix.util.StringVaultDecoder;

public final class ConcurrencyLimiter {
   private final AtomicInteger activeCount = new AtomicInteger();

   public boolean tryAcquire(int intVal) {
      int local;
      do {
         local = this.activeCount.get();
         if (local >= intVal) {
            return false;
         }
      } while (!this.activeCount.compareAndSet(local, local + 1));

      return true;
   }

   public void release() {
      this.activeCount.decrementAndGet();
   }

   public void reset() {
      this.activeCount.set(0);
   }

}
