package com.crypto.currency.data.vo;

import lombok.Data;

/**
 * @author Panzi
 * @Description Represents the result of parsing a market string
 * @date 2022/5/16 21:51
 */
@Data
public class ParsedTradePairVO {
    private String mainSymbol;
    private String baseSymbol;

    public ParsedTradePairVO(String baseSymbol, String mainSymbol) {
        this.mainSymbol = mainSymbol;
        this.baseSymbol = baseSymbol;
    }

    public ParsedTradePairVO() {
    }
}
