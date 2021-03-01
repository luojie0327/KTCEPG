package com.ktc.epg;


/**
 * Interface for singletons obj.
 */
public interface TvSingletons {

    /**
     * Returns the @{@link TvSingletons} using the application context.
     */
    static TvSingletons getSingletons() {
        return DestroyApp.getSingletons();
    }

}
