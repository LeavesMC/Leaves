package org.leavesmc.leaves.config.annotations;

import org.leavesmc.leaves.config.AutoConfigTransformer;
import org.leavesmc.leaves.config.ConfigTransformer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RemovedConfig.Array.class)
public @interface RemovedConfig {
    String name();

    String[] category();

    boolean transform() default false;

    Class<? extends ConfigTransformer<?, ?>> transformer() default AutoConfigTransformer.class;

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Array {
        RemovedConfig[] value();
    }
}
