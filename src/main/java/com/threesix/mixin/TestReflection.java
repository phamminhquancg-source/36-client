package com.threesix.mixin;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import net.minecraft.client.Keyboard;

public class TestReflection {
   public static void main(String[] string) {
      for (Method method : Keyboard.class.getDeclaredMethods()) {
         if (method.getName().equals("onChar")) {
            for (Parameter parameter : method.getParameters()) {
               parameter.getType().getName();
            }
         }
      }
   }
}
