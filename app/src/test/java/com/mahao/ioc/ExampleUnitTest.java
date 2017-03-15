package com.mahao.ioc;

import android.text.TextUtils;

import org.junit.Test;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    public static final DecimalFormat FORMAT_PERCENT_DECIMAL_0 = new DecimalFormat("0%"); // 百分比，没有小数位
    public static final DecimalFormat FORMAT_NUMBER_DECIMAl_2 = new DecimalFormat("0.00"); // 格式化数据，保留两位小数
    public static final DecimalFormat FORMAT_AMOUNT = new DecimalFormat("###,###,###,##0.00");

    @Test
    public void testDf(){


        System.out.println(formatPercentWithoutPercent(123458));
//        Systemstem.out.println(format2Decimal2("100.1"));

    }


    public static double formatPercentWithoutPercent(long mm) {
        return new BigDecimal(mm).divide(new BigDecimal(100d)).doubleValue();
    }



    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}