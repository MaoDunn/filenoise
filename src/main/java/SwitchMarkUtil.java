/**
 * 符号位转换工具类
 * @author maodun
 * @Date 2019/2/1
 */
public class SwitchMarkUtil {
    /**
     * 右移高位补0（防止负数右移补符号位）
     *
     * @param a
     * @return
     */
    public byte switchMark(int a) {
        byte hex = 0;
        switch (a) {
            case 0:
                hex = 0;
                break;
            case 1:
                hex = 0x01;
                break;
            case 2:
                hex = 0x03;
                break;
            case 3:
                hex = 0x07;
                break;
            case 4:
                hex = 0x0f;
                break;
            case 5:
                hex = 0x1f;
                break;
            case 6:
                hex = 0x3f;
                break;
            case 7:
                hex = 0x7f;
                break;
            case 8:
                hex = (byte) 0xff;
                break;
        }
        return hex;
    }
}
