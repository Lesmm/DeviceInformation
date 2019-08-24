package com.google.applicationsocket.utils;

import android.util.Log;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AndroidIdGenerator {

    // https://github.com/Lesmm/frida-scripts/blob/master/frida_47.js
    public static void test() {
        // String userKey = createUserKeyString();
        String userKey = "E4BCBEA43C8CA8B32BF577B3C81F91E97F79C1631BDD747698DEB538CCF14509";

        // 正确值应该是: fa354c92e74c6524, 得: fa354c92e74c6524
        String[] sigsHexStrings = new String[]{
                "308203563082023EA00302010202044EFEC96A300D06092A864886F70D0101050500306D310B300906035504061302434E3110300E060" +
                "355040813074265696A696E673110300E060355040713074265696A696E6731123010060355040A13094279746544616E636531123010060355040B1" +
                "3094279746544616E636531123010060355040313094D6963726F2043616F301E170D3131313233313038333535345A170D333930353138303833353" +
                "5345A306D310B300906035504061302434E3110300E060355040813074265696A696E673110300E060355040713074265696A696E673112301006035" +
                "5040A13094279746544616E636531123010060355040B13094279746544616E636531123010060355040313094D6963726F2043616F30820122300D0" +
                "6092A864886F70D01010105000382010F003082010A0282010100A46D108BE827BFF2C1AC7AD986C463B8CDA9F0E7DDC21295AF55BD16F7BFABB36FA" +
                "33B72A8E76F5A59B48B29CB6E34C38D065589636DD120F39346C37B3753830422CC0C84243FDF0E28D3E5970DCD641C70C9E2E3EC66AC14AFD351ABB" +
                "59D6885370E16B64BBFB28FBB234DFFE25F5CFB6680C84121770CF3A177BC8A28B78B7C86D30A61EB67B9FBFD92E0C8FC5EB8346A238DDFE08522F09" +
                "1C622789932D9DEBE6910B4B903D02E5F6DED69F5C13A5D1742DAC21050DFBB5F4EA615028D7A8642E4A93E075CF8F0E33A4A654AF11F4F9A4905D91" +
                "7F0BBB84E63A1A2E90B8997F936E5BF5A75EA6D19D1D93D2677886E59E95C0BB33505363C05E10A389D0B0203010001300D06092A864886F70D01010" +
                "5050003820101008704E53758907DB6785BEC65C5F51AF050873C4B0A5E08F90191B901C59969CE537942DBC9307F8FCC23B1C281A66FE4613689056" +
                "4F89FB16839AC69F836A9EA074EB03DA8578330AB50B185BD6916F195A67036060A0BBF2AED06990E72BC4DEDE895AE5E695371AA4AD26EFCD44B658" +
                "91BDA9CE02D9E71548592C2951E2CB62ED4408EEC7E828CE573FFBA0458341AEF25957B2A76403DA091322EB845B6A9903FE6AED1434012D483F1C66" +
                "8E2468CE129815E18283BAA5E1C4209691B36FFA86506FF6A4B83F24FAA744383B75968046C69703D2C5DF38BAD6920D9122CB1F7C78E8BFE2838703" +
                "59C053115E2BA0A7A03C9656A2F5A2D81F6A6FAD5DB2CD7"
        };

        // 正确值应该是: 45143a64fe716edc, 得: 45143a64fe716edc
//        String[] sigsHexStrings = new String[]{
//                "308204A830820390A003020102020900936EACBE07F201DF300D06092A864886F70D0101050500308194310B300906035504061302555" +
//                "3311330110603550408130A43616C69666F726E6961311630140603550407130D4D6F756E7461696E20566965773110300E060355040A1307416E647" +
//                "26F69643110300E060355040B1307416E64726F69643110300E06035504031307416E64726F69643122302006092A864886F70D0109011613616E647" +
//                "26F696440616E64726F69642E636F6D301E170D3038303232393031333334365A170D3335303731373031333334365A308194310B300906035504061" +
//                "3025553311330110603550408130A43616C69666F726E6961311630140603550407130D4D6F756E7461696E20566965773110300E060355040A13074" +
//                "16E64726F69643110300E060355040B1307416E64726F69643110300E06035504031307416E64726F69643122302006092A864886F70D01090116136" +
//                "16E64726F696440616E64726F69642E636F6D30820120300D06092A864886F70D01010105000382010D00308201080282010100D6931904DEC60B24B" +
//                "1EDC762E0D9D8253E3ECD6CEB1DE2FF068CA8E8BCA8CD6BD3786EA70AA76CE60EBB0F993559FFD93E77A943E7E83D4B64B8E4FEA2D3E656F1E267A81" +
//                "BBFB230B578C20443BE4C7218B846F5211586F038A14E89C2BE387F8EBECF8FCAC3DA1EE330C9EA93D0A7C3DC4AF350220D50080732E0809717EE6A0" +
//                "53359E6A694EC2CB3F284A0A466C87A94D83B31093A67372E2F6412C06E6D42F15818DFFE0381CC0CD444DA6CDDC3B82458194801B32564134FBFDE9" +
//                "8C9287748DBF5676A540D8154C8BBCA07B9E247553311C46B9AF76FDEECCC8E69E7C8A2D08E782620943F99727D3C04FE72991D99DF9BAE38A0B2177" +
//                "FA31D5B6AFEE91F020103A381FC3081F9301D0603551D0E04160414485900563D272C46AE118605A47419AC09CA8C113081C90603551D230481C1308" +
//                "1BE8014485900563D272C46AE118605A47419AC09CA8C11A1819AA48197308194310B3009060355040613025553311330110603550408130A43616C6" +
//                "9666F726E6961311630140603550407130D4D6F756E7461696E20566965773110300E060355040A1307416E64726F69643110300E060355040B13074" +
//                "16E64726F69643110300E06035504031307416E64726F69643122302006092A864886F70D0109011613616E64726F696440616E64726F69642E636F6" +
//                "D820900936EACBE07F201DF300C0603551D13040530030101FF300D06092A864886F70D010105050003820101007AAF968CEB50C441055118D0DAABA" +
//                "F015B8A765A27A715A2C2B44F221415FFDACE03095ABFA42DF70708726C2069E5C36EDDAE0400BE29452C084BC27EB6A17EAC9DBE182C204EB15311F" +
//                "455D824B656DBE4DC2240912D7586FE88951D01A8FEB5AE5A4260535DF83431052422468C36E22C2A5EF994D61DD7306AE4C9F6951BA3C12F1D1914D" +
//                "DC61F1A62DA2DF827F603FEA5603B2C540DBD7C019C36BAB29A4271C117DF523CDBC5F3817A49E0EFA60CBD7F74177E7A4F193D43F4220772666E4C4" +
//                "D83E1BD5A86087CF34F2DEC21E245CA6C2BB016E683638050D2C430EEA7C26A1C49D3760A58AB7F1A82CC938B4831384324BD0401FA12163A50570E6" +
//                "84D"
//        };

        String ssaid = getSSAID(userKey, sigsHexStrings);
        Log.d("AndroidIdGenerator", "ssaid: " + ssaid);
    }

    // https://stackoverflow.com/q/56406163
    public static String getSSAID(String userKey, String[] sigsHexStrings) {
        // Convert the user's key back to a byte array.
        final byte[] keyBytes = fromHexToByteArray(userKey);

        final Mac m;
        try {
            m = Mac.getInstance("HmacSHA256");
            m.init(new SecretKeySpec(keyBytes, m.getAlgorithm()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("HmacSHA256 is not available", e);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Key is corrupted", e);
        }

        // Mac each of the developer signatures.
        for (int i = 0; i < sigsHexStrings.length; i++) {
            String hex = sigsHexStrings[i];
            byte[] bytes = fromHexToByteArray(hex);
            byte[] sig = bytes;
            m.update(getLengthPrefix(sig), 0, 4);
            m.update(sig);
        }

        // Convert result to a string for storage in settings table. Only want first 64 bits.
        final String ssaid = toHexString(m.doFinal()).substring(0, 16).toLowerCase(Locale.US);

        // Setting Secure android_id
        return ssaid;
    }

    private static byte[] getLengthPrefix(byte[] data) {
        return ByteBuffer.allocate(4).putInt(data.length).array();
    }


    public static String createUserKeyString() {
        byte[] keyBytes = createUserKeyBytes();
        String userKey = toHexString(keyBytes);
        return userKey;
    }

    public static byte[] createUserKeyBytes() {
        byte[] keyBytes = new byte[32];
        SecureRandom rand = new SecureRandom();
        rand.nextBytes(keyBytes);
        return keyBytes;
    }


    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * Returns the hex encoded string representation of bytes.
     *
     * @param bytes Byte array to encode.
     * @return Hex encoded string representation of bytes.
     */
    public static String toHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length % 2 != 0) {
            return null;
        }

        final int byteLength = bytes.length;
        final int charCount = 2 * byteLength;
        final char[] chars = new char[charCount];

        for (int i = 0; i < byteLength; i++) {
            final int byteHex = bytes[i] & 0xFF;
            chars[i * 2] = HEX_ARRAY[byteHex >>> 4];
            chars[i * 2 + 1] = HEX_ARRAY[byteHex & 0x0F];
        }
        return new String(chars);
    }

    /**
     * Returns the decoded byte array representation of str.
     *
     * @param str Hex encoded string to decode.
     * @return Decoded byte array representation of str.
     */
    public static byte[] fromHexToByteArray(String str) {
        if (str == null || str.length() == 0 || str.length() % 2 != 0) {
            return null;
        }

        final char[] chars = str.toCharArray();
        final int charLength = chars.length;
        final byte[] bytes = new byte[charLength / 2];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] =
                    (byte) (((getIndex(chars[i * 2]) << 4) & 0xF0) | (getIndex(chars[i * 2 + 1]) & 0x0F));
        }
        return bytes;
    }

    private static int getIndex(char c) {
        for (int i = 0; i < HEX_ARRAY.length; i++) {
            if (HEX_ARRAY[i] == c) {
                return i;
            }
        }
        return -1;
    }


}
