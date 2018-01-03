package com.kikatech.go.util;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author SkeeterWang Created on 2017/5/10.
 */
public class StringUtil {
    public static final String CEMENT_COMMA = ", ";

    public static boolean equals(String string1, String string2) {
        return (TextUtils.isEmpty(string1) && TextUtils.isEmpty(string2))
                || (!TextUtils.isEmpty(string1) && string1.equals(string2));
    }

    public static boolean equals(CharSequence string1, CharSequence string2) {
        return (TextUtils.isEmpty(string1) && TextUtils.isEmpty(string2))
                || (!TextUtils.isEmpty(string1) && string1.toString().equals(string2));
    }

    public static boolean equalsIgnoreCase(String string1, String string2) {
        return (TextUtils.isEmpty(string1) && TextUtils.isEmpty(string2))
                || (!TextUtils.isEmpty(string1) && string1.equalsIgnoreCase(string2));
    }

    public static boolean equalsIgnoreCase(CharSequence string1, CharSequence string2) {
        return (TextUtils.isEmpty(string1) && TextUtils.isEmpty(string2))
                || (!TextUtils.isEmpty(string1) && string1.toString().equalsIgnoreCase(string2.toString()));
    }

    public static <T> String join(List<T> list, String cement) {
        StringBuilder builder = new StringBuilder();

        if (list == null || list.isEmpty()) return null;

        for (T item : list)
            builder.append(item).append(cement);

        builder.delete(builder.length() - cement.length(), builder.length());

        return builder.toString();
    }

    public static List<String> split(String source, String cement) {
        if (source == null) return null;

        String[] output = source.split(cement);

        return Arrays.asList(output);
    }

    public static List<Boolean> splitToBoolean(String source, String cement) {
        if (source == null) return null;

        List<Boolean> output = new ArrayList<>();

        String[] sourceArray = source.split(cement);

        for (String string : sourceArray)
            output.add(Boolean.parseBoolean(string));

        return output;
    }

    public static boolean matchRegularExpression(String targetStr, String regex) {
        if (TextUtils.isEmpty(targetStr)) return false;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(targetStr);
        return matcher.matches();
    }

    /**
     * Convert string into URL encoded one.
     *
     * @param string the original string
     * @return the URLEncodes string
     */
    public static String toURLEncodeString(String string) {
        try {
            String url = URLEncoder.encode(string, "UTF-8");
            return url.replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            return string;
        }
    }

    public static int getStringLength(String string) {
        return !TextUtils.isEmpty(string) ? string.length() : 0;
    }

    public static String removeZeroWidthSpace(String string) {
        return string == null ? null : string.replaceAll("[\\p{Cf}]", "");
    }

    public static String upperCaseFirstWord(String text) {
        try {
            return String.valueOf(text.charAt(0)).toUpperCase() +
                    text.substring(1, text.length());
        } catch (Exception ignore) {
        }
        return text;
    }

    // AndroidCommon https://github.com/xybCoder/AndroidCommon

    /**
     * 獲取字串的相似度
     */
    public static double SimilarityRatio(String str, String target) {
        return 1 - (double) compareSimilarty(str, target) / Math.max(str.length(), target.length());
    }

    /**
     * 快速比較倆個字串的相似度
     * 讓長的字串放到前面有助於提交效率
     */
    /*public static double SimilarDegree(String strA, String strB) {
        String newStrA = removeSign(strA);
		String newStrB = removeSign(strB);
		int temp = Math.max(newStrA.length(), newStrB.length());
		int temp2 = longestCommonSubstring(newStrA, newStrB).length();
		return temp2 * 1.0 / temp;
	}

	//第一種實現方式
	private static String longestCommonSubstring(String strA, String strB) {
		char[] chars_strA = strA.toCharArray();
		char[] chars_strB = strB.toCharArray();
		int m = chars_strA.length;
		int n = chars_strB.length;
		int[][] matrix = new int[m + 1][n + 1];
		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				if (chars_strA[i - 1] == chars_strB[j - 1])
					matrix[i][j] = matrix[i - 1][j - 1] + 1;
				else
					matrix[i][j] = Math.max(matrix[i][j - 1], matrix[i - 1][j]);
			}
		}
		char[] result = new char[matrix[m][n]];
		int currentIndex = result.length - 1;
		while (matrix[m][n] != 0) {
			if (matrix[n] == matrix[n - 1])
				n--;
			else if (matrix[m][n] == matrix[m - 1][n])
				m--;
			else {
				result[currentIndex] = chars_strA[m - 1];
				currentIndex--;
				n--;
				m--;
			}
		}
		return new String(result);
	}
	private static boolean charReg(char charValue) {
		return (charValue >= 0x4E00 && charValue <= 0X9FA5)
			   || (charValue >= 'a' && charValue <= 'z')
			   || (charValue >= 'A' && charValue <= 'Z')
			   || (charValue >= '0' && charValue <= '9');
	}
	private static String removeSign(String str) {
		StringBuffer sb = new StringBuffer();
		for (char item : str.toCharArray())
			if (charReg(item)) {
				sb.append(item);
			}
		return sb.toString();
	}*/
    //第二種實現方式
    private static int compareSimilarty(String str, String target) {
        int d[][]; // 矩阵
        int n = str.length();
        int m = target.length();
        int i; // 遍历str的
        int j; // 遍历target的
        char ch1; // str的
        char ch2; // target的
        int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];
        for (i = 0; i <= n; i++) { // 初始化第一列
            d[i][0] = i;
        }

        for (j = 0; j <= m; j++) { // 初始化第一行
            d[0][j] = j;
        }

        for (i = 1; i <= n; i++) { // 遍历str
            ch1 = str.charAt(i - 1);
            // 去匹配target
            for (j = 1; j <= m; j++) {
                ch2 = target.charAt(j - 1);
                if (ch1 == ch2) {
                    temp = 0;
                } else {
                    temp = 1;
                }

                // 左边+1,上边+1, 左上角+temp取最小
                d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + temp);
            }
        }
        return d[n][m];
    }

    private static int min(int one, int two, int three) {
        return (one = one < two ? one : two) < three ? one : three;
    }
}
