package com.tiabfix.compat;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AE2TickHandler {

    private static Class<?> inscriberClass = null;
    private static Method getProcessingTimeMethod = null;
    private static Method setProcessingTimeMethod = null;
    private static Method getMaxProcessingTimeMethod = null;
    private static Method setSmashMethod = null;
    private static Method getTaskMethod = null;
    private static Field finalStepField = null;
    private static Method markForUpdateMethod = null;
    private static boolean initialized = false;
    private static boolean available = false;

    private static void init() {
        if (initialized)
            return;
        initialized = true;

        try {
            inscriberClass = Class.forName("appeng.blockentity.misc.InscriberBlockEntity");

            getProcessingTimeMethod = inscriberClass.getMethod("getProcessingTime");
            setProcessingTimeMethod = inscriberClass.getMethod("setProcessingTime", int.class);
            getMaxProcessingTimeMethod = inscriberClass.getMethod("getMaxProcessingTime");
            setSmashMethod = inscriberClass.getMethod("setSmash", boolean.class);
            getTaskMethod = inscriberClass.getMethod("getTask");

            finalStepField = inscriberClass.getDeclaredField("finalStep");
            finalStepField.setAccessible(true);

            markForUpdateMethod = inscriberClass.getMethod("markForUpdate");

            available = true;
        } catch (Exception e) {
            available = false;
        }
    }

    public static boolean tryTick(BlockEntity blockEntity) {
        init();
        if (!available) {
            return false;
        }

        try {
            if (!inscriberClass.isInstance(blockEntity)) {
                return false;
            }

            Object task = getTaskMethod.invoke(blockEntity);
            if (task == null) {
                return true;
            }

            int processingTime = (int) getProcessingTimeMethod.invoke(blockEntity);
            int maxProcessingTime = (int) getMaxProcessingTimeMethod.invoke(blockEntity);

            if (processingTime < maxProcessingTime) {
                int newTime = Math.min(processingTime + 10, maxProcessingTime);
                setProcessingTimeMethod.invoke(blockEntity, newTime);

                if (newTime >= maxProcessingTime) {
                    setSmashMethod.invoke(blockEntity, true);
                    finalStepField.setInt(blockEntity, 0);
                    markForUpdateMethod.invoke(blockEntity);
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
