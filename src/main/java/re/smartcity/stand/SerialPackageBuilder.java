package re.smartcity.stand;

public final class SerialPackageBuilder {
    //{ a t c1 c2 }

    public static byte[] setBrightnessSunSimulator(int val) {
        if (val < 0) {
            val = 0;
        } else if (val > 99) {
            val = 99;
        }
        return createPackage(
                SerialElementAddresses.SUN_SIMULATOR,
                SerialPackageTypes.SET_BRIGHTNESS_SUN_SIMULATOR,
                String.format("%02d", val).getBytes());
    }

    public static byte[] setHighlightLevel(int val) {
        return setHighlightLevel(SerialElementAddresses.BROADCAST_ADDRESS, val);
    }

    public static byte[] setHighlightLevel(byte element, int val) {
        if (val < 0) {
            val = 0;
        } else if (val > 255) {
            val = 255;
        }
        return createPackage(
                element,
                SerialPackageTypes.SET_HIGHLIGHT_LEVEL,
                String.format("%03d", val).getBytes());
    }

    public static byte[] createPackage(byte address, byte packType, byte ... val) {
        byte[] res = new byte[2 + val.length + 2];
        res[0] = SerialServiceSymbols.PACKAGE_START;
        res[1] = address;
        res[2] = packType;
        if (val.length != 0) {
            System.arraycopy(val, 0, res, 3, val.length);
        }
        res[res.length - 1] = SerialServiceSymbols.PACKAGE_END;
        return res;
    }

    public static byte[] createPackage(SerialCommand command) {
        switch (command.getPack()) {
            case SerialPackageTypes.REQUEST_SCHEME_CONNECTION_ELEMENTS,
                    SerialPackageTypes.REQUEST_LEVEL_MODEL_ILLUMINATION,
                    SerialPackageTypes.REQUEST_ILLUMINATION_SOLAR_BATTERY,
                    SerialPackageTypes.REQUEST_SUPPLY_VOLTAGE,
                    SerialPackageTypes.REQUEST_STRENGTH_WIND_GENERATOR,
                    SerialPackageTypes.RESET_ELEMENT,
                    SerialPackageTypes.SOLAR_CELL_CALIBRATION,
                    SerialPackageTypes.CALIBRATION_WIND_GENERATOR -> {
                return createPackage(command.getAddress(), command.getPack());
            }
            case SerialPackageTypes.SET_BRIGHTNESS_SUN_SIMULATOR,
                    SerialPackageTypes.SET_WIND_STRENGTH_WIND_SIMULATOR -> {
                return createPackage(command.getAddress(), command.getPack(),
                        String.format("%02d", command.getVal()).getBytes());
            }
            default -> {
                return createPackage(command.getAddress(), command.getPack(),
                        String.format("%03d", command.getVal()).getBytes());
            }
        }
    }

    public static void printBytes(String prefix, byte[] bytes) {
        System.out.print(prefix + " ");
        for (byte aByte : bytes) {
            if (aByte == SerialServiceSymbols.SEQUENCE_SEPARATOR) {
                System.out.print(" | ");
            } else {
                System.out.printf("%02X ", aByte);
            }
        }
        System.out.println();
    }

    public static String bytesAsHexString(Byte[] bytes) {
        StringBuilder res = new StringBuilder();
        for (Byte aByte : bytes) {
            if (aByte == SerialServiceSymbols.SEQUENCE_SEPARATOR) {
                res.append(" | ");
            } else {
                res.append(String.format("%02X ", aByte));
            }
        }
        return res.toString();
    }
}
