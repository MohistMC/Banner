package com.mohistmc.banner.mixin.util;

import com.mohistmc.banner.stackdeobf.mappings.RemappingUtil;
import net.minecraft.util.ThreadingDetector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThreadingDetector.class)
public class MixinThreadingDetector {

    @Redirect(method = "stackTrace", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;getStackTrace()[Ljava/lang/StackTraceElement;"))
    private static StackTraceElement[] redirStackTrace(Thread thread) {
        StackTraceElement[] stackTrace = thread.getStackTrace();
        RemappingUtil.remapStackTraceElements(stackTrace);
        return stackTrace;
    }
}