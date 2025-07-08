package com.dsp.main.Utils.Font.builders;

import com.dsp.main.Utils.Font.builders.impl.BorderBuilder;
import com.dsp.main.Utils.Font.builders.impl.TextBuilder;;

public final class Builder {

    private static final BorderBuilder BORDER_BUILDER = new BorderBuilder();
    private static final TextBuilder TEXT_BUILDER = new TextBuilder();


    public static BorderBuilder border() {
        return BORDER_BUILDER;
    }


    public static TextBuilder text() {
        return TEXT_BUILDER;
    }


}