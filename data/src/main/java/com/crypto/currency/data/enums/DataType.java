package com.crypto.currency.data.enums;

/**
 * @author Panzi
 * @Description cryptocurrency data type
 * @date 2022/4/30 18:31
 */
public enum DataType {

    SPOT("Spot"), PERPETUAL("Perpetual"), FUTURES("Future"), OPTIONS("Option"), ORDER_BOOK("OrderBook");

    private String category;

    DataType(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public static DataType getEdataTypeByCategory(String category) {
        for (DataType dataType : DataType.values()) {
            if (category.equals(dataType.getCategory())) {
                return dataType;
            }
        }
        return null;
    }
}
