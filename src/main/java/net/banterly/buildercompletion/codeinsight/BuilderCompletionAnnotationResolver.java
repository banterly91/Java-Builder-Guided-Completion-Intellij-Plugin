package net.banterly.buildercompletion.codeinsight;

import com.google.common.collect.ImmutableMap;
import com.intellij.codeInsight.daemon.quickFix.ExternalLibraryResolver;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ExternalLibraryDescriptor;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public class BuilderCompletionAnnotationResolver extends ExternalLibraryResolver {
    private static final String ANNOTATIONS_PACKAGE = "net.banterly.buildercompletion.annotations";
    private static final ExternalLibraryDescriptor BUILDER_COMPLETION = new ExternalLibraryDescriptor("net.banterly", "builder-guided-completion-annotations",
            null, null, "0.1.5");

    private static final Map<String, String> shortName2Fqn = ImmutableMap.of(
            "BuilderClass","net.banterly.buildercompletion.annotations.BuilderClass",
            "BuilderMethod", "net.banterly.buildercompletion.annotations.BuilderMethod",
            "BuildMethod", "net.banterly.buildercompletion.annotations.BuildMethod"
    );

    @Override
    public @Nullable ExternalClassResolveResult resolveClass(@NotNull String shortClassName, @NotNull ThreeState isAnnotation, @NotNull Module contextModule) {
        if (isAnnotation == ThreeState.YES && shortName2Fqn.containsKey(shortClassName)) {
            return new ExternalClassResolveResult(shortName2Fqn.get(shortClassName), BUILDER_COMPLETION);
        }
        return null;
    }

    @Override
    public @Nullable ExternalLibraryDescriptor resolvePackage(@NotNull String packageName) {
        if (ANNOTATIONS_PACKAGE.equals(packageName)) {
            return BUILDER_COMPLETION;
        }
        return null;
    }
}
