
/**
 * 文件加噪、还原接口
 * @author maodun
 * @Date 2019/2/1
 */
public interface FileOpI {
    /**
     * 文件加噪
     *
     * @param filePath 文件路径
     * @param rate     膨胀率（分母为8，分子为128以内2的幂数。此处rate为分子的值）
     * @throws Exception
     */
    public void addNoise(String filePath, double rate) throws Exception;

    /**
     * 文件还原
     *
     * @param filePath 文件路径
     * @throws Exception
     */
    public void removeNoise(String filePath) throws Exception;
}
