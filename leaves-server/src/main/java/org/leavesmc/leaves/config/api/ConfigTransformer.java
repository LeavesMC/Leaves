package org.leavesmc.leaves.config.api;

public interface ConfigTransformer<FROM, TO> extends ConfigConverter<FROM> {
    TO transform(FROM from) throws StopTransformException;

    class StopTransformException extends RuntimeException {
    }
}
