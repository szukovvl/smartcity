package re.smartcity.common.utils;

public final class Helpers {

    public static byte[] byteArrayCopy(Byte[] src, int from_index, int length) {
        byte[] res = new byte[length];
        for (int i = 0; i < length; i++) {
            res[i] = src[i + from_index];
        }
        return res;
    }
}
