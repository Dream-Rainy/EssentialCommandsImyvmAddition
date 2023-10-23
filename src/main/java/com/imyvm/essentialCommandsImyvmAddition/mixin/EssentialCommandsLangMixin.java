package com.imyvm.essentialCommandsImyvmAddition.mixin;

import com.fibermc.essentialcommands.text.ECText;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.io.InputStream;

import static com.fibermc.essentialcommands.EssentialCommands.LOGGER;
import static com.fibermc.essentialcommands.text.ECText.DEFAULT_LANGUAGE_SPEC;
import static com.fibermc.essentialcommands.text.ECText.load;

@Mixin(ECText.class)
public class EssentialCommandsLangMixin {
    @Inject(method = "create", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
    private static void addCustomLanguage(String langId, CallbackInfoReturnable<ECText> cir, ImmutableMap.Builder<String, String> builder) {
        final String resourceFString = "/assets/essential-commands-imyvm-addition/lang/%s.json";
        final String resourceLocation = String.format(resourceFString, langId);
        try {
            InputStream inputStream = ECText.class.getResourceAsStream(resourceLocation);
            if (inputStream == null) {
                LOGGER.info(String.format("No EC lang file for the language '%s' found. Defaulting to 'en_us'.", langId));
                inputStream = ECText.class.getResourceAsStream(String.format(resourceFString, DEFAULT_LANGUAGE_SPEC));
            }

            try {
                if (inputStream != null) {
                    load(inputStream, builder::put);
                }
            } catch (Throwable loadEx) {
                try {
                    inputStream.close();
                } catch (Throwable closeEx) {
                    loadEx.addSuppressed(closeEx);
                }

                throw loadEx;
            }

            if (inputStream != null) {
                inputStream.close();
            }
        } catch (JsonParseException | IOException ex) {
            LOGGER.error("Couldn't read strings from {}", resourceLocation, ex);
        }
    }
}
