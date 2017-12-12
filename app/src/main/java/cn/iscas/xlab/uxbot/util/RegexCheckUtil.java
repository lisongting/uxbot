/*
 * Copyright 2017 lisongting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.iscas.xlab.uxbot.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lisongting on 2017/5/10.
 */

public class RegexCheckUtil {

    public static final String REGEX_THRESHOLD = "^0\\.[1-9]+?";

    public static final String REGEX_IP = "^0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?" +
            "\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?\\" +
            "d|1\\d\\d|2[0-4]\\d|25[0-5])$";

    public static boolean isRightThreshold(String str){
        Pattern pattern = Pattern.compile(REGEX_THRESHOLD);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    public static boolean isRightIP(String str) {
        Pattern pattern = Pattern.compile(REGEX_IP);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    public static boolean isRightPersonName(String str) {
        Pattern pattern = Pattern.compile("[\\u4E00-\\u9FA5]{2,5}(?:·[\\u4E00-\\u9FA5]{2,5})*");
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

}
