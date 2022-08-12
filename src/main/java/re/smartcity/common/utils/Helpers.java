package re.smartcity.common.utils;

public final class Helpers {

    public static byte[] byteArrayCopy(Byte[] src) {
        return byteArrayCopy(src, 0, src.length);
    }

    public static byte[] byteArrayCopy(Byte[] src, int from_index, int length) {
        byte[] res = new byte[length];
        for (int i = 0; i < length; i++) {
            res[i] = src[i + from_index];
        }
        return res;
    }

    public static double percentOf(int value, int maxvalue) {
        return (float) value * 100.0 / (float) maxvalue;
    }
}
