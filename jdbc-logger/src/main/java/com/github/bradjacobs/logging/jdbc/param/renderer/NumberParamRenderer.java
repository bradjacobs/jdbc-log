package com.github.bradjacobs.logging.jdbc.param.renderer;

import com.github.bradjacobs.logging.jdbc.param.SqlParamRenderer;

import java.math.BigDecimal;

/**
 * Converts Number to string  (will 'fix' any scientific notation as necessary)
 */
public class NumberParamRenderer implements SqlParamRenderer<Number> {

    @Override
    public void appendParamValue(Number value, StringBuilder sb) {
        String numberString = value.toString();

        if (numberString.contains("E")) {
            // if the string value number contains 'E', then it's scientific notation,
            //   and will regenerate with BigDecimal to make normal looking number.
            numberString = BigDecimal.valueOf(value.doubleValue()).toPlainString();
        }

        sb.append(numberString);
    }
}