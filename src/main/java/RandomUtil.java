import java.security.SecureRandom;

/**
 * 噪声工具类
 * @author maodun
 * @Date 2019/2/1
 */
public class RandomUtil {
    SwitchMarkUtil sw = new SwitchMarkUtil();
    SecureRandom random = new SecureRandom();

    /**
     * 生成0-num的随机数
     *
     * @param num
     * @return
     */
    public byte createNumber(int num) {
        byte result = (byte) random.nextInt(num);
        return result;
    }

    /**
     * 生成噪声内容
     *
     * @param rate
     * @return
     */
    public byte createContent(int rate) {
        byte content = 0;
        for (int i = 0; i < rate; i++) {
            byte randomcontent = (byte) ((random.nextInt(2) & 0xff) << i);
            content |= randomcontent;
        }
        return content;
    }

//  -------------------------------------以下方法暂时用不到---------------------------------------------------

    /**
     * 生成包含插入点信息的噪声
     *
     * @param rate
     * @param i
     * @param point
     * @return
     */
    public byte pointContent(int rate, int i, int point) {
        byte content;
        if (rate > 8) {
            rate = 8;
        }
        content = (byte) (point & sw.switchMark(i) >> i - 1 << rate - i | createContent(rate - i));
        return content;
    }

    /**
     * 生成包含膨胀率信息的噪声
     *
     * @param rate
     * @param i
     * @return
     */
    public byte rateContent(int rate, int i) {
        byte content;
        if (rate > 8) {
            rate = 8;
        }
        content = (byte) (rate & sw.switchMark(i - 4) >> i - 5 << rate + 4 - i | createContent(rate + 4 - i));
        return content;
    }
}
