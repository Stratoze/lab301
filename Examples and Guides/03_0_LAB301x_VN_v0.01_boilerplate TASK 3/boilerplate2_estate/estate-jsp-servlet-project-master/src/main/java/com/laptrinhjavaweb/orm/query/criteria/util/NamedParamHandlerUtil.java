package com.laptrinhjavaweb.orm.query.criteria.util;

import com.laptrinhjavaweb.orm.query.criteria.criterion.NamedParam;

import java.util.Map;

public class NamedParamHandlerUtil {
    private static void handleNamedParam(Map<String, NamedParam> namedParamMap, NamedParam namedParam) {
        String propertyName = namedParam.getPropertyName();

        if (namedParamMap.containsKey(propertyName)) {
            int nextWhereId = 1;

            for (Map.Entry<String, NamedParam> entry : namedParamMap.entrySet()) {
                NamedParam curNamedParam = entry.getValue();
                String key = curNamedParam.getPropertyName();

                if (key.startsWith(namedParam.getPropertyName())) {
//                    get index of "_" character
                    int separatorIndex = key.lastIndexOf("_");

                    if (separatorIndex != -1) {
                        int curWhereId = Integer.parseInt(key.substring(separatorIndex + 1));
                        if (curWhereId >= nextWhereId) {
                            nextWhereId = curWhereId + 1;
                        }
                    }
                }
            }

//            set new property name to avoid multiple property name in a query
            propertyName = propertyName + "_" + nextWhereId;
            namedParam.setPropertyName(propertyName);
        }
    }

    public static NamedParam createNewNamedParam(Map<String, NamedParam> namedParamMap, String propertyName, Object value) {
        NamedParam namedParam = new NamedParam(propertyName, value);
        NamedParamHandlerUtil.handleNamedParam(namedParamMap, namedParam);

        return namedParam;
    }
}
