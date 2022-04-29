package com.crypto.currency.common.utils;

import java.util.*;

/**
 * @author Panzi
 * @Description Utility class for collections
 * @date 2022/4/29 16:36
 */
public class CollectionUtils {

    /**
     * Checks if a collection is null or empty.
     *
     * @param collection the collection to check
     * @return true if it's null or empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if a collection is null or empty.
     *
     * @param collection the collection to check
     * @return false if it's null or empty
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * Checks if all the collections are null or empty.
     *
     * @param collections the collections to check
     * @return true if all collections are null or empty; false if any of the collections is not null or empty.
     */
    public static boolean isAllEmpty(Collection<?>... collections) {
        for (Collection<?> collection : collections) {
            if (isNotEmpty(collection)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if any of the collections is empty.
     *
     * @param collections the collections to check
     * @return true if any of the collections is null or empty; false if all collections are not null or empty.
     */
    public static boolean isAnyEmpty(Collection<?>... collections) {
        for (Collection<?> collection : collections) {
            if (isEmpty(collection)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a map is null or empty.
     *
     * @param map the map to check
     * @return true if it's null or empty
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Checks if a map is null or empty.
     *
     * @param map the map to check
     * @return false if it's null or empty
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    /**
     * Checks if all the maps are null or empty.
     *
     * @param maps the maps to check
     * @return true if all maps are null or empty; false if any of the maps is not null or empty.
     */
    public static boolean isAllEmpty(Map<?, ?>... maps) {
        for (Map<?, ?> map : maps) {
            if (isNotEmpty(map)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if any of the maps is empty.
     *
     * @param maps the maps to check
     * @return true if any of the maps is null or empty; false if all maps are not null or empty.
     */
    public static boolean isAnyEmpty(Map<?, ?>... maps) {
        for (Map<?, ?> collection : maps) {
            if (isEmpty(collection)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Samples data at certain interval by index
     *
     * @param interval the interval
     * @param data     the data to sample from
     * @param fromEnd  whether to sample data from the end as opposed to from the head by default
     * @return the sampled data
     */
    public static <T> List<T> sampleData(int interval, List<T> data, boolean fromEnd) {
        List<T> sampledData = new ArrayList<>((data.size() / interval) + 1);
        if (!fromEnd) {
            for (int i = 0; i < data.size(); i += interval) {
                sampledData.add(data.get(i));
            }
        } else {
            for (int i = data.size() - 1; i >= 0; i -= interval) {
                sampledData.add(data.get(i));
            }
            Collections.reverse(sampledData);
        }
        return sampledData;
    }

    /**
     * Returns the top n items using the comparator of the list given.
     *
     * @param data   the original data
     * @param topN   the number of items expected
     * @param sorted whether to sort the items using comparator in the resulting list
     * @param <T>    the type of the elements in the list provided
     * @return a list with n (or less if the size of the original list is smaller than N) items from the original list given
     */
    public static <T> List<T> topN(List<T> data, int topN, boolean sorted, Comparator<T> comparator) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(comparator);

        if (topN >= data.size()) {
            if (sorted) {
                data.sort(comparator.reversed());
            }
            return data;
        }

        T pivot = data.get(data.size() / 2);
        List<T> result = new ArrayList<>(topN);
        List<T> remaining = new ArrayList<>(data.size() - topN);
        for (T currentItem : data) {
            if (currentItem.equals(pivot)) {
                continue;
            }
            if (comparator.compare(currentItem, pivot) >= 0) {
                result.add(currentItem);
            } else {
                remaining.add(currentItem);
            }
        }
        // avoid continuously using the same element as pivot which will cause an infinite loop
        result.add(pivot);

        if (result.size() > topN) {
            return topN(result, topN, sorted, comparator);
        } else if (result.size() < topN) {
            List<T> remainingItems = topN(remaining, topN - result.size(), false, comparator);
            result.addAll(remainingItems);
        }

        if (sorted) {
            result.sort(comparator.reversed());
        }
        return result;
    }

    /**
     * @Description: group list by quantity
     * param list
     * param quantity
     * @return: java.util.List<java.util.List < T>>
     * @Author: Ivory Yin
     * @Date: 2021/8/18
     */
    public static <T> List<List<T>> groupListByQuantity(List<T> list, int quantity) {
        if (isEmpty(list)) {
            return null;
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("illegal quantity.");
        }
        List<List<T>> wrapList = new ArrayList<List<T>>();
        int count = 0;
        while (count < list.size()) {
            wrapList.add(new ArrayList<T>(list.subList(count, Math.min((count + quantity), list.size()))));
            count += quantity;
        }

        return wrapList;
    }
}
