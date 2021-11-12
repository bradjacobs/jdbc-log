package com.github.bradjacobs.logging.jdbc.param.renderer;

import com.github.bradjacobs.logging.jdbc.param.SqlParamRenderer;

import java.math.BigDecimal;

/**
 * Number values like Double/Float will _not_ show up in scientific notation by default.
 */
public class NumberParamRenderer implements SqlParamRenderer<Number> {

    private final boolean allowScientificNotation;

    public NumberParamRenderer() {
        this(false);
    }
    public NumberParamRenderer(boolean allowScientificNotation) {
        this.allowScientificNotation = allowScientificNotation;
    }

    @Override
    public void appendParamValue(Number value, StringBuilder sb) {
        String numberString = value.toString();

        if (!allowScientificNotation && numberString.contains("E")) {
            numberString = BigDecimal.valueOf(value.doubleValue()).toPlainString();
        }

        sb.append(numberString);
    }
}