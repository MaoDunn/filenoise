import java.io.IOException;
import java.util.Arrays;

/**
 * 文件加噪、还原实现类
 * @author maodun
 * @Date 2019/2/1
 */
public class FileOp implements FileOpI {

    FileRW fileRW = new FileRW();
    SwitchMarkUtil sw = new SwitchMarkUtil();
    RandomUtil random = new RandomUtil();
    ConstantUtil cons = new ConstantUtil();

    /**
     * 添加文件噪声
     *
     * @param filePath    文件路径
     * @param expansivity 膨胀率(取值范围0.125，0.25，0.5，1-15的整数）
     * @throws IOException
     */
    public void addNoise(String filePath, double expansivity) throws Exception {
        //rate为膨胀率与一个字节位数的乘积，一个字节无法存放浮点数
        int rate = (int) (expansivity * cons.blength);
        if (rate > 127 && rate < 0) {
            throw new Exception("请按要求输入膨胀率");
        } else if (rate != 1 && rate != 2 && rate != 4 && rate != 8 && rate % cons.blength != 0) {
            throw new Exception("请按要求输入膨胀率");
        }
        byte[] resource = fileRW.readFiletoByte(filePath);
        double length = resource.length;
        //防止文件末字节丢失
        byte[] newbyte = Arrays.copyOf(resource, resource.length + 1);
        //新建数组，用来存放插入噪声后字节内容
        byte[] noisebyte = new byte[(int) Math.ceil((rate + cons.blength) * length / cons.blength)];
        //生成插入噪声位置
        byte point = random.createNumber(cons.blength + 1);
        int noise = 0;
        //前16字节噪声添加
        noise = firstNoise(cons.point, cons.rate, 0, 0, newbyte, noisebyte, point, noise);
        appendNoise(cons.point, cons.rate, 0, 0, cons.blength * 2 + 1, newbyte, noisebyte, point, rate, noise);
        //后半段噪声添加
        noise = firstNoise(point, rate, cons.blength * 2, (cons.blength + 1) * 2, newbyte, noisebyte, random.createContent(rate), noise);
        appendNoise(point, rate, cons.blength * 2, (cons.blength + 1) * 2, newbyte.length, newbyte, noisebyte, point, rate, noise);
        fileRW.writeByte(filePath, noisebyte);
    }

    /**
     * 解除文件噪声
     *
     * @param filePath 文件路径
     * @throws Exception
     */
    public void removeNoise(String filePath) throws Exception {
        byte[] resource = fileRW.readFiletoByte(filePath);
        double length = resource.length;
        //获取噪声插入点信息
        byte point = extractNoise(cons.point, 0, cons.blength + 1, cons.blength, resource);
        if (point > cons.blength) {
            throw new Exception("非加噪文件，不能进行还原");
        }
        //获取噪声膨胀率信息
        byte rate = extractNoise(cons.point, cons.blength + 1, cons.blength * 2 + 1, cons.blength + 1, resource);
        byte[] newbyte = new byte[(int) Math.floor(length * cons.blength / (cons.blength + rate))];
        //获取源文件的前16字节内容
        restoreNoise(0, 0, cons.blength * 2 + 1, cons.rate, cons.point, resource, newbyte);
        //获取文件的后半部分内容
        restoreNoise(cons.blength * 2, (cons.blength + 1) * 2, newbyte.length, rate, point, resource, newbyte);
        fileRW.writeByte(filePath, newbyte);
    }

    /**
     * 对首个字节加噪
     *
     * @param point     插入点
     * @param rate      膨胀率
     * @param starter1  初始索引1
     * @param starter2  初始索引2
     * @param newbyte   加噪前字节数组
     * @param noisebyte 加噪后字节数组
     * @param content   噪声内容
     */
    private int firstNoise(byte point, int rate, int starter1, int starter2, byte[] newbyte, byte[] noisebyte, byte content, int noise) {
        if (point != cons.blength) {
            noise++;
        }
        if (point + rate > cons.blength) {
            if (rate >= cons.blength) {
                noisebyte[starter2] = (byte) ((byte) ((newbyte[starter1] >> cons.blength - point & sw.switchMark(point)) << cons.blength - point) | content >> point & sw.switchMark(cons.blength - point));
            } else if (point == cons.blength) {
                noisebyte[starter2] = newbyte[starter1];
            } else {
                noisebyte[starter2] = (byte) ((byte) ((newbyte[starter1] >> cons.blength - point & sw.switchMark(point)) << cons.blength - point) | content >> point + rate - cons.blength & sw.switchMark(cons.blength * 2 - point - rate) | ((byte) (newbyte[starter1] << point) >> point + rate) & sw.switchMark(cons.blength - point - rate));
            }
        } else {
            noisebyte[starter2] = (byte) ((byte) ((newbyte[starter1] >> cons.blength - point & sw.switchMark(point)) << cons.blength - point) | (byte) (content << cons.blength - point - rate) | ((byte) (newbyte[starter1] << point) >> point + rate) & sw.switchMark(cons.blength - point - rate));
        }
        return noise;
    }

    /**
     * 添加噪声逻辑
     *
     * @param point     插入点
     * @param rate      膨胀率
     * @param starter1  初始索引1
     * @param starter2  初始索引2
     * @param end       结束索引
     * @param newbyte   加噪前字节数组
     * @param noisebyte 加噪后字节数组
     * @param realpoint 自动生成文件噪声插入点
     * @param realrate  用户传入膨胀率
     * @param noise     噪声索引
     */
    private void appendNoise(byte point, int rate, int starter1, int starter2, int end, byte[] newbyte, byte[] noisebyte, int realpoint, int realrate, int noise) {
        byte content = 0;
        //插入噪声
        for (int i = starter1 + 1, j = starter2 + 1; i < end; i++, j++) {
            int k = i % cons.blength * rate % cons.blength;
            //当膨胀了大于1时，插入纯噪声字节
            for (int m = 1; m < rate / cons.blength; m++) {
                noisebyte[j] = random.createContent(cons.blength);
                j++;
            }
            //获取插入的噪声内容
            if (k != 0) {
                if (i <= cons.blength) {
                    if (k + point != cons.blength) {
                        content = (byte) ((realpoint & sw.switchMark(noise + 1)) >> noise);
                        noise++;
                    }
                } else if (i <= cons.blength * 2) {
                    if (k + point != cons.blength && k + point != 0) {
                        content = (byte) ((realrate & sw.switchMark(noise - (cons.blength - 1))) >> noise - cons.blength);
                        noise++;
                    }
                } else {
                    content = random.createContent(rate);
                }
            }
            //插入满一个字节，新数组向后移一个字节
            if (k == 0) {
                if (i <= cons.blength) {
                    if (k + point != 0) {
                        content = (byte) ((realpoint & sw.switchMark(noise + 1)) >> noise);
                        noise++;
                    }
                } else if (i <= cons.blength * 2) {
                    if (k + point != 0) {
                        content = (byte) ((realrate & sw.switchMark(noise - (cons.blength - 1))) >> noise - cons.blength);
                        noise++;
                    }
                } else {
                    content = random.createContent(rate);
                }
                noisebyte[j] = (byte) ((byte) ((newbyte[i - 1] >> cons.blength - point & sw.switchMark(point)) << cons.blength - point + rate) | (byte) (content << cons.blength - point) | (byte) ((byte) (newbyte[i - 1] << point) >> point & sw.switchMark(cons.blength - point)));
                if (j < noisebyte.length - 1) {
                    j++;
                    if (i <= cons.blength * 2) {
                        if (k + point != cons.blength) {
                            content = (byte) ((realrate & sw.switchMark(noise - (cons.blength - 1))) >> noise - cons.blength);
                            noise++;
                        }
                    } else {
                        content = random.createContent(rate);
                    }
                    if (rate >= cons.blength) {
                        noisebyte[j] = (byte) ((byte) ((newbyte[i] >> cons.blength - point & sw.switchMark(point)) << cons.blength - point) | content >> point & sw.switchMark(cons.blength - point));
                    } else if (rate + point > cons.blength) {
                        noisebyte[j] = (byte) ((byte) ((newbyte[i] >> cons.blength - point & sw.switchMark(point)) << cons.blength - point) | content >> rate + point - cons.blength & sw.switchMark(cons.blength * 2 - rate - point) | ((byte) (newbyte[i] << point) >> point + rate) & sw.switchMark(cons.blength - point - rate));
                    } else {
                        noisebyte[j] = (byte) ((byte) ((newbyte[i] >> cons.blength - point & sw.switchMark(point)) << cons.blength - point) | (byte) (content << cons.blength - rate - point) | ((byte) (newbyte[i] << point) >> point + rate) & sw.switchMark(cons.blength - point - rate));
                    }
                }
                continue;
            }
            //通过移位操作插入噪声组成新字节
            if (point + k < cons.blength) {
                if (k + point + rate > cons.blength) {
                    noisebyte[j] = (byte) ((byte) (newbyte[i - 1] << cons.blength - k) | (byte) ((newbyte[i] >> cons.blength - point & sw.switchMark(point)) << cons.blength - k - point) | (byte) (content >> k + point + rate - cons.blength) | ((byte) ((newbyte[i] & 0xff) << point) >> k + point + rate) & sw.switchMark(cons.blength - k - point - rate));
                } else {
                    noisebyte[j] = (byte) ((byte) (newbyte[i - 1] << cons.blength - k) | (byte) ((newbyte[i] >> cons.blength - point & sw.switchMark(point)) << cons.blength - k - point) | (byte) (content << cons.blength - k - point - rate) | ((byte) ((newbyte[i] & 0xff) << point) >> k + point + rate) & sw.switchMark(cons.blength - k - point - rate));
                }
            } else if (point + k == cons.blength) {
                noisebyte[j] = (byte) ((byte) (newbyte[i - 1] << point) | newbyte[i] >> k & sw.switchMark(cons.blength - k));
            } else {
                noisebyte[j] = (byte) ((byte) ((newbyte[i - 1] >> cons.blength - point & sw.switchMark(point)) << cons.blength * 2 + rate - point - k) | (byte) (content << cons.blength * 2 - point - k) | (byte) (newbyte[i - 1] << point) >> point + k - cons.blength & sw.switchMark(cons.blength * 2 - point - k) | newbyte[i] >> k & sw.switchMark(cons.blength - k));
            }
        }
    }

    /**
     * 提取噪声内容（插入点、膨胀率）
     *
     * @param starter  初始索引
     * @param end      结束索引
     * @param base     循环基数
     * @param resource 带噪声字节数组
     * @return
     */
    private byte extractNoise(int point, int starter, int end, int base, byte[] resource) {
        byte result = 0;
        for (int i = starter, j = 0; i < end; i++, j++) {
            int k = j % base;
            if (k + point < cons.blength) {
                result |= (byte) ((byte) (resource[i] << k + point) >> cons.blength - 1 & 0x01) << i - starter;
            } else if (k + point > cons.blength) {
                result |= (byte) ((byte) (resource[i] << k + point - (cons.blength + 1)) >> cons.blength - 1 & 0x01) << i - starter - 1;
            } else if (k + point == cons.blength) {
                continue;
            }
        }
        return result;
    }

    /**
     * 文件还原逻辑
     *
     * @param stater1  初始索引1
     * @param stater2  初始索引2
     * @param end      结束索引
     * @param rate     膨胀率
     * @param point    插入点
     * @param resource 还原前字节数组（带噪）
     * @param newbyte  还原后字节数组（解噪）
     */
    private void restoreNoise(int stater1, int stater2, int end, int rate, int point, byte[] resource, byte[] newbyte) {
        int noiseRate;
        if (rate <= cons.blength) {
            noiseRate = 1;
        } else {
            noiseRate = rate / cons.blength;
        }
        //还原文件首个字节
        if (rate > cons.blength) {
            newbyte[stater1] = (byte) ((byte) ((resource[stater2] >> cons.blength - point & sw.switchMark(point)) << cons.blength - point) | ((byte) resource[stater2 + noiseRate] << point) >> point & sw.switchMark(cons.blength - point));
        } else if (point + rate > cons.blength) {
            newbyte[stater1] = (byte) ((byte) ((resource[stater2] >> cons.blength - point & sw.switchMark(point)) << cons.blength - point) | (byte) (resource[stater2] << point + rate) >> point & sw.switchMark(cons.blength - point) | (byte) (resource[stater2 + 1] << point + rate - cons.blength) >> point & sw.switchMark(cons.blength - point));
        } else {
            newbyte[stater1] = (byte) ((byte) ((resource[stater2] >> cons.blength - point & sw.switchMark(point)) << cons.blength - point) | (byte) (resource[stater2] << point + rate) >> point & sw.switchMark(cons.blength - point) | resource[stater2 + 1] >> cons.blength - rate & sw.switchMark(rate));
        }
        for (int i = stater2 + noiseRate, j = stater1 + 1; j < end; i += noiseRate, j++) {
            int k = j % cons.blength * rate % cons.blength;
            //噪声满一个字节，噪声数组向后移一个字节
            if (k == 0) {
                if (i < resource.length - 1 - noiseRate) {
                    i++;
                    if (rate > cons.blength) {
                        newbyte[j] = (byte) ((byte) ((resource[i] >> cons.blength - point & sw.switchMark(point)) << cons.blength - point) | (byte) (resource[i + noiseRate] << point) >> point & sw.switchMark(cons.blength - point));
                    } else if (point + rate > cons.blength) {
                        newbyte[j] = (byte) ((byte) ((resource[i] >> cons.blength - point & sw.switchMark(point)) << cons.blength - point) | (byte) (resource[i + noiseRate] << point + rate - cons.blength) >> point & sw.switchMark(cons.blength - point));
                    } else {
                        newbyte[j] = (byte) ((byte) ((resource[i] >> cons.blength - point & sw.switchMark(point)) << cons.blength - point) | (byte) (resource[i] << point + rate) >> point & sw.switchMark(cons.blength - point) | resource[i + 1] >> cons.blength - rate & sw.switchMark(rate));
                    }
                }
                continue;
            }
            //通过移位操作去除噪声
            if (k + point == cons.blength) {
                newbyte[j] = (byte) ((byte) (resource[i] << cons.blength - point) | (byte) (resource[i + 1] << rate) >> point & sw.switchMark(cons.blength - point));
            } else if (k + point < cons.blength) {
                if (k + rate + point > cons.blength) {
                    if (point + rate <= cons.blength / 2) {
                        newbyte[j] = (byte) ((byte) ((resource[i] >> cons.blength - k - point & sw.switchMark(k + point)) << cons.blength - point) | (byte) (resource[i + 1] << point) >> point & sw.switchMark(cons.blength - point));
                    } else {
                        newbyte[j] = (byte) ((byte) ((resource[i] >> cons.blength - k - point & sw.switchMark(k + point)) << cons.blength - point) | (byte) (resource[i + 1] << cons.blength - k - point) >> point & sw.switchMark(cons.blength - point));
                    }
                } else {
                    newbyte[j] = (byte) ((byte) ((resource[i] >> cons.blength - k - point & sw.switchMark(k + point)) << cons.blength - point) | (byte) (resource[i] << k + rate + point) >> point & sw.switchMark(cons.blength - point) | resource[i + 1] >> cons.blength - k - rate & sw.switchMark(k + rate));
                }
            } else {
                newbyte[j] = (byte) ((byte) (resource[i] << k) | (byte) ((resource[i + 1] >> cons.blength * 2 - k - point & sw.switchMark(k + point - cons.blength)) << cons.blength - point) | (byte) (resource[i + 1] << point + k + rate - cons.blength) >> point & sw.switchMark(cons.blength - point));
            }
        }
    }
}
