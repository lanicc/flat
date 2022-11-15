package io.github.lanicc.flat.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.Function;

/**
 * Created on 2022/10/21.
 *
 * @author lan
 */
public final class DingtalkMsgUtil {


    /**
     * [今天谁值班](dtmd://dingtalkclient/sendMessage?content=%e4%bb%8a%e5%a4%a9%e8%b0%81%e5%80%bc%e7%8f%ad)
     */
    public static <E> String constructDtmdMsg(Collection<E> items, Function<E, String> itemGetter, Function<E, String> contentGetter) {
        StringBuilder sb = new StringBuilder();
        for (E item : items) {
            try {
                sb.append(String.format("- [%s](dtmd://dingtalkclient/sendMessage?content=%s)\n", itemGetter.apply(item), URLEncoder.encode(contentGetter.apply(item), StandardCharsets.UTF_8.name())));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return sb.toString();
    }

    /**
     * [今天谁值班](dtmd://dingtalkclient/sendMessage?content=%e4%bb%8a%e5%a4%a9%e8%b0%81%e5%80%bc%e7%8f%ad)
     */
    public static <E> String constructDtmdMsg(String title, String msg) {
        try {
            return String.format("- [%s](dtmd://dingtalkclient/sendMessage?content=%s)\n", msg, URLEncoder.encode(msg, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
