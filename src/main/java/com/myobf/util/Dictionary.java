package com.myobf.util;

import com.myobf.name.NameGenerator;
import org.objectweb.asm.tree.ClassNode;

public enum Dictionary {
    FIELD,
    METHOD; // Added for future use, though not in user's code yet.

    private final NameGenerator nameGenerator = new NameGenerator();

    /**
     * Gets a new unique name for a member within the context of a class.
     * The ClassNode is passed for potential future logic, but is currently unused.
     * @param classNode The class context.
     * @return A new unique name.
     */
    public String getNewName(ClassNode classNode) {
        // The provided ClassNode is ignored for now, as the NameGenerator is global.
        // This could be enhanced to generate names scoped to the class if needed.
        return switch (this) {
            case FIELD -> nameGenerator.newFieldName();
            case METHOD -> nameGenerator.newMethodName();
        };
    }
}
