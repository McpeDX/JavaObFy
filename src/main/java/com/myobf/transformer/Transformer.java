package com.myobf.transformer;

import org.objectweb.asm.tree.ClassNode;

/**
 * An interface for all obfuscation transformers.
 * Each transformer takes a ClassNode and applies its specific transformation.
 */
@FunctionalInterface
public interface Transformer {

    /**
     * Transforms the given ClassNode.
     *
     * @param cn The ClassNode to be transformed.
     */
    void transform(ClassNode cn);

}
