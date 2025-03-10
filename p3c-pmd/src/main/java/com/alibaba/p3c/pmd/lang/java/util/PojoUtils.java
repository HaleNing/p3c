package com.alibaba.p3c.pmd.lang.java.util;

import java.util.List;

import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListConfig;


/**
 * POJO Utils
 */
public class PojoUtils {
    private static final List<String> POJO_SUFFIX_SET =
        NameListConfig.NAME_LIST_SERVICE.getNameList("PojoMustOverrideToStringRule", "POJO_SUFFIX_SET");

    private PojoUtils() {
    }

    public static boolean isPojo(String klass) {
        if (klass == null) {
            return false;
        }
        for (String suffix : POJO_SUFFIX_SET) {
            if (klass.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

}
