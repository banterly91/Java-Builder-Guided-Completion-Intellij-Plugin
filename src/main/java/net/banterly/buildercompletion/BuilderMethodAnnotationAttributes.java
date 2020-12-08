package net.banterly.buildercompletion;

import com.google.common.collect.ImmutableSet;
import net.banterly.buildercompletion.annotations.BuilderMethod;

import java.util.Set;

public class BuilderMethodAnnotationAttributes {
    BuilderMethod.Type type;
    boolean repeatable;
    Set<String> incompatibleMethods;

    BuilderMethodAnnotationAttributes(BuilderMethod.Type type, boolean repeatable, Set<String> incompatibleMethods) {
        this.type = type;
        this.repeatable = repeatable;
        this.incompatibleMethods = ImmutableSet.copyOf(incompatibleMethods);
    }

    public BuilderMethod.Type getType() {
        return type;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public Set<String> getIncompatibleMethods() {
        return incompatibleMethods;
    }
}
